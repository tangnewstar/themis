package com.themis.core.factor;

/**
 * 因子计算结果
 */
public class FactorResult {
    
    /**
     * 因子ID
     */
    private String factorId;
    
    /**
     * 因子值
     */
    private Object value;
    
    /**
     * 是否计算成功
     */
    private boolean success;
    
    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;
    
    /**
     * 计算耗时（毫秒）
     */
    private long computeTimeMs;
    
    /**
     * 是否来自缓存
     */
    private boolean fromCache;
    
    /**
     * 计算时间戳
     */
    private long timestamp;
    
    /**
     * 数据源
     */
    private String dataSource;
    
    public FactorResult() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public FactorResult(String factorId, Object value) {
        this();
        this.factorId = factorId;
        this.value = value;
        this.success = true;
    }
    
    /**
     * 创建成功的结果
     */
    public static FactorResult success(String factorId, Object value) {
        return new FactorResult(factorId, value);
    }
    
    /**
     * 创建失败的结果
     */
    public static FactorResult failure(String factorId, String errorMessage) {
        FactorResult result = new FactorResult();
        result.setFactorId(factorId);
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
    
    // Getters and Setters
    
    public String getFactorId() {
        return factorId;
    }
    
    public void setFactorId(String factorId) {
        this.factorId = factorId;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public long getComputeTimeMs() {
        return computeTimeMs;
    }
    
    public void setComputeTimeMs(long computeTimeMs) {
        this.computeTimeMs = computeTimeMs;
    }
    
    public boolean isFromCache() {
        return fromCache;
    }
    
    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getDataSource() {
        return dataSource;
    }
    
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * 获取类型化的值
     */
    @SuppressWarnings("unchecked")
    public <T> T getValueAs(Class<T> type) {
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        // 尝试类型转换
        return convertValue(value, type);
    }
    
    /**
     * 类型转换
     */
    @SuppressWarnings("unchecked")
    private <T> T convertValue(Object value, Class<T> targetType) {
        if (targetType == String.class) {
            return (T) String.valueOf(value);
        } else if (targetType == Integer.class || targetType == int.class) {
            return (T) Integer.valueOf(value.toString());
        } else if (targetType == Long.class || targetType == long.class) {
            return (T) Long.valueOf(value.toString());
        } else if (targetType == Double.class || targetType == double.class) {
            return (T) Double.valueOf(value.toString());
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return (T) Boolean.valueOf(value.toString());
        }
        return targetType.cast(value);
    }
    
    @Override
    public String toString() {
        return String.format("FactorResult{factorId='%s', success=%s, value=%s, fromCache=%s, timeMs=%d}", 
                           factorId, success, value, fromCache, computeTimeMs);
    }
}
