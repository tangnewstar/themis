package com.themis.orchestration.workflow;

/**
 * Enumeration of workflow execution statuses for asynchronous execution tracking.
 */
public enum WorkflowExecutionStatus {
    /**
     * Workflow execution has been submitted but not yet started.
     */
    SUBMITTED("Submitted", "Workflow execution has been submitted"),
    
    /**
     * Workflow execution is currently in progress.
     */
    IN_PROGRESS("In Progress", "Workflow execution is in progress"),
    
    /**
     * Workflow execution completed successfully.
     */
    COMPLETED("Completed", "Workflow execution completed successfully"),
    
    /**
     * Workflow execution failed.
     */
    FAILED("Failed", "Workflow execution failed"),
    
    /**
     * Workflow execution was cancelled.
     */
    CANCELLED("Cancelled", "Workflow execution was cancelled"),
    
    /**
     * Workflow execution timed out.
     */
    TIMEOUT("Timeout", "Workflow execution timed out");
    
    private final String displayName;
    private final String description;
    
    WorkflowExecutionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    
    /**
     * Checks if the execution status is terminal.
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == TIMEOUT;
    }
    
    /**
     * Checks if the execution is still active.
     */
    public boolean isActive() {
        return this == SUBMITTED || this == IN_PROGRESS;
    }
}