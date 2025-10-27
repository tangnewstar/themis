package com.themis.orchestration.dag;

import java.util.*;

/**
 * DAG工作流定义
 */
public class DAGWorkflow {
    
    private String workflowId;
    private String workflowName;
    private List<DAGNode> nodes;
    private List<DAGEdge> edges;
    private Map<String, Object> metadata;
    
    public DAGWorkflow() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.metadata = new HashMap<>();
    }
    
    public DAGWorkflow(String workflowId, String workflowName) {
        this();
        this.workflowId = workflowId;
        this.workflowName = workflowName;
    }
    
    /**
     * 添加节点
     */
    public void addNode(DAGNode node) {
        nodes.add(node);
    }
    
    /**
     * 添加边
     */
    public void addEdge(String from, String to) {
        edges.add(new DAGEdge(from, to));
    }
    
    /**
     * 添加条件边
     */
    public void addEdge(String from, String to, String condition) {
        edges.add(new DAGEdge(from, to, condition));
    }
    
    // Getters and Setters
    
    public String getWorkflowId() {
        return workflowId;
    }
    
    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }
    
    public String getWorkflowName() {
        return workflowName;
    }
    
    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }
    
    public List<DAGNode> getNodes() {
        return nodes;
    }
    
    public void setNodes(List<DAGNode> nodes) {
        this.nodes = nodes;
    }
    
    public List<DAGEdge> getEdges() {
        return edges;
    }
    
    public void setEdges(List<DAGEdge> edges) {
        this.edges = edges;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
