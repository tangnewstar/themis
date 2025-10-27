package com.themis.orchestration.dag;

import java.util.*;
import java.util.concurrent.*;

/**
 * DAG工作流引擎
 * 基于有向无环图的规则编排执行引擎
 */
public class DAGWorkflowEngine {
    
    private final ExecutorService executorService;
    
    public DAGWorkflowEngine() {
        this(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }
    
    public DAGWorkflowEngine(ExecutorService executorService) {
        this.executorService = executorService;
    }
    
    /**
     * 执行DAG工作流
     * 
     * @param workflow DAG工作流定义
     * @param context 执行上下文
     * @return 执行结果
     */
    public DAGExecutionResult execute(DAGWorkflow workflow, Map<String, Object> context) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 拓扑排序
            List<String> executionOrder = topologicalSort(workflow);
            
            // 按层级分组（可并行执行的节点）
            List<List<DAGNode>> levels = groupByLevel(workflow, executionOrder);
            
            Map<String, Object> results = new ConcurrentHashMap<>();
            
            // 逐层执行
            for (List<DAGNode> level : levels) {
                executeLevelParallel(level, context, results);
            }
            
            long endTime = System.currentTimeMillis();
            return DAGExecutionResult.success(results, endTime - startTime);
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            return DAGExecutionResult.failure(e.getMessage(), endTime - startTime);
        }
    }
    
    /**
     * 拓扑排序
     */
    private List<String> topologicalSort(DAGWorkflow workflow) {
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> adjList = new HashMap<>();
        
        // 初始化
        for (DAGNode node : workflow.getNodes()) {
            inDegree.put(node.getId(), 0);
            adjList.put(node.getId(), new ArrayList<>());
        }
        
        // 构建图
        for (DAGEdge edge : workflow.getEdges()) {
            adjList.get(edge.getFrom()).add(edge.getTo());
            inDegree.put(edge.getTo(), inDegree.get(edge.getTo()) + 1);
        }
        
        // Kahn算法
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }
        
        List<String> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String nodeId = queue.poll();
            result.add(nodeId);
            
            for (String neighbor : adjList.get(nodeId)) {
                int newDegree = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, newDegree);
                if (newDegree == 0) {
                    queue.offer(neighbor);
                }
            }
        }
        
        if (result.size() != workflow.getNodes().size()) {
            throw new IllegalStateException("Circular dependency detected in DAG workflow");
        }
        
        return result;
    }
    
    /**
     * 按层级分组
     */
    private List<List<DAGNode>> groupByLevel(DAGWorkflow workflow, List<String> executionOrder) {
        Map<String, Integer> levels = new HashMap<>();
        Map<String, DAGNode> nodeMap = new HashMap<>();
        
        for (DAGNode node : workflow.getNodes()) {
            nodeMap.put(node.getId(), node);
        }
        
        // 计算每个节点的层级
        for (String nodeId : executionOrder) {
            int maxLevel = 0;
            for (DAGEdge edge : workflow.getEdges()) {
                if (edge.getTo().equals(nodeId)) {
                    int fromLevel = levels.getOrDefault(edge.getFrom(), 0);
                    maxLevel = Math.max(maxLevel, fromLevel + 1);
                }
            }
            levels.put(nodeId, maxLevel);
        }
        
        // 按层级分组
        int maxLevelNum = levels.values().stream().max(Integer::compareTo).orElse(0);
        List<List<DAGNode>> result = new ArrayList<>(maxLevelNum + 1);
        for (int i = 0; i <= maxLevelNum; i++) {
            result.add(new ArrayList<>());
        }
        
        for (Map.Entry<String, Integer> entry : levels.entrySet()) {
            result.get(entry.getValue()).add(nodeMap.get(entry.getKey()));
        }
        
        return result;
    }
    
    /**
     * 并行执行同一层级的节点
     */
    private void executeLevelParallel(List<DAGNode> level, Map<String, Object> context, 
                                     Map<String, Object> results) {
        if (level.isEmpty()) {
            return;
        }
        
        if (level.size() == 1) {
            // 单节点直接执行
            DAGNode node = level.get(0);
            Object result = executeNode(node, context, results);
            results.put(node.getId(), result);
        } else {
            // 多节点并行执行
            List<Future<NodeExecutionResult>> futures = new ArrayList<>();
            
            for (DAGNode node : level) {
                Future<NodeExecutionResult> future = executorService.submit(() -> {
                    Object result = executeNode(node, context, results);
                    return new NodeExecutionResult(node.getId(), result, true, null);
                });
                futures.add(future);
            }
            
            // 等待所有节点完成
            for (Future<NodeExecutionResult> future : futures) {
                try {
                    NodeExecutionResult nodeResult = future.get();
                    results.put(nodeResult.nodeId, nodeResult.result);
                } catch (Exception e) {
                    throw new RuntimeException("Node execution failed", e);
                }
            }
        }
    }
    
    /**
     * 执行单个节点
     */
    private Object executeNode(DAGNode node, Map<String, Object> context, Map<String, Object> results) {
        try {
            return node.execute(context, results);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute node: " + node.getId(), e);
        }
    }
    
    /**
     * 关闭引擎
     */
    public void shutdown() {
        executorService.shutdown();
    }
    
    /**
     * 节点执行结果
     */
    private static class NodeExecutionResult {
        final String nodeId;
        final Object result;
        final boolean success;
        final String error;
        
        NodeExecutionResult(String nodeId, Object result, boolean success, String error) {
            this.nodeId = nodeId;
            this.result = result;
            this.success = success;
            this.error = error;
        }
    }
}
