package com.themis.core.engine;

import java.util.Map;
import java.util.Properties;

/**
 * Configuration class for rule engines.
 * Contains engine-specific settings and parameters.
 */
public class EngineConfiguration {
    
    private final String engineType;
    private final Properties properties;
    private final Map<String, Object> customSettings;
    private final boolean enableMetrics;
    private final boolean enableDebugging;
    private final boolean enableCaching;
    private final int threadPoolSize;
    private final long executionTimeoutMillis;
    private final int maxBatchSize;
    private final String cachingStrategy;
    
    private EngineConfiguration(Builder builder) {
        this.engineType = builder.engineType;
        this.properties = builder.properties;
        this.customSettings = builder.customSettings;
        this.enableMetrics = builder.enableMetrics;
        this.enableDebugging = builder.enableDebugging;
        this.enableCaching = builder.enableCaching;
        this.threadPoolSize = builder.threadPoolSize;
        this.executionTimeoutMillis = builder.executionTimeoutMillis;
        this.maxBatchSize = builder.maxBatchSize;
        this.cachingStrategy = builder.cachingStrategy;
    }
    
    // Getters
    public String getEngineType() { return engineType; }
    public Properties getProperties() { return properties; }
    public Map<String, Object> getCustomSettings() { return customSettings; }
    public boolean isMetricsEnabled() { return enableMetrics; }
    public boolean isDebuggingEnabled() { return enableDebugging; }
    public boolean isCachingEnabled() { return enableCaching; }
    public int getThreadPoolSize() { return threadPoolSize; }
    public long getExecutionTimeoutMillis() { return executionTimeoutMillis; }
    public int getMaxBatchSize() { return maxBatchSize; }
    public String getCachingStrategy() { return cachingStrategy; }
    
    /**
     * Gets a property value as string.
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Gets a property value with default.
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
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
    
    @Override
    public String toString() {
        return String.format("EngineConfiguration{engineType='%s', metrics=%s, debugging=%s}", 
                           engineType, enableMetrics, enableDebugging);
    }
    
    /**
     * Builder class for EngineConfiguration.
     */
    public static class Builder {
        private String engineType;
        private Properties properties = new Properties();
        private Map<String, Object> customSettings = new java.util.HashMap<>();
        private boolean enableMetrics = true;
        private boolean enableDebugging = false;
        private boolean enableCaching = true;
        private int threadPoolSize = Runtime.getRuntime().availableProcessors();
        private long executionTimeoutMillis = 30000; // 30 seconds
        private int maxBatchSize = 1000;
        private String cachingStrategy = "LRU";
        
        public Builder engineType(String engineType) {
            this.engineType = engineType;
            return this;
        }
        
        public Builder properties(Properties properties) {
            this.properties = properties;
            return this;
        }
        
        public Builder property(String key, String value) {
            this.properties.setProperty(key, value);
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
        
        public Builder enableMetrics(boolean enableMetrics) {
            this.enableMetrics = enableMetrics;
            return this;
        }
        
        public Builder enableDebugging(boolean enableDebugging) {
            this.enableDebugging = enableDebugging;
            return this;
        }
        
        public Builder enableCaching(boolean enableCaching) {
            this.enableCaching = enableCaching;
            return this;
        }
        
        public Builder threadPoolSize(int threadPoolSize) {
            this.threadPoolSize = threadPoolSize;
            return this;
        }
        
        public Builder executionTimeoutMillis(long executionTimeoutMillis) {
            this.executionTimeoutMillis = executionTimeoutMillis;
            return this;
        }
        
        public Builder maxBatchSize(int maxBatchSize) {
            this.maxBatchSize = maxBatchSize;
            return this;
        }
        
        public Builder cachingStrategy(String cachingStrategy) {
            this.cachingStrategy = cachingStrategy;
            return this;
        }
        
        public EngineConfiguration build() {
            return new EngineConfiguration(this);
        }
    }
}