package com.themis.core.factor;

import com.themis.core.engine.RuleContext;

import java.util.List;
import java.util.Map;

/**
 * 因子计算引擎接口
 * 负责因子的计算、缓存和依赖管理
 */
public interface FactorEngine {
    
    /**
     * 计算单个因子的值
     * 
     * @param factorId 因子ID
     * @param context 规则上下文
     * @return 因子计算结果
     * @throws FactorComputeException 计算失败时抛出
     */
    FactorResult compute(String factorId, RuleContext context) throws FactorComputeException;
    
    /**
     * 批量计算多个因子
     * 
     * @param factorIds 因子ID列表
     * @param context 规则上下文
     * @return 因子ID到计算结果的映射
     * @throws FactorComputeException 计算失败时抛出
     */
    Map<String, FactorResult> computeBatch(List<String> factorIds, RuleContext context) 
        throws FactorComputeException;
    
    /**
     * 注册因子定义
     * 
     * @param factorDefinition 因子定义
     */
    void registerFactor(FactorDefinition factorDefinition);
    
    /**
     * 取消注册因子
     * 
     * @param factorId 因子ID
     */
    void unregisterFactor(String factorId);
    
    /**
     * 获取因子定义
     * 
     * @param factorId 因子ID
     * @return 因子定义，如果不存在返回null
     */
    FactorDefinition getFactorDefinition(String factorId);
    
    /**
     * 分析因子依赖关系
     * 
     * @param factorId 因子ID
     * @return 依赖的因子ID列表（按依赖顺序）
     */
    List<String> analyzeDependencies(String factorId);
    
    /**
     * 检查因子是否存在循环依赖
     * 
     * @param factorId 因子ID
     * @return 如果存在循环依赖返回true
     */
    boolean hasCircularDependency(String factorId);
    
    /**
     * 清除因子缓存
     * 
     * @param factorId 因子ID
     * @param context 规则上下文
     */
    void clearCache(String factorId, RuleContext context);
    
    /**
     * 清除所有因子缓存
     */
    void clearAllCache();
    
    /**
     * 获取引擎支持的计算模式
     * 
     * @return 计算模式列表
     */
    List<ComputeMode> getSupportedModes();
    
    /**
     * 设置计算模式
     * 
     * @param mode 计算模式
     */
    void setComputeMode(ComputeMode mode);
    
    /**
     * 获取当前计算模式
     * 
     * @return 当前计算模式
     */
    ComputeMode getComputeMode();
    
    /**
     * 计算模式枚举
     */
    enum ComputeMode {
        /**
         * 实时计算 - 规则执行时同步计算
         * 适用于：低频访问、时效性要求高
         */
        REALTIME("realtime", "实时计算"),
        
        /**
         * 预计算 - 定时批量计算并缓存
         * 适用于：高频访问、计算复杂
         */
        PRECOMPUTE("precompute", "预计算"),
        
        /**
         * 流式计算 - 基于事件流实时更新
         * 适用于：时间窗因子、实时统计
         */
        STREAMING("streaming", "流式计算"),
        
        /**
         * 混合模式 - 预计算+增量更新
         * 适用于：兼顾性能和实时性
         */
        HYBRID("hybrid", "混合模式");
        
        private final String code;
        private final String description;
        
        ComputeMode(String code, String description) {
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
}
