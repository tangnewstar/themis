package com.themis.core.engine;

import java.util.Map;

/**
 * Execution settings that control how rules are executed.
 */
public class ExecutionSettings {
    
    private final boolean stopOnFirstError;
    private final boolean collectMetrics;
    private final boolean enableTracing;
    private final boolean enableCaching;
    private final long timeoutMillis;
    private final int maxIterations;
    private final Map<String, Object> customSettings;
    
    private ExecutionSettings(Builder builder) {
        this.stopOnFirstError = builder.stopOnFirstError;
        this.collectMetrics = builder.collectMetrics;
        this.enableTracing = builder.enableTracing;
        this.enableCaching = builder.enableCaching;
        this.timeoutMillis = builder.timeoutMillis;
        this.maxIterations = builder.maxIterations;
        this.customSettings = builder.customSettings;
    }
    
    // Getters
    public boolean isStopOnFirstError() { return stopOnFirstError; }
    public boolean isCollectMetrics() { return collectMetrics; }
    public boolean isEnableTracing() { return enableTracing; }
    public boolean isEnableCaching() { return enableCaching; }
    public long getTimeoutMillis() { return timeoutMillis; }
    public int getMaxIterations() { return maxIterations; }
    public Map<String, Object> getCustomSettings() { return customSettings; }
    
    /**
     * Gets a custom setting value.
     */
    public Object getCustomSetting(String key) {
        return customSettings.get(key);
    }
    
    /**
     * Gets a custom setting value with type casting.
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomSetting(String key, Class<T> type) {
        Object value = customSettings.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Creates default execution settings.
     */
    public static ExecutionSettings defaultSettings() {
        return new Builder().build();
    }
    
    @Override
    public String toString() {
        return String.format("ExecutionSettings{stopOnFirstError=%s, collectMetrics=%s, timeoutMillis=%d}", 
                           stopOnFirstError, collectMetrics, timeoutMillis);
    }
    
    /**
     * Builder class for ExecutionSettings.
     */
    public static class Builder {
        private boolean stopOnFirstError = true;
        private boolean collectMetrics = true;
        private boolean enableTracing = false;
        private boolean enableCaching = true;
        private long timeoutMillis = 30000; // 30 seconds
        private int maxIterations = 1000;
        private Map<String, Object> customSettings = new java.util.HashMap<>();
        
        public Builder stopOnFirstError(boolean stopOnFirstError) {
            this.stopOnFirstError = stopOnFirstError;
            return this;
        }
        
        public Builder collectMetrics(boolean collectMetrics) {
            this.collectMetrics = collectMetrics;
            return this;
        }
        
        public Builder enableTracing(boolean enableTracing) {
            this.enableTracing = enableTracing;
            return this;
        }
        
        public Builder enableCaching(boolean enableCaching) {
            this.enableCaching = enableCaching;
            return this;
        }
        
        public Builder timeoutMillis(long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            return this;
        }
        
        public Builder maxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
            return this;
        }
        
        public Builder customSettings(Map<String, Object> customSettings) {
            this.customSettings = customSettings;
            return this;
        }
        
        public Builder customSetting(String key, Object value) {
            this.customSettings.put(key, value);
            return this;
        }
        
        public ExecutionSettings build() {
            return new ExecutionSettings(this);
        }
    }
}