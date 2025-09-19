package com.themis.core.engine;

import java.util.Map;

/**
 * Metrics collected during rule execution.
 * Provides performance and operational insights.
 */
public class ExecutionMetrics {
    
    private final long executionTimeMillis;
    private final long compilationTimeMillis;
    private final long evaluationTimeMillis;
    private final long actionTimeMillis;
    private final int conditionsEvaluated;
    private final int actionsExecuted;
    private final long memoryUsedBytes;
    private final int cacheHits;
    private final int cacheMisses;
    private final Map<String, Object> customMetrics;
    
    private ExecutionMetrics(Builder builder) {
        this.executionTimeMillis = builder.executionTimeMillis;
        this.compilationTimeMillis = builder.compilationTimeMillis;
        this.evaluationTimeMillis = builder.evaluationTimeMillis;
        this.actionTimeMillis = builder.actionTimeMillis;
        this.conditionsEvaluated = builder.conditionsEvaluated;
        this.actionsExecuted = builder.actionsExecuted;
        this.memoryUsedBytes = builder.memoryUsedBytes;
        this.cacheHits = builder.cacheHits;
        this.cacheMisses = builder.cacheMisses;
        this.customMetrics = builder.customMetrics;
    }
    
    // Getters
    public long getExecutionTimeMillis() { return executionTimeMillis; }
    public long getCompilationTimeMillis() { return compilationTimeMillis; }
    public long getEvaluationTimeMillis() { return evaluationTimeMillis; }
    public long getActionTimeMillis() { return actionTimeMillis; }
    public int getConditionsEvaluated() { return conditionsEvaluated; }
    public int getActionsExecuted() { return actionsExecuted; }
    public long getMemoryUsedBytes() { return memoryUsedBytes; }
    public int getCacheHits() { return cacheHits; }
    public int getCacheMisses() { return cacheMisses; }
    public Map<String, Object> getCustomMetrics() { return customMetrics; }
    
    /**
     * Gets the cache hit ratio as a percentage.
     */
    public double getCacheHitRatio() {
        int total = cacheHits + cacheMisses;
        return total > 0 ? (double) cacheHits / total * 100.0 : 0.0;
    }
    
    /**
     * Gets the total cache operations.
     */
    public int getTotalCacheOperations() {
        return cacheHits + cacheMisses;
    }
    
    /**
     * Gets a custom metric value.
     */
    public Object getCustomMetric(String key) {
        return customMetrics.get(key);
    }
    
    /**
     * Gets a custom metric value with type casting.
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomMetric(String key, Class<T> type) {
        Object value = customMetrics.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    @Override
    public String toString() {
        return String.format("ExecutionMetrics{executionTime=%dms, conditions=%d, actions=%d, cacheHitRatio=%.1f%%}", 
                           executionTimeMillis, conditionsEvaluated, actionsExecuted, getCacheHitRatio());
    }
    
    /**
     * Builder class for ExecutionMetrics.
     */
    public static class Builder {
        private long executionTimeMillis;
        private long compilationTimeMillis;
        private long evaluationTimeMillis;
        private long actionTimeMillis;
        private int conditionsEvaluated;
        private int actionsExecuted;
        private long memoryUsedBytes;
        private int cacheHits;
        private int cacheMisses;
        private Map<String, Object> customMetrics = new java.util.HashMap<>();
        
        public Builder executionTimeMillis(long executionTimeMillis) {
            this.executionTimeMillis = executionTimeMillis;
            return this;
        }
        
        public Builder compilationTimeMillis(long compilationTimeMillis) {
            this.compilationTimeMillis = compilationTimeMillis;
            return this;
        }
        
        public Builder evaluationTimeMillis(long evaluationTimeMillis) {
            this.evaluationTimeMillis = evaluationTimeMillis;
            return this;
        }
        
        public Builder actionTimeMillis(long actionTimeMillis) {
            this.actionTimeMillis = actionTimeMillis;
            return this;
        }
        
        public Builder conditionsEvaluated(int conditionsEvaluated) {
            this.conditionsEvaluated = conditionsEvaluated;
            return this;
        }
        
        public Builder actionsExecuted(int actionsExecuted) {
            this.actionsExecuted = actionsExecuted;
            return this;
        }
        
        public Builder memoryUsedBytes(long memoryUsedBytes) {
            this.memoryUsedBytes = memoryUsedBytes;
            return this;
        }
        
        public Builder cacheHits(int cacheHits) {
            this.cacheHits = cacheHits;
            return this;
        }
        
        public Builder cacheMisses(int cacheMisses) {
            this.cacheMisses = cacheMisses;
            return this;
        }
        
        public Builder customMetrics(Map<String, Object> customMetrics) {
            this.customMetrics = customMetrics;
            return this;
        }
        
        public Builder customMetric(String key, Object value) {
            this.customMetrics.put(key, value);
            return this;
        }
        
        public ExecutionMetrics build() {
            return new ExecutionMetrics(this);
        }
    }
}