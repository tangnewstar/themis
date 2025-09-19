package com.themis.core.model;

/**
 * Enumeration of supported rule types in the Themis Rule Engine.
 */
public enum RuleType {
    /**
     * Simple condition-action rules.
     * Example: if age > 18 then eligible = true
     */
    EXPRESSION("Expression Rule", "Simple condition-action rules"),
    
    /**
     * Multi-condition decision trees with branching logic.
     * Example: Credit approval workflows with multiple decision paths
     */
    BRANCHING("Branching Rule", "Multi-condition decision trees"),
    
    /**
     * Mathematical formula calculations.
     * Example: score = weight1 * factor1 + weight2 * factor2
     */
    FORMULA("Formula Rule", "Mathematical calculations"),
    
    /**
     * Decision tables with multiple dimensions.
     * Example: Tax rate calculation based on income and region
     */
    TABLE("Rule Table", "Decision tables with multiple dimensions"),
    
    /**
     * Multi-factor scoring models.
     * Example: Credit score calculation with weighted factors
     */
    SCORECARD("Scorecard", "Multi-factor scoring models"),
    
    /**
     * Combination of multiple rule types in a workflow.
     * Example: Complex business processes with multiple rule evaluations
     */
    COMPOSITE("Composite Rule", "Combination of multiple rule types");
    
    private final String displayName;
    private final String description;
    
    RuleType(String displayName, String description) {
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
     * Checks if this rule type supports conditional logic.
     */
    public boolean supportsConditions() {
        return this == EXPRESSION || this == BRANCHING || this == TABLE || this == COMPOSITE;
    }
    
    /**
     * Checks if this rule type supports mathematical calculations.
     */
    public boolean supportsFormulas() {
        return this == FORMULA || this == SCORECARD || this == COMPOSITE;
    }
    
    /**
     * Checks if this rule type supports complex workflows.
     */
    public boolean supportsWorkflow() {
        return this == BRANCHING || this == COMPOSITE;
    }
}