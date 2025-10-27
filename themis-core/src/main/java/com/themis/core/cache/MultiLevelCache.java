package com.themis.core.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 多级缓存
 * L1: 本地缓存 (内存)
 * L2: 分布式缓存 (Redis)
 */
public class MultiLevelCache<K, V> {
    
    /**
     * L1缓存 (本地缓存)
     */
    private final LocalCache<K, V> l1Cache;
    
    /**
     * L2缓存 (分布式缓存)
     */
    private final DistributedCache<K, V> l2Cache;
    
    /**
     * 缓存配置
     */
    private final CacheConfig config;
    
    /**
     * 缓存统计
     */
    private final CacheStats stats = new CacheStats();
    
    public MultiLevelCache(CacheConfig config) {
        this.config = config;
        this.l1Cache = new LocalCache<>(config.getL1MaxSize(), config.getL1TtlSeconds());
        this.l2Cache = new DistributedCache<>(config.getL2TtlSeconds());
    }
    
    /**
     * 获取缓存值
     */
    public V get(K key) {
        // 先查询L1缓存
        V value = l1Cache.get(key);
        if (value != null) {
            stats.recordL1Hit();
            return value;
        }
        
        // 查询L2缓存
        value = l2Cache.get(key);
        if (value != null) {
            stats.recordL2Hit();
            
            // 回填L1缓存
            if (config.isEnableL1Backfill()) {
                l1Cache.put(key, value);
            }
            
            return value;
        }
        
        stats.recordMiss();
        return null;
    }
    
    /**
     * 放入缓存
     */
    public void put(K key, V value) {
        // 写入L1缓存
        if (config.isEnableL1()) {
            l1Cache.put(key, value);
        }
        
        // 写入L2缓存
        if (config.isEnableL2()) {
            l2Cache.put(key, value);
        }
    }
    
    /**
     * 带TTL的放入缓存
     */
    public void put(K key, V value, long ttl, TimeUnit unit) {
        long ttlSeconds = unit.toSeconds(ttl);
        
        if (config.isEnableL1()) {
            l1Cache.put(key, value, ttlSeconds);
        }
        
        if (config.isEnableL2()) {
            l2Cache.put(key, value, ttlSeconds);
        }
    }
    
    /**
     * 删除缓存
     */
    public void remove(K key) {
        l1Cache.remove(key);
        l2Cache.remove(key);
    }
    
    /**
     * 清空缓存
     */
    public void clear() {
        l1Cache.clear();
        l2Cache.clear();
        stats.reset();
    }
    
    /**
     * 获取缓存统计
     */
    public CacheStats getStats() {
        return stats;
    }
    
    /**
     * 本地缓存实现
     */
    private static class LocalCache<K, V> {
        private final Map<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
        private final int maxSize;
        private final long defaultTtlSeconds;
        
        public LocalCache(int maxSize, long defaultTtlSeconds) {
            this.maxSize = maxSize;
            this.defaultTtlSeconds = defaultTtlSeconds;
        }
        
        public V get(K key) {
            CacheEntry<V> entry = cache.get(key);
            if (entry == null) {
                return null;
            }
            
            if (entry.isExpired()) {
                cache.remove(key);
                return null;
            }
            
            return entry.getValue();
        }
        
        public void put(K key, V value) {
            put(key, value, defaultTtlSeconds);
        }
        
        public void put(K key, V value, long ttlSeconds) {
            // 检查缓存大小
            if (cache.size() >= maxSize) {
                evictOne();
            }
            
            long expireTime = System.currentTimeMillis() + ttlSeconds * 1000;
            cache.put(key, new CacheEntry<>(value, expireTime));
        }
        
        public void remove(K key) {
            cache.remove(key);
        }
        
        public void clear() {
            cache.clear();
        }
        
        private void evictOne() {
            // 简单LRU: 删除第一个过期的条目,如果没有则删除第一个
            K keyToRemove = cache.entrySet().stream()
                .filter(e -> e.getValue().isExpired())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(cache.keySet().iterator().next());
            
            cache.remove(keyToRemove);
        }
    }
    
    /**
     * 分布式缓存实现 (Redis模拟)
     */
    private static class DistributedCache<K, V> {
        private final Map<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
        private final long defaultTtlSeconds;
        
