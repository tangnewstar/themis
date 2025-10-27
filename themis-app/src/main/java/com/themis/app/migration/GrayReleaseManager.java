package com.themis.app.migration;

import com.themis.core.engine.RuleEngine;
import com.themis.core.context.RuleContext;
import com.themis.core.model.RuleDefinition;
import com.themis.core.model.RuleExecutionResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 灰度发布管理器
 * 支持双引擎并行运行和流量灰度切换
 */
public class GrayReleaseManager {
    
    /**
     * 旧引擎
     */
    private final RuleEngine legacyEngine;
    
    /**
     * 新引擎
     */
    private final RuleEngine newEngine;
    
    /**
     * 灰度配置
     */
    private volatile GrayConfig grayConfig;
    
    /**
     * 灰度统计
     */
    private final GrayStats stats = new GrayStats();
    
    public GrayReleaseManager(RuleEngine legacyEngine, RuleEngine newEngine) {
        this.legacyEngine = legacyEngine;
        this.newEngine = newEngine;
        this.grayConfig = new GrayConfig();
    }
    
    /**
     * 执行规则（带灰度策略）
     * 
     * @param rule 规则
     * @param context 上下文
     * @return 执行结果
     */
    public RuleExecutionResult execute(RuleDefinition rule, RuleContext context) {
        // 决定使用哪个引擎
        boolean useNewEngine = shouldUseNewEngine(context);
        
        RuleEngine selectedEngine = useNewEngine ? newEngine : legacyEngine;
        
        // 执行规则
        RuleExecutionResult result = selectedEngine.execute(rule, context);
        
        // 记录统计
        stats.record(useNewEngine, result.isSuccess());
        
        // 如果启用了对比模式，同时执行另一个引擎进行对比
        if (grayConfig.isCompareMode()) {
            compareEngines(rule, context, result, useNewEngine);
        }
        
        return result;
    }
    
    /**
     * 判断是否使用新引擎
     */
    private boolean shouldUseNewEngine(RuleContext context) {
        GrayStrategy strategy = grayConfig.getStrategy();
        
        switch (strategy) {
            case PERCENTAGE:
                // 按百分比灰度
                return shouldUseNewEngineByPercentage();
                
            case USER_WHITELIST:
                // 白名单用户使用新引擎
                String userId = (String) context.getAttribute("userId");
                return grayConfig.getUserWhitelist().contains(userId);
                
            case RULE_WHITELIST:
                // 白名单规则使用新引擎
                String ruleId = (String) context.getAttribute("ruleId");
                return grayConfig.getRuleWhitelist().contains(ruleId);
                
            case ALL_NEW:
                return true;
                
            case ALL_LEGACY:
            default:
                return false;
        }
    }
    
    /**
     * 按百分比决定是否使用新引擎
     */
    private boolean shouldUseNewEngineByPercentage() {
        int percentage = grayConfig.getNewEnginePercentage();
        int random = (int) (Math.random() * 100);
        return random < percentage;
    }
    
    /**
     * 对比两个引擎的执行结果
     */
    private void compareEngines(RuleDefinition rule, RuleContext context, 
                               RuleExecutionResult primaryResult, boolean primaryIsNew) {
        RuleEngine compareEngine = primaryIsNew ? legacyEngine : newEngine;
        
        try {
            RuleExecutionResult compareResult = compareEngine.execute(rule, context);
            
            // 比较结果
            boolean resultMatch = compareResults(primaryResult, compareResult);
            stats.recordComparison(resultMatch);
            
            if (!resultMatch) {
                logDifference(rule, primaryResult, compareResult);
            }
            
        } catch (Exception e) {
            System.err.println("Error in compare mode: " + e.getMessage());
        }
    }
    
    /**
     * 比较两个结果
     */
    private boolean compareResults(RuleExecutionResult result1, RuleExecutionResult result2) {
        if (result1.isSuccess() != result2.isSuccess()) {
            return false;
        }
        
        // 简化比较：只比较成功/失败状态
        // 实际应用可能需要比较具体的结果值
        return true;
    }
    
    /**
     * 记录差异
     */
    private void logDifference(RuleDefinition rule, 
                             RuleExecutionResult result1, 
                             RuleExecutionResult result2) {
        System.out.println("Result difference detected for rule: " + rule.getRuleId());
        System.out.println("  Result 1: " + result1);
        System.out.println("  Result 2: " + result2);
    }
    
    /**
     * 更新灰度配置
     */
    public void updateGrayConfig(GrayConfig config) {
        this.grayConfig = config;
    }
    
