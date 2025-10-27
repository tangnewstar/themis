package com.themis.orchestration.dag;

/**
 * DAG边定义
 */
public class DAGEdge {
    
    private String from;
    private String to;
    private String condition;
    
    public DAGEdge() {
    }
    
    public DAGEdge(String from, String to) {
        this.from = from;
        this.to = to;
    }
    
    public DAGEdge(String from, String to, String condition) {
        this.from = from;
        this.to = to;
        this.condition = condition;
    }
    
    public String getFrom() {
        return from;
    }
    
    public void setFrom(String from) {
        this.from = from;
    }
    
    public String getTo() {
        return to;
    }
    
    public void setTo(String to) {
        this.to = to;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
}
