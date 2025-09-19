package com.themis.orchestration.workflow;

import java.time.LocalDateTime;

/**
 * Represents the result of a workflow step execution.
 */
public class StepExecutionResult {
    
    private String stepId;
    private String stepName;
    private StepStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Object input;
    private Object output;
    private String errorMessage;
    private Exception exception;
    
    // Constructors
    public StepExecutionResult() {}
    
    public StepExecutionResult(String stepId, String stepName) {
        this.stepId = stepId;
        this.stepName = stepName;
    }
    
    // Getters and Setters
    public String getStepId() { return stepId; }
    public void setStepId(String stepId) { this.stepId = stepId; }
    
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    
    public StepStatus getStatus() { return status; }
    public void setStatus(StepStatus status) { this.status = status; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public Object getInput() { return input; }
    public void setInput(Object input) { this.input = input; }
    
    public Object getOutput() { return output; }
    public void setOutput(Object output) { this.output = output; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public Exception getException() { return exception; }
    public void setException(Exception exception) { this.exception = exception; }
    
    /**
     * Checks if the step execution was successful.
     */
    public boolean isSuccess() {
        return status == StepStatus.COMPLETED;
    }
    
    /**
     * Checks if the step execution failed.
     */
    public boolean isFailure() {
        return status == StepStatus.FAILED;
    }
    
    /**
     * Gets the execution time in milliseconds.
     */
    public long getExecutionTimeMillis() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return String.format("StepExecutionResult{stepId='%s', status=%s, executionTime=%dms}", 
                           stepId, status, getExecutionTimeMillis());
    }
}