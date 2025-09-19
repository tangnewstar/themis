package com.themis.orchestration.workflow;

/**
 * Enumeration of workflow execution statuses.
 */
public enum WorkflowStatus {
    /**
     * Workflow is pending execution.
     */
    PENDING("Pending", "Workflow is waiting to be executed"),
    
    /**
     * Workflow is currently running.
     */
    RUNNING("Running", "Workflow is currently executing"),
    
    /**
     * Workflow has completed successfully.
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
     * Workflow is paused and can be resumed.
     */
    PAUSED("Paused", "Workflow execution is paused"),
    
    /**
     * Workflow is suspended waiting for external input.
     */
    SUSPENDED("Suspended", "Workflow is suspended waiting for external input");
    
    private final String displayName;
    private final String description;
    
    WorkflowStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    
    /**
     * Checks if the workflow is in a terminal state (completed, failed, or cancelled).
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
    
    /**
     * Checks if the workflow is still active (running, paused, or suspended).
     */
    public boolean isActive() {
        return this == RUNNING || this == PAUSED || this == SUSPENDED;
    }
}