    /**
     * 获取灰度统计
     */
    public GrayStats getStats() {
        return stats;
    }
    
    /**
     * 逐步提升新引擎流量
     * 
     * @param step 每次提升的百分比
     */
    public void increaseNewEngineTraffic(int step) {
        int currentPercentage = grayConfig.getNewEnginePercentage();
        int newPercentage = Math.min(100, currentPercentage + step);
        
        grayConfig.setNewEnginePercentage(newPercentage);
        
        System.out.println("Increased new engine traffic to " + newPercentage + "%");
    }
    
    /**
     * 完全切换到新引擎
     */
    public void switchToNewEngine() {
        grayConfig.setStrategy(GrayStrategy.ALL_NEW);
        System.out.println("Switched to new engine completely");
    }
    
    /**
     * 回滚到旧引擎
     */
    public void rollbackToLegacyEngine() {
        grayConfig.setStrategy(GrayStrategy.ALL_LEGACY);
        System.out.println("Rolled back to legacy engine");
    }
    
    /**
     * 灰度策略
     */
    public enum GrayStrategy {
        /**
         * 按百分比灰度
         */
        PERCENTAGE,
        
        /**
         * 用户白名单
         */
        USER_WHITELIST,
        
        /**
         * 规则白名单
         */
        RULE_WHITELIST,
        
        /**
         * 全部使用新引擎
         */
        ALL_NEW,
        
        /**
         * 全部使用旧引擎
         */
        ALL_LEGACY
    }
    
    /**
     * 灰度配置
     */
    public static class GrayConfig {
        private GrayStrategy strategy = GrayStrategy.PERCENTAGE;
        private int newEnginePercentage = 0;
        private boolean compareMode = false;
        private Map<String, Boolean> userWhitelist = new ConcurrentHashMap<>();
        private Map<String, Boolean> ruleWhitelist = new ConcurrentHashMap<>();
        
        public GrayStrategy getStrategy() {
            return strategy;
        }
        
        public void setStrategy(GrayStrategy strategy) {
            this.strategy = strategy;
        }
        
        public int getNewEnginePercentage() {
            return newEnginePercentage;
        }
        
        public void setNewEnginePercentage(int percentage) {
            this.newEnginePercentage = Math.min(100, Math.max(0, percentage));
        }
        
        public boolean isCompareMode() {
            return compareMode;
        }
        
        public void setCompareMode(boolean compareMode) {
            this.compareMode = compareMode;
        }
        
        public Map<String, Boolean> getUserWhitelist() {
            return userWhitelist;
        }
        
        public Map<String, Boolean> getRuleWhitelist() {
            return ruleWhitelist;
        }
    }
    
    /**
     * 灰度统计
     */
    public static class GrayStats {
        private final AtomicInteger legacyCount = new AtomicInteger(0);
        private final AtomicInteger newCount = new AtomicInteger(0);
        private final AtomicInteger legacySuccessCount = new AtomicInteger(0);
        private final AtomicInteger newSuccessCount = new AtomicInteger(0);
        private final AtomicInteger comparisonCount = new AtomicInteger(0);
        private final AtomicInteger comparisonMatchCount = new AtomicInteger(0);
        
        public void record(boolean useNewEngine, boolean success) {
            if (useNewEngine) {
                newCount.incrementAndGet();
                if (success) {
                    newSuccessCount.incrementAndGet();
                }
            } else {
                legacyCount.incrementAndGet();
                if (success) {
                    legacySuccessCount.incrementAndGet();
                }
            }
        }
        
        public void recordComparison(boolean match) {
            comparisonCount.incrementAndGet();
            if (match) {
                comparisonMatchCount.incrementAndGet();
            }
        }
        
        public int getLegacyCount() {
            return legacyCount.get();
        }
        
        public int getNewCount() {
            return newCount.get();
        }
        
        public double getLegacySuccessRate() {
            int total = legacyCount.get();
            return total > 0 ? (double) legacySuccessCount.get() / total * 100 : 0;
        }
        
        public double getNewSuccessRate() {
            int total = newCount.get();
            return total > 0 ? (double) newSuccessCount.get() / total * 100 : 0;
        }
        
        public double getComparisonMatchRate() {
            int total = comparisonCount.get();
            return total > 0 ? (double) comparisonMatchCount.get() / total * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "GrayStats{legacy=%d (%.1f%% success), new=%d (%.1f%% success), match=%.1f%%}",
                getLegacyCount(), getLegacySuccessRate(),
                getNewCount(), getNewSuccessRate(),
                getComparisonMatchRate()
            );
        }
    }
}
