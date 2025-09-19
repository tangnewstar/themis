package com.themis.core.engine;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents the result of executing multiple rules in batch.
 * Contains individual execution results and overall batch metrics.
 */
public class BatchExecutionResult {
    
    private final List<RuleExecutionResult> results;
    private final boolean overallSuccess;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final Duration totalExecutionTime;
    private final ExecutionMetrics aggregatedMetrics;
    private final Map<String, Object> batchOutputs;
    private final String batchId;
    
    private BatchExecutionResult(Builder builder) {
        this.results = builder.results;
        this.overallSuccess = builder.overallSuccess;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.totalExecutionTime = builder.totalExecutionTime;
        this.aggregatedMetrics = builder.aggregatedMetrics;
        this.batchOutputs = builder.batchOutputs;
        this.batchId = builder.batchId;
    }
    
    // Getters
    public List<RuleExecutionResult> getResults() { return results; }
    public boolean isOverallSuccess() { return overallSuccess; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Duration getTotalExecutionTime() { return totalExecutionTime; }
    public ExecutionMetrics getAggregatedMetrics() { return aggregatedMetrics; }
    public Map<String, Object> getBatchOutputs() { return batchOutputs; }
    public String getBatchId() { return batchId; }
    
    /**
     * Gets the number of successfully executed rules.
     */
    public int getSuccessCount() {
        return (int) results.stream().filter(RuleExecutionResult::isSuccess).count();
    }
    
    /**
     * Gets the number of failed rule executions.
     */
    public int getFailureCount() {
        return (int) results.stream().filter(RuleExecutionResult::isFailure).count();
    }
    
    /**
     * Gets the total number of rules executed.
     */
    public int getTotalCount() {
        return results.size();
    }
    
    /**
     * Gets the success rate as a percentage.
     */
    public double getSuccessRate() {
        if (results.isEmpty()) {
            return 100.0;
        }
        return (double) getSuccessCount() / getTotalCount() * 100.0;
    }
    
    /**
     * Gets results for rules that succeeded.
     */
    public List<RuleExecutionResult> getSuccessfulResults() {
        return results.stream()
                .filter(RuleExecutionResult::isSuccess)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Gets results for rules that failed.
     */
    public List<RuleExecutionResult> getFailedResults() {
        return results.stream()
                .filter(RuleExecutionResult::isFailure)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Gets the execution result for a specific rule.
     */
    public RuleExecutionResult getResultByRuleId(String ruleId) {
        return results.stream()
                .filter(result -> ruleId.equals(result.getRuleId()))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public String toString() {
        return String.format("BatchExecutionResult{totalRules=%d, successful=%d, failed=%d, totalTime=%s}", 
                           getTotalCount(), getSuccessCount(), getFailureCount(), totalExecutionTime);
    }
    
    /**
     * Builder class for BatchExecutionResult.
     */
    public static class Builder {
        private List<RuleExecutionResult> results;
        private boolean overallSuccess;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Duration totalExecutionTime;
        private ExecutionMetrics aggregatedMetrics;
        private Map<String, Object> batchOutputs;
        private String batchId;
        
        public Builder results(List<RuleExecutionResult> results) {
            this.results = results;
            return this;
        }
        
        public Builder overallSuccess(boolean overallSuccess) {
            this.overallSuccess = overallSuccess;
            return this;
        }
        
        public Builder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }
        
        public Builder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }
        
        public Builder totalExecutionTime(Duration totalExecutionTime) {
            this.totalExecutionTime = totalExecutionTime;
            return this;
        }
        
        public Builder aggregatedMetrics(ExecutionMetrics aggregatedMetrics) {
            this.aggregatedMetrics = aggregatedMetrics;
            return this;
        }
        
        public Builder batchOutputs(Map<String, Object> batchOutputs) {
            this.batchOutputs = batchOutputs;
            return this;
        }
        
        public Builder batchId(String batchId) {
            this.batchId = batchId;
            return this;
        }
        
        public BatchExecutionResult build() {
            return new BatchExecutionResult(this);
        }
    }
}