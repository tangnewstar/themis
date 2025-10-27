package com.themis.core.factor;

import java.util.concurrent.TimeUnit;

/**
 * 缓存策略配置
 * 定义因子值的缓存行为
 */
public class CacheStrategy {
    
    /**
     * 缓存策略类型
     */
    private CacheType cacheType;
    
    /**
     * 缓存TTL（生存时间）
     */
    private long ttl;
    
    /**
     * 时间单位
     */
    private TimeUnit timeUnit;
    
    /**
     * 更新机制
     */
    private UpdateMechanism updateMechanism;
    
    /**
     * 是否启用本地缓存
     */
    private boolean enableLocalCache = true;
    
    /**
     * 是否启用分布式缓存
     */
    private boolean enableDistributedCache = true;
    
    /**
     * 缓存键前缀
     */
    private String keyPrefix;
    
    public CacheStrategy() {
    }
    
    public CacheStrategy(CacheType cacheType, long ttl, TimeUnit timeUnit) {
        this.cacheType = cacheType;
        this.ttl = ttl;
        this.timeUnit = timeUnit;
    }
    
    /**
     * 创建短时缓存策略（1-5分钟）
     */
    public static CacheStrategy shortTerm(long minutes) {
        return new CacheStrategy(CacheType.SHORT_TERM, minutes, TimeUnit.MINUTES);
    }
    
    /**
     * 创建中时缓存策略（1-24小时）
     */
    public static CacheStrategy mediumTerm(long hours) {
        return new CacheStrategy(CacheType.MEDIUM_TERM, hours, TimeUnit.HOURS);
    }
    
    /**
     * 创建长时缓存策略（1-7天）
     */
    public static CacheStrategy longTerm(long days) {
        return new CacheStrategy(CacheType.LONG_TERM, days, TimeUnit.DAYS);
    }
    
    /**
     * 创建无缓存策略
     */
    public static CacheStrategy noCache() {
        return new CacheStrategy(CacheType.NO_CACHE, 0, TimeUnit.SECONDS);
    }
    
    // Getters and Setters
    
    public CacheType getCacheType() {
        return cacheType;
    }
    
    public void setCacheType(CacheType cacheType) {
        this.cacheType = cacheType;
    }
    
    public long getTtl() {
        return ttl;
    }
    
    public void setTtl(long ttl) {
        this.ttl = ttl;
    }
    
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
    
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
    
    public UpdateMechanism getUpdateMechanism() {
        return updateMechanism;
    }
    
    public void setUpdateMechanism(UpdateMechanism updateMechanism) {
        this.updateMechanism = updateMechanism;
    }
    
    public boolean isEnableLocalCache() {
        return enableLocalCache;
    }
    
    public void setEnableLocalCache(boolean enableLocalCache) {
        this.enableLocalCache = enableLocalCache;
    }
    
    public boolean isEnableDistributedCache() {
        return enableDistributedCache;
    }
    
    public void setEnableDistributedCache(boolean enableDistributedCache) {
        this.enableDistributedCache = enableDistributedCache;
    }
    
    public String getKeyPrefix() {
        return keyPrefix;
    }
    
    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }
    
    /**
     * 获取TTL（毫秒）
     */
    public long getTtlInMillis() {
        return timeUnit.toMillis(ttl);
    }
    
    /**
     * 是否启用缓存
     */
    public boolean isCacheEnabled() {
        return cacheType != CacheType.NO_CACHE;
    }
    
    /**
     * 缓存策略类型
     */
    public enum CacheType {
        /**
         * 短时缓存 - 1-5分钟，被动过期
         * 适用于：时间窗因子
         */
        SHORT_TERM("short_term", "短时缓存"),
        
        /**
         * 中时缓存 - 1-24小时，定时刷新
         * 适用于：个体画像
         */
        MEDIUM_TERM("medium_term", "中时缓存"),
        
        /**
         * 长时缓存 - 1-7天，事件触发更新
         * 适用于：全局统计
         */
        LONG_TERM("long_term", "长时缓存"),
        
        /**
         * 无缓存 - 实时计算
         * 适用于：强实时因子
         */
        NO_CACHE("no_cache", "无缓存");
        
        private final String code;
        private final String description;
        
        CacheType(String code, String description) {
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
    
    /**
     * 更新机制
     */
    public enum UpdateMechanism {
        /**
         * 被动过期 - TTL到期后自动失效
         */
        PASSIVE_EXPIRE("passive_expire", "被动过期"),
        
        /**
         * 定时刷新 - 定时主动刷新缓存
         */
        SCHEDULED_REFRESH("scheduled_refresh", "定时刷新"),
        
        /**
         * 事件触发更新 - 相关事件发生时更新
         */
        EVENT_DRIVEN("event_driven", "事件触发更新");
        
        private final String code;
        private final String description;
        
        UpdateMechanism(String code, String description) {
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
        return String.format("CacheStrategy{type=%s, ttl=%d %s, update=%s}", 
                           cacheType, ttl, timeUnit, updateMechanism);
    }
}
