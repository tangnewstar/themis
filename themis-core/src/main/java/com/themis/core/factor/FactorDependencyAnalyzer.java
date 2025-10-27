package com.themis.core.factor;

import java.util.*;

/**
 * 因子依赖分析器
 * 负责分析因子间的依赖关系、检测循环依赖、优化计算顺序
 */
public class FactorDependencyAnalyzer {
    
    private final Map<String, FactorDefinition> factorRegistry;
    
    public FactorDependencyAnalyzer(Map<String, FactorDefinition> factorRegistry) {
        this.factorRegistry = factorRegistry;
    }
    
    /**
     * 分析因子的所有依赖（递归）
     * 
     * @param factorId 因子ID
     * @return 依赖的因子ID列表（拓扑排序后）
     */
    public List<String> analyzeDependencies(String factorId) {
        Set<String> visited = new HashSet<>();
        List<String> result = new ArrayList<>();
        
        buildDependencyTree(factorId, visited, result);
        
        return result;
    }
    
    /**
     * 递归构建依赖树
     */
    private void buildDependencyTree(String factorId, Set<String> visited, List<String> result) {
        if (visited.contains(factorId)) {
            return;
        }
        
        visited.add(factorId);
        
        FactorDefinition factor = factorRegistry.get(factorId);
        if (factor == null || factor.getDependencies() == null) {
            return;
        }
        
        // 先处理依赖的因子
        for (String depId : factor.getDependencies()) {
            buildDependencyTree(depId, visited, result);
        }
        
        // 再添加当前因子
        result.add(factorId);
    }
    
    /**
     * 检测循环依赖
     * 
     * @param factorId 因子ID
     * @return 如果存在循环依赖返回true
     */
    public boolean hasCircularDependency(String factorId) {
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        
        return hasCircularDependencyDFS(factorId, visiting, visited);
    }
    
    /**
     * DFS检测循环依赖
     */
    private boolean hasCircularDependencyDFS(String factorId, Set<String> visiting, Set<String> visited) {
        if (visiting.contains(factorId)) {
            return true; // 发现循环
        }
        
        if (visited.contains(factorId)) {
            return false; // 已经访问过，无循环
        }
        
        FactorDefinition factor = factorRegistry.get(factorId);
        if (factor == null || factor.getDependencies() == null) {
            return false;
        }
        
        visiting.add(factorId);
        
        for (String depId : factor.getDependencies()) {
            if (hasCircularDependencyDFS(depId, visiting, visited)) {
                return true;
            }
        }
        
        visiting.remove(factorId);
        visited.add(factorId);
        
        return false;
    }
    
    /**
     * 获取因子的直接依赖
     */
    public List<String> getDirectDependencies(String factorId) {
        FactorDefinition factor = factorRegistry.get(factorId);
        if (factor == null || factor.getDependencies() == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(factor.getDependencies());
    }
    
    /**
     * 获取依赖于指定因子的所有因子（反向依赖）
     */
    public List<String> getDependents(String factorId) {
        List<String> dependents = new ArrayList<>();
        
        for (Map.Entry<String, FactorDefinition> entry : factorRegistry.entrySet()) {
            FactorDefinition factor = entry.getValue();
            if (factor.getDependencies() != null && 
                factor.getDependencies().contains(factorId)) {
                dependents.add(entry.getKey());
            }
        }
        
        return dependents;
    }
    
    /**
     * 拓扑排序 - 返回所有因子的计算顺序
     */
    public List<String> topologicalSort(Collection<String> factorIds) {
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> adjList = new HashMap<>();
        
        // 初始化
        for (String factorId : factorIds) {
            inDegree.put(factorId, 0);
            adjList.put(factorId, new ArrayList<>());
        }
        
        // 构建图
        for (String factorId : factorIds) {
            FactorDefinition factor = factorRegistry.get(factorId);
            if (factor != null && factor.getDependencies() != null) {
                for (String depId : factor.getDependencies()) {
                    if (factorIds.contains(depId)) {
                        adjList.get(depId).add(factorId);
                        inDegree.put(factorId, inDegree.get(factorId) + 1);
                    }
                }
            }
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
            String factorId = queue.poll();
            result.add(factorId);
            
            for (String dependent : adjList.get(factorId)) {
                int newDegree = inDegree.get(dependent) - 1;
                inDegree.put(dependent, newDegree);
                if (newDegree == 0) {
                    queue.offer(dependent);
                }
            }
        }
        
        // 检查是否有环
        if (result.size() != factorIds.size()) {
            throw new IllegalStateException("Circular dependency detected in factors");
        }
        
        return result;
    }
    
    /**
     * 计算因子依赖深度
     */
    public int getDependencyDepth(String factorId) {
        return calculateDepth(factorId, new HashSet<>());
    }
    
    private int calculateDepth(String factorId, Set<String> visited) {
        if (visited.contains(factorId)) {
            return 0;
        }
        
        visited.add(factorId);
        
        FactorDefinition factor = factorRegistry.get(factorId);
        if (factor == null || factor.getDependencies() == null || factor.getDependencies().isEmpty()) {
            return 0;
        }
        
        int maxDepth = 0;
        for (String depId : factor.getDependencies()) {
            int depth = calculateDepth(depId, new HashSet<>(visited));
            maxDepth = Math.max(maxDepth, depth);
        }
        
        return maxDepth + 1;
    }
    
    /**
     * 获取依赖图的可视化表示
     */
    public Map<String, Object> getDependencyGraph(Collection<String> factorIds) {
        Map<String, Object> graph = new HashMap<>();
        List<Map<String, String>> nodes = new ArrayList<>();
        List<Map<String, String>> edges = new ArrayList<>();
        
        for (String factorId : factorIds) {
            Map<String, String> node = new HashMap<>();
            node.put("id", factorId);
            FactorDefinition factor = factorRegistry.get(factorId);
            if (factor != null) {
                node.put("name", factor.getFactorName());
                node.put("type", factor.getFactorType().toString());
            }
            nodes.add(node);
            
            if (factor != null && factor.getDependencies() != null) {
                for (String depId : factor.getDependencies()) {
                    if (factorIds.contains(depId)) {
                        Map<String, String> edge = new HashMap<>();
                        edge.put("from", depId);
                        edge.put("to", factorId);
                        edges.add(edge);
                    }
                }
            }
        }
        
        graph.put("nodes", nodes);
        graph.put("edges", edges);
        
        return graph;
    }
}
