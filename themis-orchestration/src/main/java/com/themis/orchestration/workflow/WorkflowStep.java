package com.themis.orchestration.workflow;

import com.themis.core.model.RuleDefinition;
import java.util.List;
import java.util.Map;

/**
 * Represents a single step in a workflow.
 */
public class WorkflowStep {
    
    private String stepId;
    private String stepName;
    private StepType stepType;
    private String ruleId;
    private RuleDefinition ruleDefinition;
    private List<String> dependencies;
    private Map<String, Object> inputMappings;
    private Map<String, Object> outputMappings;
    private StepCondition condition;
    private boolean parallel = false;
    private int retryAttempts = 0;
    private long timeoutMillis = 30000;
    
    // Additional fields needed by implementation
    private Map<String, Object> configuration;
    private String outputVariable;
    private boolean failOnError = true;
    private StepType type; // alias for stepType
    
    // Getters and Setters
    public String getStepId() { return stepId; }
    public void setStepId(String stepId) { this.stepId = stepId; }
    
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    
    public StepType getStepType() { return stepType; }
    public void setStepType(StepType stepType) { this.stepType = stepType; }
    
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    
    public RuleDefinition getRuleDefinition() { return ruleDefinition; }
    public void setRuleDefinition(RuleDefinition ruleDefinition) { this.ruleDefinition = ruleDefinition; }
    
    public List<String> getDependencies() { return dependencies; }
    public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }
    
    public Map<String, Object> getInputMappings() { return inputMappings; }
    public void setInputMappings(Map<String, Object> inputMappings) { this.inputMappings = inputMappings; }
    
    public Map<String, Object> getOutputMappings() { return outputMappings; }
    public void setOutputMappings(Map<String, Object> outputMappings) { this.outputMappings = outputMappings; }
    
    public StepCondition getCondition() { return condition; }
    public void setCondition(StepCondition condition) { this.condition = condition; }
    
    /**
     * Convenience method to set condition from string.
     */
    public void setCondition(String conditionExpression) {
        if (conditionExpression != null) {
            StepCondition cond = new StepCondition();
            cond.setExpression(conditionExpression);
            this.condition = cond;
        }
    }
    
    public boolean isParallel() { return parallel; }
    public void setParallel(boolean parallel) { this.parallel = parallel; }
    
    public int getRetryAttempts() { return retryAttempts; }
    public void setRetryAttempts(int retryAttempts) { this.retryAttempts = retryAttempts; }
    
    public long getTimeoutMillis() { return timeoutMillis; }
    public void setTimeoutMillis(long timeoutMillis) { this.timeoutMillis = timeoutMillis; }
    
    // Additional getters/setters
    public Map<String, Object> getConfiguration() { return configuration; }
    public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }
    
    public String getOutputVariable() { return outputVariable; }
    public void setOutputVariable(String outputVariable) { this.outputVariable = outputVariable; }
    
    public boolean isFailOnError() { return failOnError; }
    public void setFailOnError(boolean failOnError) { this.failOnError = failOnError; }
    
    // Aliases
    public String getId() { return stepId; }
    public String getName() { return stepName; }
    public StepType getType() { return stepType; }
    
    public void setId(String id) { this.stepId = id; }
    public void setName(String name) { this.stepName = name; }
    public void setType(StepType type) { this.stepType = type; }
    public void setDescription(String description) {
        // Add description field if needed - for now just log it
    }
    
    /**
     * Step types enumeration.
     */
    public enum StepType {
        RULE_EXECUTION("Rule Execution", "Execute a rule"),
        DECISION_GATEWAY("Decision Gateway", "Conditional branching"),
        PARALLEL_GATEWAY("Parallel Gateway", "Parallel execution"),
        MERGE_GATEWAY("Merge Gateway", "Merge parallel branches"),
        SCRIPT_TASK("Script Task", "Execute custom script"),
        SERVICE_TASK("Service Task", "Call external service"),
        HUMAN_TASK("Human Task", "Manual task requiring human input"),
        
        // Additional types used in implementation
        DECISION("Decision", "Decision step"),
        PARALLEL("Parallel", "Parallel execution step"),
        SEQUENTIAL("Sequential", "Sequential execution step"),
        CUSTOM("Custom", "Custom step execution");
        
        private final String displayName;
        private final String description;
        
        StepType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Condition for step execution.
     */
    public static class StepCondition {
        private String expression;
        private Map<String, Object> parameters;
        
        public String getExpression() { return expression; }
        public void setExpression(String expression) { this.expression = expression; }
        
        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    }
}