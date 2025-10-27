package com.themis.core.factor.cache;

import com.themis.core.factor.FactorResult;

/**
 * 因子缓存接口
 * 定义因子值的缓存操作
 */
public interface FactorCache {
    
    /**
     * 获取因子缓存值
     * 
     * @param factorId 因子ID
     * @param cacheKey 缓存键（通常包含上下文参数）
     * @return 缓存的因子结果，如果不存在返回null
     */
    FactorResult get(String factorId, String cacheKey);
    
    /**
     * 设置因子缓存值
     * 
     * @param factorId 因子ID
     * @param cacheKey 缓存键
     * @param result 因子计算结果
     * @param ttlMillis 缓存TTL（毫秒）
     */
    void put(String factorId, String cacheKey, FactorResult result, long ttlMillis);
    
    /**
     * 删除因子缓存
     * 
     * @param factorId 因子ID
     * @param cacheKey 缓存键
     */
    void evict(String factorId, String cacheKey);
    
    /**
     * 删除因子的所有缓存
     * 
     * @param factorId 因子ID
     */
    void evictAll(String factorId);
    
    /**
     * 清空所有缓存
     */
    void clear();
    
    /**
     * 检查缓存是否存在
     * 
     * @param factorId 因子ID
     * @param cacheKey 缓存键
     * @return 如果存在返回true
     */
    boolean exists(String factorId, String cacheKey);
    
    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计
     */
    CacheStats getStats();
    
    /**
     * 缓存统计信息
     */
    class CacheStats {
        private long hitCount;
        private long missCount;
        private long putCount;
        private long evictionCount;
        private long size;
        
        public CacheStats() {
        }
        
        public CacheStats(long hitCount, long missCount, long putCount, long evictionCount, long size) {
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.putCount = putCount;
            this.evictionCount = evictionCount;
            this.size = size;
        }
        
        public double getHitRate() {
            long total = hitCount + missCount;
            return total == 0 ? 0.0 : (double) hitCount / total;
        }
        
        // Getters and Setters
        
        public long getHitCount() {
            return hitCount;
        }
        
        public void setHitCount(long hitCount) {
            this.hitCount = hitCount;
        }
        
        public long getMissCount() {
            return missCount;
        }
        
        public void setMissCount(long missCount) {
            this.missCount = missCount;
        }
        
        public long getPutCount() {
            return putCount;
        }
        
        public void setPutCount(long putCount) {
            this.putCount = putCount;
        }
        
        public long getEvictionCount() {
            return evictionCount;
        }
        
        public void setEvictionCount(long evictionCount) {
            this.evictionCount = evictionCount;
        }
        
        public long getSize() {
            return size;
        }
        
        public void setSize(long size) {
            this.size = size;
        }
        
        @Override
        public String toString() {
            return String.format("CacheStats{hits=%d, misses=%d, hitRate=%.2f%%, size=%d}", 
                               hitCount, missCount, getHitRate() * 100, size);
        }
    }
}