        public DistributedCache(long defaultTtlSeconds) {
            this.defaultTtlSeconds = defaultTtlSeconds;
        }
        
        public V get(K key) {
            CacheEntry<V> entry = cache.get(key);
            if (entry == null) {
                return null;
            }
            
            if (entry.isExpired()) {
                cache.remove(key);
                return null;
            }
            
            return entry.getValue();
        }
        
        public void put(K key, V value) {
            put(key, value, defaultTtlSeconds);
        }
        
        public void put(K key, V value, long ttlSeconds) {
            long expireTime = System.currentTimeMillis() + ttlSeconds * 1000;
            cache.put(key, new CacheEntry<>(value, expireTime));
        }
        
        public void remove(K key) {
            cache.remove(key);
        }
        
        public void clear() {
            cache.clear();
        }
    }
    
    /**
     * 缓存条目
     */
    private static class CacheEntry<V> {
        private final V value;
        private final long expireTime;
        
        public CacheEntry(V value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }
        
        public V getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
    
    /**
     * 缓存配置
     */
    public static class CacheConfig {
        private boolean enableL1 = true;
        private boolean enableL2 = true;
        private boolean enableL1Backfill = true;
        private int l1MaxSize = 10000;
        private long l1TtlSeconds = 300;  // 5分钟
        private long l2TtlSeconds = 3600; // 1小时
        
        public boolean isEnableL1() {
            return enableL1;
        }
        
        public CacheConfig setEnableL1(boolean enableL1) {
            this.enableL1 = enableL1;
            return this;
        }
        
        public boolean isEnableL2() {
            return enableL2;
        }
        
        public CacheConfig setEnableL2(boolean enableL2) {
            this.enableL2 = enableL2;
            return this;
        }
        
        public boolean isEnableL1Backfill() {
            return enableL1Backfill;
        }
        
        public CacheConfig setEnableL1Backfill(boolean enableL1Backfill) {
            this.enableL1Backfill = enableL1Backfill;
            return this;
        }
        
        public int getL1MaxSize() {
            return l1MaxSize;
        }
        
        public CacheConfig setL1MaxSize(int l1MaxSize) {
            this.l1MaxSize = l1MaxSize;
            return this;
        }
        
        public long getL1TtlSeconds() {
            return l1TtlSeconds;
        }
        
        public CacheConfig setL1TtlSeconds(long l1TtlSeconds) {
            this.l1TtlSeconds = l1TtlSeconds;
            return this;
        }
        
        public long getL2TtlSeconds() {
            return l2TtlSeconds;
        }
        
        public CacheConfig setL2TtlSeconds(long l2TtlSeconds) {
            this.l2TtlSeconds = l2TtlSeconds;
            return this;
        }
    }
    
    /**
     * 缓存统计
     */
    public static class CacheStats {
        private long l1Hits = 0;
        private long l2Hits = 0;
        private long misses = 0;
        
        public synchronized void recordL1Hit() {
            l1Hits++;
        }
        
        public synchronized void recordL2Hit() {
            l2Hits++;
        }
        
        public synchronized void recordMiss() {
            misses++;
        }
        
        public synchronized void reset() {
            l1Hits = 0;
            l2Hits = 0;
            misses = 0;
        }
        
        public long getTotalRequests() {
            return l1Hits + l2Hits + misses;
        }
        
        public double getL1HitRate() {
            long total = getTotalRequests();
            return total > 0 ? (double) l1Hits / total * 100 : 0;
        }
        
        public double getL2HitRate() {
            long total = getTotalRequests();
            return total > 0 ? (double) l2Hits / total * 100 : 0;
        }
        
        public double getTotalHitRate() {
            long total = getTotalRequests();
            return total > 0 ? (double) (l1Hits + l2Hits) / total * 100 : 0;
        }
        
        public long getL1Hits() {
            return l1Hits;
        }
        
        public long getL2Hits() {
            return l2Hits;
        }
        
        public long getMisses() {
            return misses;
        }
        
        @Override
        public String toString() {
            return String.format("CacheStats{L1=%.1f%%, L2=%.1f%%, Total=%.1f%%, Requests=%d}",
                getL1HitRate(), getL2HitRate(), getTotalHitRate(), getTotalRequests());
        }
    }
}
