package com.themis.core.function.optimization;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 函数结果缓存
 * 基于LRU策略的函数执行结果缓存
 */
public class FunctionResultCache {
    
    /**
     * 缓存容器
     */
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    
    /**
     * 缓存配置
     */
    private final CacheConfig config;
    
    public FunctionResultCache(CacheConfig config) {
        this.config = config;
        
        // 启动过期清理线程
        if (config.isEnableExpiration()) {
            startExpirationTask();
        }
    }
    
    /**
     * 获取缓存结果
     * 
     * @param cacheKey 缓存键
     * @return 缓存结果，如果不存在或已过期返回null
     */
    public Object get(String cacheKey) {
        CacheEntry entry = cache.get(cacheKey);
        
        if (entry == null) {
            return null;
        }
        
        // 检查是否过期
        if (isExpired(entry)) {
            cache.remove(cacheKey);
            return null;
        }
        
        // 更新访问时间（LRU）
        entry.lastAccessTime = System.currentTimeMillis();
        entry.accessCount++;
        
        return entry.value;
    }
    
    /**
     * 放入缓存
     * 
     * @param cacheKey 缓存键
     * @param value 缓存值
     */
    public void put(String cacheKey, Object value) {
        // 检查缓存大小
        if (cache.size() >= config.getMaxSize()) {
            evictLRU();
        }
        
        CacheEntry entry = new CacheEntry(value);
        cache.put(cacheKey, entry);
    }
    
    /**
     * 生成缓存键
     * 
     * @param functionName 函数名
     * @param args 参数
     * @return 缓存键
     */
    public static String generateCacheKey(String functionName, Object... args) {
        StringBuilder sb = new StringBuilder(functionName);
        sb.append(":");
        
        for (Object arg : args) {
            sb.append(arg != null ? arg.hashCode() : "null").append("_");
        }
        
        return sb.toString();
    }
    
    /**
     * 清除缓存
     */
    public void clear() {
        cache.clear();
    }
    
    /**
     * 删除指定缓存
     */
    public void remove(String cacheKey) {
        cache.remove(cacheKey);
    }
    
    /**
     * 获取缓存统计
     */
    public CacheStats getStats() {
        long totalAccessCount = cache.values().stream()
            .mapToLong(e -> e.accessCount)
            .sum();
        
        return new CacheStats(cache.size(), totalAccessCount);
    }
    
    /**
     * 检查是否过期
     */
    private boolean isExpired(CacheEntry entry) {
        if (!config.isEnableExpiration()) {
            return false;
        }
        
        long age = System.currentTimeMillis() - entry.createTime;
        return age > config.getTtlMillis();
    }
    
    /**
     * LRU驱逐
     */
    private void evictLRU() {
        String lruKey = null;
        long oldestAccessTime = Long.MAX_VALUE;
        
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().lastAccessTime < oldestAccessTime) {
                oldestAccessTime = entry.getValue().lastAccessTime;
                lruKey = entry.getKey();
            }
        }
        
        if (lruKey != null) {
            cache.remove(lruKey);
        }
    }
    
    /**
     * 启动过期清理任务
     */
    private void startExpirationTask() {
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(config.getCleanupIntervalMillis());
                    cleanupExpired();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        cleanupThread.setDaemon(true);
        cleanupThread.setName("FunctionCache-Cleanup");
        cleanupThread.start();
    }
    
    /**
     * 清理过期条目
     */
    private void cleanupExpired() {
        cache.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }
    
    /**
     * 缓存条目
     */
    private static class CacheEntry {
        final Object value;
        final long createTime;
        long lastAccessTime;
        long accessCount;
        
        CacheEntry(Object value) {
            this.value = value;
            this.createTime = System.currentTimeMillis();
            this.lastAccessTime = createTime;
            this.accessCount = 0;
        }
    }
    
    /**
     * 缓存配置
     */
    public static class CacheConfig {
        private int maxSize = 10000;
        private boolean enableExpiration = true;
        private long ttlMillis = TimeUnit.MINUTES.toMillis(30);
        private long cleanupIntervalMillis = TimeUnit.MINUTES.toMillis(5);
        
        public int getMaxSize() {
            return maxSize;
        }
        
        public CacheConfig setMaxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }
        
        public boolean isEnableExpiration() {
            return enableExpiration;
        }
        
        public CacheConfig setEnableExpiration(boolean enableExpiration) {
            this.enableExpiration = enableExpiration;
            return this;
        }
        
        public long getTtlMillis() {
            return ttlMillis;
        }
        
        public CacheConfig setTtlMillis(long ttlMillis) {
            this.ttlMillis = ttlMillis;
            return this;
        }
        
        public long getCleanupIntervalMillis() {
            return cleanupIntervalMillis;
        }
        
        public CacheConfig setCleanupIntervalMillis(long cleanupIntervalMillis) {
            this.cleanupIntervalMillis = cleanupIntervalMillis;
            return this;
        }
    }
    
    /**
     * 缓存统计
     */
    public static class CacheStats {
        private final int size;
        private final long totalAccessCount;
        
        public CacheStats(int size, long totalAccessCount) {
            this.size = size;
            this.totalAccessCount = totalAccessCount;
        }
        
        public int getSize() {
            return size;
        }
        
        public long getTotalAccessCount() {
            return totalAccessCount;
        }
    }
}
