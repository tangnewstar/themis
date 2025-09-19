package com.themis.metadata.storage;

import java.util.Map;
import java.util.Properties;

/**
 * Configuration class for metadata storage backends.
 */
public class StorageConfiguration {
    
    private final StorageType storageType;
    private final String connectionUrl;
    private final Properties properties;
    private final Map<String, Object> customSettings;
    private final boolean enableCaching;
    private final long cacheExpirationMillis;
    private final int maxConnections;
    private final long connectionTimeoutMillis;
    private final long operationTimeoutMillis;
    private final boolean enableEncryption;
    private final String encryptionKey;
    private final boolean enableCompression;
    private final boolean enableMetrics;
    
    private StorageConfiguration(Builder builder) {
        this.storageType = builder.storageType;
        this.connectionUrl = builder.connectionUrl;
        this.properties = builder.properties;
        this.customSettings = builder.customSettings;
        this.enableCaching = builder.enableCaching;
        this.cacheExpirationMillis = builder.cacheExpirationMillis;
        this.maxConnections = builder.maxConnections;
        this.connectionTimeoutMillis = builder.connectionTimeoutMillis;
        this.operationTimeoutMillis = builder.operationTimeoutMillis;
        this.enableEncryption = builder.enableEncryption;
        this.encryptionKey = builder.encryptionKey;
        this.enableCompression = builder.enableCompression;
        this.enableMetrics = builder.enableMetrics;
    }
    
    // Getters
    public StorageType getStorageType() { return storageType; }
    public String getConnectionUrl() { return connectionUrl; }
    public Properties getProperties() { return properties; }
    public Map<String, Object> getCustomSettings() { return customSettings; }
    public boolean isEnableCaching() { return enableCaching; }
    public long getCacheExpirationMillis() { return cacheExpirationMillis; }
    public int getMaxConnections() { return maxConnections; }
    public long getConnectionTimeoutMillis() { return connectionTimeoutMillis; }
    public long getOperationTimeoutMillis() { return operationTimeoutMillis; }
    public boolean isEnableEncryption() { return enableEncryption; }
    public String getEncryptionKey() { return encryptionKey; }
    public boolean isEnableCompression() { return enableCompression; }
    public boolean isEnableMetrics() { return enableMetrics; }
    
    /**
     * Gets a property value.
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
     * Storage type enumeration.
     */
    public enum StorageType {
        DATABASE("Database", "SQL/NoSQL database storage"),
        ETCD("Etcd", "Distributed key-value store"),
        REDIS("Redis", "In-memory data structure store"),
        FILE_SYSTEM("File System", "Local or distributed file system"),
        MEMORY("Memory", "In-memory storage (for testing)"),
        MONGODB("MongoDB", "Document-oriented database"),
        ELASTICSEARCH("Elasticsearch", "Search and analytics engine");
        
        private final String displayName;
        private final String description;
        
        StorageType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Builder for StorageConfiguration.
     */
    public static class Builder {
        private StorageType storageType;
        private String connectionUrl;
        private Properties properties = new Properties();
        private Map<String, Object> customSettings = new java.util.HashMap<>();
        private boolean enableCaching = true;
        private long cacheExpirationMillis = 300000; // 5 minutes
        private int maxConnections = 10;
        private long connectionTimeoutMillis = 5000; // 5 seconds
        private long operationTimeoutMillis = 30000; // 30 seconds
        private boolean enableEncryption = false;
        private String encryptionKey;
        private boolean enableCompression = false;
        private boolean enableMetrics = true;
        
        public Builder storageType(StorageType storageType) {
            this.storageType = storageType;
            return this;
        }
        
        public Builder connectionUrl(String connectionUrl) {
            this.connectionUrl = connectionUrl;
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
        
        public Builder enableCaching(boolean enableCaching) {
            this.enableCaching = enableCaching;
            return this;
        }
        
        public Builder cacheExpirationMillis(long cacheExpirationMillis) {
            this.cacheExpirationMillis = cacheExpirationMillis;
            return this;
        }
        
        public Builder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }
        
        public Builder connectionTimeoutMillis(long connectionTimeoutMillis) {
            this.connectionTimeoutMillis = connectionTimeoutMillis;
            return this;
        }
        
        public Builder operationTimeoutMillis(long operationTimeoutMillis) {
            this.operationTimeoutMillis = operationTimeoutMillis;
            return this;
        }
        
        public Builder enableEncryption(boolean enableEncryption) {
            this.enableEncryption = enableEncryption;
            return this;
        }
        
        public Builder encryptionKey(String encryptionKey) {
            this.encryptionKey = encryptionKey;
            return this;
        }
        
        public Builder enableCompression(boolean enableCompression) {
            this.enableCompression = enableCompression;
            return this;
        }
        
        public Builder enableMetrics(boolean enableMetrics) {
            this.enableMetrics = enableMetrics;
            return this;
        }
        
        public StorageConfiguration build() {
            return new StorageConfiguration(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("StorageConfiguration{type=%s, url='%s', caching=%s}", 
                           storageType, connectionUrl, enableCaching);
    }
}