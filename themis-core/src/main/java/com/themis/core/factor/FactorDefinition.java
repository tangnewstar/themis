package com.themis.core.factor;

import java.util.List;
import java.util.Map;

/**
 * 因子定义模型
 * 描述因子的元信息、计算逻辑和缓存策略
 */
public class FactorDefinition {
    
    /**
     * 因子唯一标识
     */
    private String factorId;
    
    /**
     * 因子名称
     */
    private String factorName;
    
    /**
     * 因子类型
     */
    private FactorType factorType;
    
    /**
     * 数据源配置
     */
    private DataSourceConfig dataSource;
    
    /**
     * 计算逻辑表达式
     */
    private ComputeExpression computeLogic;
    
    /**
     * 缓存策略
     */
    private CacheStrategy cacheStrategy;
    
    /**
     * 时间窗配置（仅用于时间窗因子）
     */
    private TimeWindowConfig timeWindow;
    
    /**
     * 依赖的其他因子ID列表
     */
    private List<String> dependencies;
    
    /**
     * 因子描述
     */
    private String description;
    
    /**
     * 因子返回值类型
     */
    private Class<?> returnType;
    
    /**
     * 扩展参数
     */
    private Map<String, Object> parameters;
    
    /**
     * 因子分类/标签
     */
    private List<String> tags;
    
    /**
     * 因子版本
     */
    private String version;
    
    /**
     * 是否启用
     */
    private boolean enabled = true;
    
    public FactorDefinition() {
    }
    
    public FactorDefinition(String factorId, String factorName, FactorType factorType) {
        this.factorId = factorId;
        this.factorName = factorName;
        this.factorType = factorType;
    }
    
    // Getters and Setters
    
    public String getFactorId() {
        return factorId;
    }
    
    public void setFactorId(String factorId) {
        this.factorId = factorId;
    }
    
    public String getFactorName() {
        return factorName;
    }
    
    public void setFactorName(String factorName) {
        this.factorName = factorName;
    }
    
    public FactorType getFactorType() {
        return factorType;
    }
    
    public void setFactorType(FactorType factorType) {
        this.factorType = factorType;
    }
    
    public DataSourceConfig getDataSource() {
        return dataSource;
    }
    
    public void setDataSource(DataSourceConfig dataSource) {
        this.dataSource = dataSource;
    }
    
    public ComputeExpression getComputeLogic() {
        return computeLogic;
    }
    
    public void setComputeLogic(ComputeExpression computeLogic) {
        this.computeLogic = computeLogic;
    }
    
    public CacheStrategy getCacheStrategy() {
        return cacheStrategy;
    }
    
    public void setCacheStrategy(CacheStrategy cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }
    
    public TimeWindowConfig getTimeWindow() {
        return timeWindow;
    }
    
    public void setTimeWindow(TimeWindowConfig timeWindow) {
        this.timeWindow = timeWindow;
    }
    
    public List<String> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Class<?> getReturnType() {
        return returnType;
    }
    
    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 检查是否需要时间窗
     */
    public boolean requiresTimeWindow() {
        return factorType == FactorType.TIME_WINDOW_COUNT ||
               factorType == FactorType.TIME_WINDOW_SUM ||
               factorType == FactorType.TIME_WINDOW_AVG ||
               factorType == FactorType.TIME_WINDOW_MAX ||
               factorType == FactorType.TIME_WINDOW_MIN ||
               factorType == FactorType.TIME_WINDOW_DISTINCT ||
               factorType == FactorType.TIME_WINDOW_TREND;
    }
    
    /**
     * 检查是否有依赖
     */
    public boolean hasDependencies() {
        return dependencies != null && !dependencies.isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("FactorDefinition{factorId='%s', factorName='%s', factorType=%s}", 
                           factorId, factorName, factorType);
    }
}
