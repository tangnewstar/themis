package com.themis.core.engine;

/**
 * Enumeration of rule execution modes.
 */
public enum RuleExecutionMode {
    
    /**
     * Execute rules sequentially in order of priority.
     * Stop on first failure.
     */
    SEQUENTIAL("Sequential", "Execute rules one by one in priority order"),
    
    /**
     * Execute all rules regardless of individual failures.
     * Collect all results and continue processing.
     */
    CONTINUE_ON_ERROR("Continue on Error", "Execute all rules even if some fail"),
    
    /**
     * Execute rules in parallel where possible.
     * Suitable for independent rules without dependencies.
     */
    PARALLEL("Parallel", "Execute independent rules in parallel"),
    
    /**
     * Validation mode - only validate rules without executing actions.
     * Used for testing and validation purposes.
     */
    VALIDATION_ONLY("Validation Only", "Validate rules without executing actions"),
    
    /**
     * Debug mode - execute with detailed logging and tracing.
     * Useful for troubleshooting and development.
     */
    DEBUG("Debug", "Execute with detailed logging and tracing"),
    
    /**
     * Simulation mode - execute rules but don't persist changes.
     * Used for what-if analysis and testing.
     */
    SIMULATION("Simulation", "Execute without persisting changes");
    
    private final String displayName;
    private final String description;
    
    RuleExecutionMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this mode allows parallel execution.
     */
    public boolean allowsParallelExecution() {
        return this == PARALLEL;
    }
    
    /**
     * Checks if this mode continues execution on errors.
     */
    public boolean continuesOnError() {
        return this == CONTINUE_ON_ERROR || this == VALIDATION_ONLY || this == SIMULATION;
    }
    
    /**
     * Checks if this mode executes actions or only validates.
     */
    public boolean executesActions() {
        return this != VALIDATION_ONLY;
    }
    
    /**
     * Checks if this mode persists changes.
     */
    public boolean persistsChanges() {
        return this != SIMULATION && this != VALIDATION_ONLY;
    }
    
    /**
     * Checks if this mode provides detailed logging.
     */
    public boolean providesDetailedLogging() {
        return this == DEBUG;
    }
}