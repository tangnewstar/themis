package com.themis.core.engine;

import com.themis.core.model.RuleType;

import java.util.Set;

/**
 * Describes the capabilities of a rule engine implementation.
 * Used to determine which features and rule types are supported.
 */
public class EngineCapabilities {
    
    private final String engineName;
    private final String engineVersion;
    private final Set<RuleType> supportedRuleTypes;
    private final Set<ExecutionFeature> supportedFeatures;
    private final boolean supportsParallelExecution;
    private final boolean supportsBatchExecution;
    private final boolean supportsTransactions;
    private final boolean supportsDebugging;
    private final boolean supportsMetrics;
    private final int maxRulesPerBatch;
    private final long maxExecutionTimeMillis;
    
    private EngineCapabilities(Builder builder) {
        this.engineName = builder.engineName;
        this.engineVersion = builder.engineVersion;
        this.supportedRuleTypes = builder.supportedRuleTypes;
        this.supportedFeatures = builder.supportedFeatures;
        this.supportsParallelExecution = builder.supportsParallelExecution;
        this.supportsBatchExecution = builder.supportsBatchExecution;
        this.supportsTransactions = builder.supportsTransactions;
        this.supportsDebugging = builder.supportsDebugging;
        this.supportsMetrics = builder.supportsMetrics;
        this.maxRulesPerBatch = builder.maxRulesPerBatch;
        this.maxExecutionTimeMillis = builder.maxExecutionTimeMillis;
    }
    
    // Getters
    public String getEngineName() { return engineName; }
    public String getEngineVersion() { return engineVersion; }
    public Set<RuleType> getSupportedRuleTypes() { return supportedRuleTypes; }
    public Set<ExecutionFeature> getSupportedFeatures() { return supportedFeatures; }
    public boolean supportsParallelExecution() { return supportsParallelExecution; }
    public boolean supportsBatchExecution() { return supportsBatchExecution; }
    public boolean supportsTransactions() { return supportsTransactions; }
    public boolean supportsDebugging() { return supportsDebugging; }
    public boolean supportsMetrics() { return supportsMetrics; }
    public int getMaxRulesPerBatch() { return maxRulesPerBatch; }
    public long getMaxExecutionTimeMillis() { return maxExecutionTimeMillis; }
    
    /**
     * Checks if the engine supports a specific rule type.
     */
    public boolean supportsRuleType(RuleType ruleType) {
        return supportedRuleTypes.contains(ruleType);
    }
    
    /**
     * Checks if the engine supports a specific execution feature.
     */
    public boolean supportsFeature(ExecutionFeature feature) {
        return supportedFeatures.contains(feature);
    }
    
    @Override
    public String toString() {
        return String.format("EngineCapabilities{name='%s', version='%s', ruleTypes=%s}", 
                           engineName, engineVersion, supportedRuleTypes);
    }
    
    /**
     * Enumeration of execution features that engines may support.
     */
    public enum ExecutionFeature {
        CONDITIONAL_LOGIC,
        FORMULA_EVALUATION,
        TABLE_LOOKUP,
        SCORECARD_CALCULATION,
        WORKFLOW_ORCHESTRATION,
        RULE_CHAINING,
        FACT_INFERENCE,
        PATTERN_MATCHING,
        TEMPORAL_REASONING,
        FUZZY_LOGIC,
        MACHINE_LEARNING_INTEGRATION,
        CUSTOM_FUNCTIONS,
        DYNAMIC_RULE_MODIFICATION,
        RULE_VERSIONING,
        ROLLBACK_SUPPORT
    }
    
    /**
     * Builder class for EngineCapabilities.
     */
    public static class Builder {
        private String engineName;
        private String engineVersion;
        private Set<RuleType> supportedRuleTypes;
        private Set<ExecutionFeature> supportedFeatures;
        private boolean supportsParallelExecution;
        private boolean supportsBatchExecution;
        private boolean supportsTransactions;
        private boolean supportsDebugging;
        private boolean supportsMetrics;
        private int maxRulesPerBatch = Integer.MAX_VALUE;
        private long maxExecutionTimeMillis = Long.MAX_VALUE;
        
        public Builder engineName(String engineName) {
            this.engineName = engineName;
            return this;
        }
        
        public Builder engineVersion(String engineVersion) {
            this.engineVersion = engineVersion;
            return this;
        }
        
        public Builder supportedRuleTypes(Set<RuleType> supportedRuleTypes) {
            this.supportedRuleTypes = supportedRuleTypes;
            return this;
        }
        
        public Builder supportedFeatures(Set<ExecutionFeature> supportedFeatures) {
            this.supportedFeatures = supportedFeatures;
            return this;
        }
        
        public Builder supportsParallelExecution(boolean supportsParallelExecution) {
            this.supportsParallelExecution = supportsParallelExecution;
            return this;
        }
        
        public Builder supportsBatchExecution(boolean supportsBatchExecution) {
            this.supportsBatchExecution = supportsBatchExecution;
            return this;
        }
        
        public Builder supportsTransactions(boolean supportsTransactions) {
            this.supportsTransactions = supportsTransactions;
            return this;
        }
        
        public Builder supportsDebugging(boolean supportsDebugging) {
            this.supportsDebugging = supportsDebugging;
            return this;
        }
        
        public Builder supportsMetrics(boolean supportsMetrics) {
            this.supportsMetrics = supportsMetrics;
            return this;
        }
        
        public Builder maxRulesPerBatch(int maxRulesPerBatch) {
            this.maxRulesPerBatch = maxRulesPerBatch;
            return this;
        }
        
        public Builder maxExecutionTimeMillis(long maxExecutionTimeMillis) {
            this.maxExecutionTimeMillis = maxExecutionTimeMillis;
            return this;
        }
        
        public EngineCapabilities build() {
            return new EngineCapabilities(this);
        }
    }
}