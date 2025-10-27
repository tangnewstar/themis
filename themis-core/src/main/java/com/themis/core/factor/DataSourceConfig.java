package com.themis.core.factor;

import java.util.Map;

/**
 * 数据源配置
 * 定义因子数据的来源
 */
public class DataSourceConfig {
    
    /**
     * 数据源类型
     */
    private DataSourceType sourceType;
    
    /**
     * 数据源名称/标识
     */
    private String sourceName;
    
    /**
     * 查询语句或API路径
     */
    private String query;
    
    /**
     * 连接配置
     */
    private Map<String, Object> connectionConfig;
    
    /**
     * 超时时间（毫秒）
     */
    private long timeout = 5000;
    
    /**
     * 是否启用重试
     */
    private boolean retryEnabled = true;
    
    /**
     * 重试次数
     */
    private int maxRetries = 3;
    
    public DataSourceConfig() {
    }
    
    public DataSourceConfig(DataSourceType sourceType, String sourceName) {
        this.sourceType = sourceType;
        this.sourceName = sourceName;
    }
    
    // Getters and Setters
    
    public DataSourceType getSourceType() {
        return sourceType;
    }
    
    public void setSourceType(DataSourceType sourceType) {
        this.sourceType = sourceType;
    }
    
    public String getSourceName() {
        return sourceName;
    }
    
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public Map<String, Object> getConnectionConfig() {
        return connectionConfig;
    }
    
    public void setConnectionConfig(Map<String, Object> connectionConfig) {
        this.connectionConfig = connectionConfig;
    }
    
    public long getTimeout() {
        return timeout;
    }
    
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    
    public boolean isRetryEnabled() {
        return retryEnabled;
    }
    
    public void setRetryEnabled(boolean retryEnabled) {
        this.retryEnabled = retryEnabled;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    /**
     * 数据源类型
     */
    public enum DataSourceType {
        /**
         * 数据库查询
         */
        DATABASE("database", "数据库"),
        
        /**
         * Redis缓存
         */
        REDIS("redis", "Redis缓存"),
        
        /**
         * HTTP API调用
         */
        HTTP_API("http_api", "HTTP API"),
        
        /**
         * RPC服务调用
         */
        RPC("rpc", "RPC服务"),
        
        /**
         * 消息队列
         */
        MESSAGE_QUEUE("mq", "消息队列"),
        
        /**
         * 文件系统
         */
        FILE("file", "文件系统"),
        
        /**
         * 内存数据
         */
        MEMORY("memory", "内存数据"),
        
        /**
         * 自定义数据源
         */
        CUSTOM("custom", "自定义数据源");
        
        private final String code;
        private final String description;
        
        DataSourceType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Override
    public String toString() {
        return String.format("DataSourceConfig{type=%s, name='%s'}", sourceType, sourceName);
    }
}
