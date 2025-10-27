package com.themis.engine.abtest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AB测试管理器
 * 支持流量分配、效果评估、自动切换
 */
public class ABTestManager {
    
    /**
     * 实验配置映射
     */
    private final Map<String, Experiment> experiments = new ConcurrentHashMap<>();
    
    /**
     * 实验结果统计
     */
    private final Map<String, ExperimentStats> experimentStats = new ConcurrentHashMap<>();
    
    /**
     * 创建实验
     */
    public void createExperiment(Experiment experiment) {
        if (experiment == null || experiment.getExperimentId() == null) {
            throw new IllegalArgumentException("Invalid experiment");
        }
        
        // 验证分流配置
        validateTrafficAllocation(experiment);
        
        experiments.put(experiment.getExperimentId(), experiment);
        experimentStats.put(experiment.getExperimentId(), new ExperimentStats(experiment.getExperimentId()));
    }
    
    /**
     * 分流决策
     * 
     * @param experimentId 实验ID
     * @param userId 用户ID
     * @return 分配的变体ID
     */
    public String allocate(String experimentId, String userId) {
        Experiment experiment = experiments.get(experimentId);
        
        if (experiment == null || !experiment.isActive()) {
            return null;
        }
        
        // 基于用户ID的哈希分流
        return determineVariant(experiment, userId);
    }
    
    /**
     * 记录实验结果
     */
    public void recordResult(String experimentId, String variantId, boolean success, Map<String, Object> metrics) {
        ExperimentStats stats = experimentStats.get(experimentId);
        
        if (stats != null) {
            stats.record(variantId, success, metrics);
        }
    }
    
    /**
     * 获取实验统计
     */
    public ExperimentStats getStats(String experimentId) {
        return experimentStats.get(experimentId);
    }
    
    /**
     * 评估实验效果
     */
    public ExperimentEvaluation evaluate(String experimentId) {
        Experiment experiment = experiments.get(experimentId);
        ExperimentStats stats = experimentStats.get(experimentId);
        
        if (experiment == null || stats == null) {
            return null;
        }
        
        return new ExperimentEvaluation(experiment, stats);
    }
    
    /**
     * 停止实验
     */
    public void stopExperiment(String experimentId) {
        Experiment experiment = experiments.get(experimentId);
        if (experiment != null) {
            experiment.setActive(false);
        }
    }
    
    /**
     * 确定变体
     */
    private String determineVariant(Experiment experiment, String userId) {
        // 使用一致性哈希进行分流
        int hash = Math.abs(userId.hashCode());
        int bucket = hash % 100;
        
        int cumulative = 0;
        for (Variant variant : experiment.getVariants()) {
            cumulative += variant.getTrafficPercent();
            if (bucket < cumulative) {
                return variant.getVariantId();
            }
        }
        
        // 默认返回控制组
        return experiment.getVariants().get(0).getVariantId();
    }
    
    /**
     * 验证分流配置
     */
    private void validateTrafficAllocation(Experiment experiment) {
        int totalPercent = experiment.getVariants().stream()
            .mapToInt(Variant::getTrafficPercent)
            .sum();
        
        if (totalPercent != 100) {
            throw new IllegalArgumentException(
                "Traffic allocation must sum to 100%, got: " + totalPercent);
        }
    }
    
    /**
     * 实验配置
     */
    public static class Experiment {
        private String experimentId;
        private String name;
        private String description;
        private List<Variant> variants;
        private boolean active = true;
        private long startTime;
        private long endTime;
        
        public Experiment(String experimentId, String name) {
            this.experimentId = experimentId;
            this.name = name;
            this.variants = new ArrayList<>();
            this.startTime = System.currentTimeMillis();
        }
        
        public String getExperimentId() {
            return experimentId;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public List<Variant> getVariants() {
            return variants;
        }
        
        public void addVariant(Variant variant) {
            this.variants.add(variant);
        }
        
        public boolean isActive() {
            return active;
        }
        
        public void setActive(boolean active) {
            this.active = active;
            if (!active && endTime == 0) {
                endTime = System.currentTimeMillis();
            }
        }
        
        public long getStartTime() {
            return startTime;
        }
        
        public long getEndTime() {
            return endTime;
        }
    }
    
    /**
     * 实验变体
     */
    public static class Variant {
        private String variantId;
        private String name;
        private int trafficPercent;
        private Map<String, Object> config;
        
        public Variant(String variantId, String name, int trafficPercent) {
            this.variantId = variantId;
            this.name = name;
            this.trafficPercent = trafficPercent;
            this.config = new HashMap<>();
        }
        
        public String getVariantId() {
            return variantId;
        }
        
        public String getName() {
            return name;
        }
        
        public int getTrafficPercent() {
            return trafficPercent;
        }
        
        public Map<String, Object> getConfig() {
            return config;
        }
        
        public void setConfig(Map<String, Object> config) {
            this.config = config;
        }
    }
    
    /**
     * 实验统计
     */
    public static class ExperimentStats {
        private final String experimentId;
        private final Map<String, VariantStats> variantStats = new ConcurrentHashMap<>();
        
        public ExperimentStats(String experimentId) {
            this.experimentId = experimentId;
        }
        
        public void record(String variantId, boolean success, Map<String, Object> metrics) {
            VariantStats stats = variantStats.computeIfAbsent(
                variantId, k -> new VariantStats(variantId));
            stats.record(success, metrics);
        }
        
        public Map<String, VariantStats> getVariantStats() {
            return new HashMap<>(variantStats);
        }
        
        public String getExperimentId() {
            return experimentId;
        }
    }
    
    /**
     * 变体统计
     */
    public static class VariantStats {
        private final String variantId;
        private long totalCount = 0;
        private long successCount = 0;
        private double totalValue = 0;
        
        public VariantStats(String variantId) {
            this.variantId = variantId;
        }
        
        public synchronized void record(boolean success, Map<String, Object> metrics) {
            totalCount++;
            if (success) {
                successCount++;
            }
            
            if (metrics != null && metrics.containsKey("value")) {
                totalValue += ((Number) metrics.get("value")).doubleValue();
            }
        }
        
        public String getVariantId() {
            return variantId;
        }
        
        public long getTotalCount() {
            return totalCount;
        }
        
        public long getSuccessCount() {
            return successCount;
        }
        
        public double getSuccessRate() {
            return totalCount > 0 ? (double) successCount / totalCount : 0;
        }
        
        public double getAverageValue() {
            return totalCount > 0 ? totalValue / totalCount : 0;
        }
    }
    
    /**
     * 实验评估结果
     */
    public static class ExperimentEvaluation {
        private final Experiment experiment;
        private final ExperimentStats stats;
        private String winner;
        private double confidence;
        
        public ExperimentEvaluation(Experiment experiment, ExperimentStats stats) {
            this.experiment = experiment;
            this.stats = stats;
            this.winner = determineWinner();
        }
        
        private String determineWinner() {
            // 简化实现：选择成功率最高的变体
            return stats.getVariantStats().entrySet().stream()
                .max(Comparator.comparingDouble(e -> e.getValue().getSuccessRate()))
                .map(Map.Entry::getKey)
                .orElse(null);
        }
        
        public String getWinner() {
            return winner;
        }
        
        public double getConfidence() {
            return confidence;
        }
        
        public Experiment getExperiment() {
            return experiment;
        }
        
        public ExperimentStats getStats() {
            return stats;
        }
    }
}
