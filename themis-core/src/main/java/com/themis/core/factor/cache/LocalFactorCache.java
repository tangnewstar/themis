package com.themis.core.factor.cache;

import com.themis.core.factor.FactorResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 本地内存因子缓存实现
 * 基于ConcurrentHashMap实现的本地缓存
 */
public class LocalFactorCache implements FactorCache {
    
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong putCount = new AtomicLong(0);
    private final AtomicLong evictionCount = new AtomicLong(0);
    
    private final int maxSize;
    
    public LocalFactorCache() {
        this(10000); // 默认最大缓存10000条
    }
    
    public LocalFactorCache(int maxSize) {
        this.maxSize = maxSize;
    }
    
    @Override
    public FactorResult get(String factorId, String cacheKey) {
        String fullKey = buildKey(factorId, cacheKey);
        CacheEntry entry = cache.get(fullKey);
        
        if (entry == null || entry.isExpired()) {
            missCount.incrementAndGet();
            if (entry != null) {
                cache.remove(fullKey);
                evictionCount.incrementAndGet();
            }
            return null;
        }
        
        hitCount.incrementAndGet();
        return entry.getResult();
    }
    
    @Override
    public void put(String factorId, String cacheKey, FactorResult result, long ttlMillis) {
        if (cache.size() >= maxSize) {
            evictOldest();
        }
        
        String fullKey = buildKey(factorId, cacheKey);
        long expirationTime = ttlMillis > 0 ? System.currentTimeMillis() + ttlMillis : Long.MAX_VALUE;
        
        cache.put(fullKey, new CacheEntry(result, expirationTime));
        putCount.incrementAndGet();
    }
    
    @Override
    public void evict(String factorId, String cacheKey) {
        String fullKey = buildKey(factorId, cacheKey);
        if (cache.remove(fullKey) != null) {
            evictionCount.incrementAndGet();
        }
    }
    
    @Override
    public void evictAll(String factorId) {
        String prefix = factorId + ":";
        cache.keySet().removeIf(key -> {
            if (key.startsWith(prefix)) {
                evictionCount.incrementAndGet();
                return true;
            }
            return false;
        });
    }
    
    @Override
    public void clear() {
        long count = cache.size();
        cache.clear();
        evictionCount.addAndGet(count);
    }
    
    @Override
    public boolean exists(String factorId, String cacheKey) {
        String fullKey = buildKey(factorId, cacheKey);
        CacheEntry entry = cache.get(fullKey);
        return entry != null && !entry.isExpired();
    }
    
    @Override
    public CacheStats getStats() {
        return new CacheStats(
            hitCount.get(),
            missCount.get(),
            putCount.get(),
            evictionCount.get(),
            cache.size()
        );
    }
    
    /**
     * 构建完整缓存键
     */
    private String buildKey(String factorId, String cacheKey) {
        return factorId + ":" + cacheKey;
    }
    
    /**
     * 淘汰最老的缓存项
     */
    private void evictOldest() {
        // 简单实现：移除第一个元素
        // 生产环境可使用LRU等更高效的淘汰策略
        if (!cache.isEmpty()) {
            String firstKey = cache.keySet().iterator().next();
            cache.remove(firstKey);
            evictionCount.incrementAndGet();
        }
    }
    
    /**
     * 清理过期缓存（可以定期调用）
     */
    public void cleanupExpired() {
        cache.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                evictionCount.incrementAndGet();
                return true;
            }
            return false;
        });
    }
    
    /**
     * 缓存条目
     */
    private static class CacheEntry {
        private final FactorResult result;
        private final long expirationTime;
        
        public CacheEntry(FactorResult result, long expirationTime) {
            this.result = result;
            this.expirationTime = expirationTime;
        }
        
        public FactorResult getResult() {
            return result;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
}
