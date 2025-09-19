package com.themis.orchestration.workflow;

/**
 * Exception thrown when workflow execution fails.
 */
public class WorkflowExecutionException extends Exception {
    
    private final String workflowId;
    private final String stepId;
    
    public WorkflowExecutionException(String message) {
        super(message);
        this.workflowId = null;
        this.stepId = null;
    }
    
    public WorkflowExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.workflowId = null;
        this.stepId = null;
    }
    
    public WorkflowExecutionException(String workflowId, String message) {
        super(message);
        this.workflowId = workflowId;
        this.stepId = null;
    }
    
    public WorkflowExecutionException(String workflowId, String message, Throwable cause) {
        super(message, cause);
        this.workflowId = workflowId;
        this.stepId = null;
    }
    
    public WorkflowExecutionException(String workflowId, String stepId, String message) {
        super(message);
        this.workflowId = workflowId;
        this.stepId = stepId;
    }
    
    public WorkflowExecutionException(String workflowId, String stepId, String message, Throwable cause) {
        super(message, cause);
        this.workflowId = workflowId;
        this.stepId = stepId;
    }
    
    public String getWorkflowId() { return workflowId; }
    public String getStepId() { return stepId; }
}