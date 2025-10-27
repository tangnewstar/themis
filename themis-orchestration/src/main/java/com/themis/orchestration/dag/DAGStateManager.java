package com.themis.orchestration.dag;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DAG工作流状态管理器
 * 管理DAG执行过程中的状态变化
 */
public class DAGStateManager {
    
    /**
     * 节点状态映射
     */
    private final Map<String, NodeState> nodeStates = new ConcurrentHashMap<>();
    
    /**
     * 工作流状态
     */
    private volatile WorkflowState workflowState = WorkflowState.PENDING;
    
    /**
     * 开始时间
     */
    private long startTime;
    
    /**
     * 结束时间
     */
    private long endTime;
    
    /**
     * 初始化DAG工作流
     */
    public void initialize(DAGWorkflow workflow) {
        this.workflowState = WorkflowState.PENDING;
        this.nodeStates.clear();
        
        // 初始化所有节点状态
        for (DAGNode node : workflow.getNodes()) {
            nodeStates.put(node.getId(), NodeState.PENDING);
        }
    }
    
    /**
     * 开始工作流执行
     */
    public void start() {
        this.workflowState = WorkflowState.RUNNING;
        this.startTime = System.currentTimeMillis();
    }
    
    /**
     * 完成工作流执行
     */
    public void complete() {
        this.workflowState = WorkflowState.COMPLETED;
        this.endTime = System.currentTimeMillis();
    }
    
    /**
     * 工作流执行失败
     */
    public void fail() {
        this.workflowState = WorkflowState.FAILED;
        this.endTime = System.currentTimeMillis();
    }
    
    /**
     * 更新节点状态
     */
    public void updateNodeState(String nodeId, NodeState state) {
        nodeStates.put(nodeId, state);
    }
    
    /**
     * 获取节点状态
     */
    public NodeState getNodeState(String nodeId) {
        return nodeStates.getOrDefault(nodeId, NodeState.PENDING);
    }
    
    /**
     * 获取工作流状态
     */
    public WorkflowState getWorkflowState() {
        return workflowState;
    }
    
    /**
     * 检查所有节点是否完成
     */
    public boolean isAllNodesCompleted() {
        return nodeStates.values().stream()
            .allMatch(state -> state == NodeState.COMPLETED || state == NodeState.SKIPPED);
    }
    
    /**
     * 检查是否有失败节点
     */
    public boolean hasFailedNodes() {
        return nodeStates.values().stream()
            .anyMatch(state -> state == NodeState.FAILED);
    }
    
    /**
     * 获取执行时长
     */
    public long getExecutionDuration() {
        if (startTime == 0) {
            return 0;
        }
        
        long end = endTime > 0 ? endTime : System.currentTimeMillis();
        return end - startTime;
    }
    
    /**
     * 获取状态摘要
     */
    public StateSummary getSummary() {
        long completed = nodeStates.values().stream()
            .filter(state -> state == NodeState.COMPLETED)
            .count();
        
        long failed = nodeStates.values().stream()
            .filter(state -> state == NodeState.FAILED)
            .count();
        
        long running = nodeStates.values().stream()
            .filter(state -> state == NodeState.RUNNING)
            .count();
        
        return new StateSummary(
            workflowState,
            nodeStates.size(),
            (int) completed,
            (int) failed,
            (int) running,
            getExecutionDuration()
        );
    }
    
    /**
     * 重置状态
     */
    public void reset() {
        nodeStates.clear();
        workflowState = WorkflowState.PENDING;
        startTime = 0;
        endTime = 0;
    }
    
    /**
     * 节点状态
     */
    public enum NodeState {
        /**
         * 等待执行
         */
        PENDING,
        
        /**
         * 执行中
         */
        RUNNING,
        
        /**
         * 执行完成
         */
        COMPLETED,
        
        /**
         * 执行失败
         */
        FAILED,
        
        /**
         * 已跳过
         */
        SKIPPED
    }
    
    /**
     * 工作流状态
     */
    public enum WorkflowState {
        /**
         * 等待执行
         */
        PENDING,
        
        /**
         * 执行中
         */
        RUNNING,
        
        /**
         * 执行完成
         */
        COMPLETED,
        
        /**
         * 执行失败
         */
        FAILED,
        
        /**
         * 已取消
         */
        CANCELLED
    }
    
    /**
     * 状态摘要
     */
    public static class StateSummary {
        private final WorkflowState workflowState;
        private final int totalNodes;
        private final int completedNodes;
        private final int failedNodes;
        private final int runningNodes;
        private final long executionDuration;
        
        public StateSummary(WorkflowState workflowState, int totalNodes, 
                          int completedNodes, int failedNodes, int runningNodes,
                          long executionDuration) {
            this.workflowState = workflowState;
            this.totalNodes = totalNodes;
            this.completedNodes = completedNodes;
            this.failedNodes = failedNodes;
            this.runningNodes = runningNodes;
            this.executionDuration = executionDuration;
        }
        
        public WorkflowState getWorkflowState() {
            return workflowState;
        }
        
        public int getTotalNodes() {
            return totalNodes;
        }
        
        public int getCompletedNodes() {
            return completedNodes;
        }
        
        public int getFailedNodes() {
            return failedNodes;
        }
        
        public int getRunningNodes() {
            return runningNodes;
        }
        
        public long getExecutionDuration() {
            return executionDuration;
        }
        
        public double getCompletionPercent() {
            return totalNodes > 0 ? (double) completedNodes / totalNodes * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format("StateSummary{state=%s, progress=%d/%d (%.1f%%), failed=%d, duration=%dms}",
                workflowState, completedNodes, totalNodes, getCompletionPercent(), 
                failedNodes, executionDuration);
        }
    }
}
