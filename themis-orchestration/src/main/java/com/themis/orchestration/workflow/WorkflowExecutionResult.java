package com.themis.orchestration.workflow;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents the result of a workflow execution.
 */
public class WorkflowExecutionResult {
    
    private String workflowId;
    private String workflowName;
    private WorkflowStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Map<String, Object> context;
    private List<StepExecutionResult> stepResults;
    private String errorMessage;
    private Exception exception;
    
    // Constructors
    public WorkflowExecutionResult() {}
    
    public WorkflowExecutionResult(String workflowId, String workflowName) {
        this.workflowId = workflowId;
        this.workflowName = workflowName;
    }
    
    // Getters and Setters
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    
    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }
    
    public WorkflowStatus getStatus() { return status; }
    public void setStatus(WorkflowStatus status) { this.status = status; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }
    
    public List<StepExecutionResult> getStepResults() { return stepResults; }
    public void setStepResults(List<StepExecutionResult> stepResults) { this.stepResults = stepResults; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public Exception getException() { return exception; }
    public void setException(Exception exception) { this.exception = exception; }
    
    /**
     * Checks if the workflow execution was successful.
     */
    public boolean isSuccess() {
        return status == WorkflowStatus.COMPLETED;
    }
    
    /**
     * Checks if the workflow execution failed.
     */
    public boolean isFailure() {
        return status == WorkflowStatus.FAILED;
    }
    
    /**
     * Gets the total execution time in milliseconds.
     */
    public long getExecutionTimeMillis() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return String.format("WorkflowExecutionResult{workflowId='%s', status=%s, steps=%d}", 
                           workflowId, status, stepResults != null ? stepResults.size() : 0);
    }
}