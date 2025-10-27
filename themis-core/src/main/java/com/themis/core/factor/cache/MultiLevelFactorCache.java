package com.themis.core.factor.cache;

import com.themis.core.factor.FactorResult;

/**
 * 多级因子缓存实现
 * 组合本地缓存和分布式缓存，实现两级缓存策略
 */
public class MultiLevelFactorCache implements FactorCache {
    
    private final FactorCache localCache;
    private final FactorCache distributedCache;
    
    public MultiLevelFactorCache(FactorCache localCache, FactorCache distributedCache) {
        this.localCache = localCache;
        this.distributedCache = distributedCache;
    }
    
    @Override
    public FactorResult get(String factorId, String cacheKey) {
        // 先查本地缓存
        FactorResult result = localCache.get(factorId, cacheKey);
        if (result != null) {
            return result;
        }
        
        // 本地未命中，查分布式缓存
        if (distributedCache != null) {
            result = distributedCache.get(factorId, cacheKey);
            if (result != null) {
                // 回写本地缓存
                localCache.put(factorId, cacheKey, result, 300000); // 5分钟
                return result;
            }
        }
        
        return null;
    }
    
    @Override
    public void put(String factorId, String cacheKey, FactorResult result, long ttlMillis) {
        // 同时写入本地和分布式缓存
        localCache.put(factorId, cacheKey, result, ttlMillis);
        
        if (distributedCache != null) {
            distributedCache.put(factorId, cacheKey, result, ttlMillis);
        }
    }
    
    @Override
    public void evict(String factorId, String cacheKey) {
        localCache.evict(factorId, cacheKey);
        
        if (distributedCache != null) {
            distributedCache.evict(factorId, cacheKey);
        }
    }
    
    @Override
    public void evictAll(String factorId) {
        localCache.evictAll(factorId);
        
        if (distributedCache != null) {
            distributedCache.evictAll(factorId);
        }
    }
    
    @Override
    public void clear() {
        localCache.clear();
        
        if (distributedCache != null) {
            distributedCache.clear();
        }
    }
    
    @Override
    public boolean exists(String factorId, String cacheKey) {
        return localCache.exists(factorId, cacheKey) || 
               (distributedCache != null && distributedCache.exists(factorId, cacheKey));
    }
    
    @Override
    public CacheStats getStats() {
        CacheStats localStats = localCache.getStats();
        
        if (distributedCache == null) {
            return localStats;
        }
        
        CacheStats distStats = distributedCache.getStats();
        
        // 合并统计信息
        return new CacheStats(
            localStats.getHitCount() + distStats.getHitCount(),
            localStats.getMissCount() + distStats.getMissCount(),
            localStats.getPutCount() + distStats.getPutCount(),
            localStats.getEvictionCount() + distStats.getEvictionCount(),
            localStats.getSize() + distStats.getSize()
        );
    }
    
    /**
     * 获取本地缓存统计
     */
    public CacheStats getLocalStats() {
        return localCache.getStats();
    }
    
    /**
     * 获取分布式缓存统计
     */
    public CacheStats getDistributedStats() {
        return distributedCache != null ? distributedCache.getStats() : new CacheStats();
    }
}
