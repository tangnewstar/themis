package com.themis.engine.monitor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 性能监控器
 * 收集规则执行的性能指标：延迟、吞吐量、成功率等
 */
public class PerformanceMonitor {
    
    /**
     * 规则执行指标映射
     */
    private final Map<String, RuleMetrics> ruleMetricsMap = new ConcurrentHashMap<>();
    
    /**
     * 全局指标
     */
    private final GlobalMetrics globalMetrics = new GlobalMetrics();
    
    /**
     * 监控开关
     */
    private volatile boolean enabled = true;
    
    /**
     * 记录规则执行
     * 
     * @param ruleId 规则ID
     * @param executionTimeMillis 执行时间（毫秒）
     * @param success 是否成功
     */
    public void recordExecution(String ruleId, long executionTimeMillis, boolean success) {
        if (!enabled) {
            return;
        }
        
        // 更新规则指标
        RuleMetrics metrics = ruleMetricsMap.computeIfAbsent(ruleId, k -> new RuleMetrics(ruleId));
        metrics.record(executionTimeMillis, success);
        
        // 更新全局指标
        globalMetrics.record(executionTimeMillis, success);
    }
    
    /**
     * 获取规则指标
     */
    public RuleMetrics getRuleMetrics(String ruleId) {
        return ruleMetricsMap.get(ruleId);
    }
    
    /**
     * 获取全局指标
     */
    public GlobalMetrics getGlobalMetrics() {
        return globalMetrics;
    }
    
    /**
     * 获取所有规则指标
     */
    public Map<String, RuleMetrics> getAllRuleMetrics() {
        return new HashMap<>(ruleMetricsMap);
    }
    
    /**
     * 获取Top N慢规则
     */
    public List<RuleMetrics> getTopSlowRules(int topN) {
        return ruleMetricsMap.values().stream()
            .sorted((m1, m2) -> Double.compare(m2.getAvgExecutionTime(), m1.getAvgExecutionTime()))
            .limit(topN)
            .toList();
    }
    
    /**
     * 获取Top N高频规则
     */
    public List<RuleMetrics> getTopFrequentRules(int topN) {
        return ruleMetricsMap.values().stream()
            .sorted((m1, m2) -> Long.compare(m2.getTotalExecutions(), m1.getTotalExecutions()))
            .limit(topN)
            .toList();
    }
    
    /**
     * 重置所有指标
     */
    public void reset() {
        ruleMetricsMap.clear();
        globalMetrics.reset();
    }
    
    /**
     * 启用监控
     */
    public void enable() {
        this.enabled = true;
    }
    
    /**
     * 禁用监控
     */
    public void disable() {
        this.enabled = false;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 规则执行指标
     */
    public static class RuleMetrics {
        private final String ruleId;
        private final LongAdder totalExecutions = new LongAdder();
        private final LongAdder successCount = new LongAdder();
        private final LongAdder failureCount = new LongAdder();
        private final LongAdder totalExecutionTime = new LongAdder();
        
        private final AtomicLong minExecutionTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxExecutionTime = new AtomicLong(0);
        
        private volatile long lastExecutionTime = 0;
        
        public RuleMetrics(String ruleId) {
            this.ruleId = ruleId;
        }
        
        void record(long executionTimeMillis, boolean success) {
            totalExecutions.increment();
            totalExecutionTime.add(executionTimeMillis);
            
            if (success) {
                successCount.increment();
            } else {
                failureCount.increment();
            }
            
            // 更新最小值
            minExecutionTime.updateAndGet(current -> 
                Math.min(current, executionTimeMillis));
            
            // 更新最大值
            maxExecutionTime.updateAndGet(current -> 
                Math.max(current, executionTimeMillis));
            
            lastExecutionTime = executionTimeMillis;
        }
        
        public String getRuleId() {
            return ruleId;
        }
        
        public long getTotalExecutions() {
            return totalExecutions.sum();
        }
        
        public long getSuccessCount() {
            return successCount.sum();
        }
        
        public long getFailureCount() {
            return failureCount.sum();
        }
        
        public double getSuccessRate() {
            long total = getTotalExecutions();
            return total > 0 ? (double) getSuccessCount() / total * 100 : 0;
        }
        
        public double getAvgExecutionTime() {
            long total = getTotalExecutions();
            return total > 0 ? (double) totalExecutionTime.sum() / total : 0;
        }
        
        public long getMinExecutionTime() {
            long min = minExecutionTime.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }
        
        public long getMaxExecutionTime() {
            return maxExecutionTime.get();
        }
        
        public long getLastExecutionTime() {
            return lastExecutionTime;
        }
        
        public double getThroughput(long windowMillis) {
            // 吞吐量 = 执行次数 / 时间窗口（秒）
            return getTotalExecutions() / (windowMillis / 1000.0);
        }
    }
    
    /**
     * 全局指标
     */
    public static class GlobalMetrics {
        private final LongAdder totalExecutions = new LongAdder();
        private final LongAdder successCount = new LongAdder();
        private final LongAdder failureCount = new LongAdder();
        private final LongAdder totalExecutionTime = new LongAdder();
        
        private volatile long startTime = System.currentTimeMillis();
        
        void record(long executionTimeMillis, boolean success) {
            totalExecutions.increment();
            totalExecutionTime.add(executionTimeMillis);
            
            if (success) {
                successCount.increment();
            } else {
                failureCount.increment();
            }
        }
        
        void reset() {
            totalExecutions.reset();
            successCount.reset();
            failureCount.reset();
            totalExecutionTime.reset();
            startTime = System.currentTimeMillis();
        }
        
        public long getTotalExecutions() {
            return totalExecutions.sum();
        }
        
        public long getSuccessCount() {
            return successCount.sum();
        }
        
        public long getFailureCount() {
            return failureCount.sum();
        }
        
        public double getSuccessRate() {
            long total = getTotalExecutions();
            return total > 0 ? (double) getSuccessCount() / total * 100 : 0;
        }
        
        public double getAvgExecutionTime() {
            long total = getTotalExecutions();
            return total > 0 ? (double) totalExecutionTime.sum() / total : 0;
        }
        
        public double getThroughput() {
            long elapsed = System.currentTimeMillis() - startTime;
            return elapsed > 0 ? getTotalExecutions() / (elapsed / 1000.0) : 0;
        }
        
        public long getUptime() {
            return System.currentTimeMillis() - startTime;
        }
    }
}
