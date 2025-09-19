package com.themis.core.engine;

import com.themis.core.model.RuleDefinition;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents the result of executing a single rule.
 * Contains execution status, outputs, performance metrics, and error information.
 */
public class RuleExecutionResult {
    
    private final String ruleId;
    private final String ruleName;
    private final boolean success;
    private final Map<String, Object> outputs;
    private final String errorMessage;
    private final Exception exception;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final Duration executionTime;
    private final ExecutionMetrics metrics;
    private final Map<String, Object> debugInfo;
    
    private RuleExecutionResult(Builder builder) {
        this.ruleId = builder.ruleId;
        this.ruleName = builder.ruleName;
        this.success = builder.success;
        this.outputs = builder.outputs;
        this.errorMessage = builder.errorMessage;
        this.exception = builder.exception;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.executionTime = builder.executionTime;
        this.metrics = builder.metrics;
        this.debugInfo = builder.debugInfo;
    }
    
    // Getters
    public String getRuleId() { return ruleId; }
    public String getRuleName() { return ruleName; }
    public boolean isSuccess() { return success; }
    public boolean isFailure() { return !success; }
    public Map<String, Object> getOutputs() { return outputs; }
    public String getErrorMessage() { return errorMessage; }
    public Exception getException() { return exception; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Duration getExecutionTime() { return executionTime; }
    public ExecutionMetrics getMetrics() { return metrics; }
    public Map<String, Object> getDebugInfo() { return debugInfo; }
    
    /**
     * Gets a specific output value by key.
     */
    public Object getOutput(String key) {
        return outputs != null ? outputs.get(key) : null;
    }
    
    /**
     * Gets a specific output value with type casting.
     */
    @SuppressWarnings("unchecked")
    public <T> T getOutput(String key, Class<T> type) {
        Object value = getOutput(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Checks if the rule produced any outputs.
     */
    public boolean hasOutputs() {
        return outputs != null && !outputs.isEmpty();
    }
    
    /**
     * Gets the execution time in milliseconds.
     */
    public long getExecutionTimeMillis() {
        return executionTime != null ? executionTime.toMillis() : 0L;
    }
    
    @Override
    public String toString() {
        return String.format("RuleExecutionResult{ruleId='%s', success=%s, executionTime=%s}", 
                           ruleId, success, executionTime);
    }
    
    /**
     * Creates a successful execution result.
     */
    public static RuleExecutionResult success(RuleDefinition rule, Map<String, Object> outputs, 
                                            LocalDateTime startTime, LocalDateTime endTime) {
        return new Builder(rule.getRuleId(), rule.getRuleName())
                .success(true)
                .outputs(outputs)
                .startTime(startTime)
                .endTime(endTime)
                .executionTime(Duration.between(startTime, endTime))
                .build();
    }
    
    /**
     * Creates a failed execution result.
     */
    public static RuleExecutionResult failure(RuleDefinition rule, String errorMessage, 
                                            LocalDateTime startTime, LocalDateTime endTime) {
        return new Builder(rule.getRuleId(), rule.getRuleName())
                .success(false)
                .errorMessage(errorMessage)
                .startTime(startTime)
                .endTime(endTime)
                .executionTime(Duration.between(startTime, endTime))
                .build();
    }
    
    /**
     * Creates a failed execution result with exception.
     */
    public static RuleExecutionResult failure(RuleDefinition rule, Exception exception, 
                                            LocalDateTime startTime, LocalDateTime endTime) {
        return new Builder(rule.getRuleId(), rule.getRuleName())
                .success(false)
                .errorMessage(exception.getMessage())
                .exception(exception)
                .startTime(startTime)
                .endTime(endTime)
                .executionTime(Duration.between(startTime, endTime))
                .build();
    }
    
    /**
     * Builder class for RuleExecutionResult.
     */
    public static class Builder {
        private final String ruleId;
        private final String ruleName;
        private boolean success;
        private Map<String, Object> outputs;
        private String errorMessage;
        private Exception exception;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Duration executionTime;
        private ExecutionMetrics metrics;
        private Map<String, Object> debugInfo;
        
        public Builder(String ruleId, String ruleName) {
            this.ruleId = ruleId;
            this.ruleName = ruleName;
        }
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder outputs(Map<String, Object> outputs) {
            this.outputs = outputs;
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public Builder exception(Exception exception) {
            this.exception = exception;
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
        
        public Builder executionTime(Duration executionTime) {
            this.executionTime = executionTime;
            return this;
        }
        
        public Builder metrics(ExecutionMetrics metrics) {
            this.metrics = metrics;
            return this;
        }
        
        public Builder debugInfo(Map<String, Object> debugInfo) {
            this.debugInfo = debugInfo;
            return this;
        }
        
        public RuleExecutionResult build() {
            return new RuleExecutionResult(this);
        }
    }
}