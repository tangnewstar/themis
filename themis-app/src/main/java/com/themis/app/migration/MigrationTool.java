package com.themis.app.migration;

import com.themis.core.model.RuleDefinition;
import com.themis.app.compatibility.LegacyRuleAdapter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 规则迁移工具
 * 支持从旧系统平滑迁移到新系统
 */
public class MigrationTool {
    
    private final LegacyRuleAdapter adapter;
    private final MigrationConfig config;
    private final MigrationReport report;
    
    public MigrationTool(MigrationConfig config) {
        this.adapter = new LegacyRuleAdapter();
        this.config = config != null ? config : new MigrationConfig();
        this.report = new MigrationReport();
    }
    
    /**
     * 执行迁移
     * 
     * @param legacyRules 旧版规则
     * @return 迁移后的规则
     */
    public MigrationResult migrate(List<Map<String, Object>> legacyRules) {
        report.reset();
        report.setTotalRules(legacyRules.size());
        report.setStartTime(System.currentTimeMillis());
        
        List<RuleDefinition> migratedRules = new ArrayList<>();
        
        for (Map<String, Object> legacyRule : legacyRules) {
            try {
                RuleDefinition rule = adapter.adaptLegacyRule(legacyRule);
                
                // 验证规则
                if (config.isValidateAfterMigration()) {
                    ValidationResult validation = rule.validate();
                    if (!validation.isValid()) {
                        report.addError("Validation failed for rule: " + rule.getRuleId());
                        if (!config.isContinueOnError()) {
                            break;
                        }
                        continue;
                    }
                }
                
                migratedRules.add(rule);
                report.incrementSuccessCount();
                
            } catch (Exception e) {
                report.addError("Failed to migrate rule: " + e.getMessage());
                report.incrementFailureCount();
                
                if (!config.isContinueOnError()) {
                    break;
                }
            }
        }
        
        report.setEndTime(System.currentTimeMillis());
        
        return new MigrationResult(migratedRules, report);
    }
    
    /**
     * 验证迁移结果
     */
    public MigrationValidation validateMigration(
            List<Map<String, Object>> legacyRules,
            List<RuleDefinition> migratedRules) {
        
        MigrationValidation validation = new MigrationValidation();
        
        // 检查数量
        if (legacyRules.size() != migratedRules.size()) {
            validation.addWarning("Rule count mismatch: " + 
                legacyRules.size() + " legacy vs " + 
                migratedRules.size() + " migrated");
        }
        
        // 检查ID映射
        Set<String> legacyIds = new HashSet<>();
        for (Map<String, Object> rule : legacyRules) {
            Object id = rule.get("id");
            if (id != null) {
                legacyIds.add(String.valueOf(id));
            }
        }
        
        Set<String> migratedIds = new HashSet<>();
        for (RuleDefinition rule : migratedRules) {
            migratedIds.add(rule.getRuleId());
        }
        
        Set<String> missingIds = new HashSet<>(legacyIds);
        missingIds.removeAll(migratedIds);
        
        if (!missingIds.isEmpty()) {
            validation.addError("Missing rules after migration: " + missingIds);
        }
        
        return validation;
    }
    
    /**
     * 获取迁移报告
     */
    public MigrationReport getReport() {
        return report;
    }
    
    /**
     * 迁移配置
     */
    public static class MigrationConfig {
        private boolean validateAfterMigration = true;
        private boolean continueOnError = true;
        private boolean createBackup = true;
        private int batchSize = 100;
        
        public boolean isValidateAfterMigration() {
            return validateAfterMigration;
        }
        
        public void setValidateAfterMigration(boolean validateAfterMigration) {
            this.validateAfterMigration = validateAfterMigration;
        }
        
        public boolean isContinueOnError() {
            return continueOnError;
        }
        
        public void setContinueOnError(boolean continueOnError) {
            this.continueOnError = continueOnError;
        }
        
        public boolean isCreateBackup() {
            return createBackup;
        }
        
        public void setCreateBackup(boolean createBackup) {
            this.createBackup = createBackup;
        }
        
        public int getBatchSize() {
            return batchSize;
        }
        
        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
    }
    
    /**
     * 迁移报告
     */
    public static class MigrationReport {
        private int totalRules;
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final List<String> errors = new ArrayList<>();
        private long startTime;
        private long endTime;
        
        public void reset() {
            totalRules = 0;
            successCount.set(0);
            failureCount.set(0);
            errors.clear();
            startTime = 0;
            endTime = 0;
        }
        
        public void incrementSuccessCount() {
            successCount.incrementAndGet();
        }
        
        public void incrementFailureCount() {
            failureCount.incrementAndGet();
        }
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public int getTotalRules() {
            return totalRules;
        }
        
        public void setTotalRules(int totalRules) {
            this.totalRules = totalRules;
        }
        
        public int getSuccessCount() {
            return successCount.get();
        }
        
        public int getFailureCount() {
            return failureCount.get();
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public long getDuration() {
            return endTime - startTime;
        }
        
        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }
        
        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }
        
        @Override
        public String toString() {
            return String.format("Migration Report: Total=%d, Success=%d, Failed=%d, Duration=%dms",
                totalRules, getSuccessCount(), getFailureCount(), getDuration());
        }
    }
    
    /**
     * 迁移结果
     */
    public static class MigrationResult {
        private final List<RuleDefinition> migratedRules;
        private final MigrationReport report;
        
        public MigrationResult(List<RuleDefinition> migratedRules, MigrationReport report) {
            this.migratedRules = migratedRules;
            this.report = report;
        }
        
        public List<RuleDefinition> getMigratedRules() {
            return migratedRules;
        }
        
        public MigrationReport getReport() {
            return report;
        }
        
        public boolean isSuccess() {
            return report.getFailureCount() == 0;
        }
    }
    
    /**
     * 迁移验证
     */
    public static class MigrationValidation {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
    }
}
