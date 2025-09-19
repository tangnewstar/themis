package com.themis.core.engine;

import com.themis.core.model.RuleDefinition;
import com.themis.core.model.RuleType;
import com.themis.core.model.ValidationResult;

import java.util.List;

/**
 * Core interface for rule execution engines in the Themis Rule Engine.
 * Implementations provide different execution strategies (Default, RETE, etc.).
 */
public interface RuleEngine {
    
    /**
     * Executes a single rule with the given context.
     * 
     * @param context The execution context containing input data and variables
     * @param rule The rule definition to execute
     * @return The execution result with outputs and status
     * @throws RuleExecutionException if execution fails
     */
    RuleExecutionResult execute(RuleContext context, RuleDefinition rule) throws RuleExecutionException;
    
    /**
     * Executes multiple rules in batch with the given context.
     * Rules are executed according to their priority and dependencies.
     * 
     * @param context The execution context containing input data and variables
     * @param rules The list of rule definitions to execute
     * @return The batch execution result with all individual results
     * @throws RuleExecutionException if execution fails
     */
    BatchExecutionResult executeBatch(RuleContext context, List<RuleDefinition> rules) throws RuleExecutionException;
    
    /**
     * Validates a rule definition for syntax and semantic correctness.
     * 
     * @param rule The rule definition to validate
     * @return The validation result with any errors or warnings
     */
    ValidationResult validate(RuleDefinition rule);
    
    /**
     * Gets the capabilities of this rule engine implementation.
     * 
     * @return Engine capabilities information
     */
    EngineCapabilities getCapabilities();
    
    /**
     * Gets the name of this rule engine implementation.
     * 
     * @return Engine name (e.g., "DefaultEngine", "RETEEngine")
     */
    String getEngineName();
    
    /**
     * Gets the version of this rule engine implementation.
     * 
     * @return Engine version
     */
    String getEngineVersion();
    
    /**
     * Checks if this engine supports the given rule type.
     * 
     * @param ruleType The rule type to check
     * @return true if supported, false otherwise
     */
    boolean supportsRuleType(RuleType ruleType);
    
    /**
     * Initializes the rule engine with configuration.
     * Called once during engine startup.
     * 
     * @param configuration Engine-specific configuration
     */
    void initialize(EngineConfiguration configuration);
    
    /**
     * Shuts down the rule engine and releases resources.
     * Called during engine shutdown.
     */
    void shutdown();
}