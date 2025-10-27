package com.themis.core.factor.storage;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 因子数据分层架构
 * 支持实时、准实时、离线三层数据访问
 */
public class FactorDataLayer {
    
    /**
     * 实时数据层
     */
    private final RealtimeDataStore realtimeStore;
    
    /**
     * 准实时数据层
     */
    private final NearRealtimeDataStore nearRealtimeStore;
    
    /**
     * 离线数据层
     */
    private final OfflineDataStore offlineStore;
    
    /**
     * 数据访问策略
     */
    private volatile DataAccessStrategy strategy = DataAccessStrategy.CASCADING;
    
    public FactorDataLayer() {
        this.realtimeStore = new RealtimeDataStore();
        this.nearRealtimeStore = new NearRealtimeDataStore();
        this.offlineStore = new OfflineDataStore();
    }
    
    /**
     * 获取因子数据
     * 
     * @param factorId 因子ID
     * @param entityId 实体ID
     * @return 因子值
     */
    public Object getFactorData(String factorId, String entityId) {
        switch (strategy) {
            case CASCADING:
                return getCascading(factorId, entityId);
            case REALTIME_ONLY:
                return realtimeStore.get(factorId, entityId);
            case NEAR_REALTIME_ONLY:
                return nearRealtimeStore.get(factorId, entityId);
            case OFFLINE_ONLY:
                return offlineStore.get(factorId, entityId);
            default:
                return getCascading(factorId, entityId);
        }
    }
    
    /**
     * 级联查询
     * 实时 -> 准实时 -> 离线
     */
    private Object getCascading(String factorId, String entityId) {
        // 优先查询实时数据
        Object value = realtimeStore.get(factorId, entityId);
        if (value != null) {
            return value;
        }
        
        // 查询准实时数据
        value = nearRealtimeStore.get(factorId, entityId);
        if (value != null) {
            return value;
        }
        
        // 查询离线数据
        return offlineStore.get(factorId, entityId);
    }
    
    /**
     * 异步获取因子数据
     */
    public CompletableFuture<Object> getFactorDataAsync(String factorId, String entityId) {
        return CompletableFuture.supplyAsync(() -> getFactorData(factorId, entityId));
    }
    
    /**
     * 批量获取因子数据
     */
    public Map<String, Object> getFactorDataBatch(String factorId, Iterable<String> entityIds) {
        Map<String, Object> result = new java.util.HashMap<>();
        
        for (String entityId : entityIds) {
            Object value = getFactorData(factorId, entityId);
            if (value != null) {
                result.put(entityId, value);
            }
        }
        
        return result;
    }
    
    /**
     * 存储因子数据
     * 
     * @param factorId 因子ID
     * @param entityId 实体ID
     * @param value 因子值
     * @param layer 数据层
     */
    public void putFactorData(String factorId, String entityId, Object value, DataLayer layer) {
        switch (layer) {
            case REALTIME:
                realtimeStore.put(factorId, entityId, value);
                break;
            case NEAR_REALTIME:
                nearRealtimeStore.put(factorId, entityId, value);
                break;
            case OFFLINE:
                offlineStore.put(factorId, entityId, value);
                break;
        }
    }
    
    /**
     * 设置数据访问策略
     */
    public void setStrategy(DataAccessStrategy strategy) {
        this.strategy = strategy;
    }
    
    /**
     * 获取统计信息
     */
    public DataLayerStats getStats() {
        return new DataLayerStats(
            realtimeStore.size(),
            nearRealtimeStore.size(),
            offlineStore.size()
        );
    }
    
    /**
     * 数据层枚举
     */
    public enum DataLayer {
        /**
         * 实时数据层 (毫秒级延迟)
         */
        REALTIME,
        
        /**
         * 准实时数据层 (秒级延迟)
         */
        NEAR_REALTIME,
        
        /**
         * 离线数据层 (T+1)
         */
        OFFLINE
    }
    
    /**
     * 数据访问策略
     */
    public enum DataAccessStrategy {
        /**
         * 级联查询 (实时->准实时->离线)
         */
        CASCADING,
        
        /**
         * 仅查询实时数据
         */
        REALTIME_ONLY,
        
        /**
         * 仅查询准实时数据
         */
        NEAR_REALTIME_ONLY,
        
        /**
         * 仅查询离线数据
         */
        OFFLINE_ONLY
    }
    
    /**
     * 实时数据存储
     */
    private static class RealtimeDataStore {
        private final Map<String, Map<String, Object>> data = new java.util.concurrent.ConcurrentHashMap<>();
        
        public Object get(String factorId, String entityId) {
            Map<String, Object> factorData = data.get(factorId);
            return factorData != null ? factorData.get(entityId) : null;
        }
        
        public void put(String factorId, String entityId, Object value) {
            data.computeIfAbsent(factorId, k -> new java.util.concurrent.ConcurrentHashMap<>())
                .put(entityId, value);
        }
        
        public int size() {
            return data.values().stream().mapToInt(Map::size).sum();
        }
    }
    
    /**
     * 准实时数据存储
     */
    private static class NearRealtimeDataStore {
        private final Map<String, Map<String, Object>> data = new java.util.concurrent.ConcurrentHashMap<>();
        
        public Object get(String factorId, String entityId) {
            Map<String, Object> factorData = data.get(factorId);
            return factorData != null ? factorData.get(entityId) : null;
        }
        
        public void put(String factorId, String entityId, Object value) {
            data.computeIfAbsent(factorId, k -> new java.util.concurrent.ConcurrentHashMap<>())
                .put(entityId, value);
        }
        
        public int size() {
            return data.values().stream().mapToInt(Map::size).sum();
        }
    }
    
    /**
     * 离线数据存储
     */
    private static class OfflineDataStore {
        private final Map<String, Map<String, Object>> data = new java.util.concurrent.ConcurrentHashMap<>();
        
        public Object get(String factorId, String entityId) {
            Map<String, Object> factorData = data.get(factorId);
            return factorData != null ? factorData.get(entityId) : null;
        }
        
        public void put(String factorId, String entityId, Object value) {
            data.computeIfAbsent(factorId, k -> new java.util.concurrent.ConcurrentHashMap<>())
                .put(entityId, value);
        }
        
        public int size() {
            return data.values().stream().mapToInt(Map::size).sum();
        }
    }
    
    /**
     * 数据层统计
     */
    public static class DataLayerStats {
        private final int realtimeCount;
        private final int nearRealtimeCount;
        private final int offlineCount;
        
        public DataLayerStats(int realtimeCount, int nearRealtimeCount, int offlineCount) {
            this.realtimeCount = realtimeCount;
            this.nearRealtimeCount = nearRealtimeCount;
            this.offlineCount = offlineCount;
        }
        
        public int getRealtimeCount() {
            return realtimeCount;
        }
        
        public int getNearRealtimeCount() {
            return nearRealtimeCount;
        }
        
        public int getOfflineCount() {
            return offlineCount;
        }
        
        public int getTotalCount() {
            return realtimeCount + nearRealtimeCount + offlineCount;
        }
        
        @Override
        public String toString() {
            return String.format("DataLayerStats{realtime=%d, nearRealtime=%d, offline=%d, total=%d}",
                realtimeCount, nearRealtimeCount, offlineCount, getTotalCount());
        }
    }
}
