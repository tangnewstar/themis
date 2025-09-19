package com.themis.orchestration.workflow;

/**
 * Enumeration of workflow step execution statuses.
 */
public enum StepStatus {
    /**
     * Step is pending execution.
     */
    PENDING("Pending", "Step is waiting to be executed"),
    
    /**
     * Step is currently running.
     */
    RUNNING("Running", "Step is currently executing"),
    
    /**
     * Step has completed successfully.
     */
    COMPLETED("Completed", "Step execution completed successfully"),
    
    /**
     * Step execution failed.
     */
    FAILED("Failed", "Step execution failed"),
    
    /**
     * Step execution was skipped due to conditions.
     */
    SKIPPED("Skipped", "Step execution was skipped"),
    
    /**
     * Step execution was cancelled.
     */
    CANCELLED("Cancelled", "Step execution was cancelled");
    
    private final String displayName;
    private final String description;
    
    StepStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    
    /**
     * Checks if the step is in a terminal state.
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == SKIPPED || this == CANCELLED;
    }
    
    /**
     * Checks if the step is still active.
     */
    public boolean isActive() {
        return this == RUNNING;
    }
}