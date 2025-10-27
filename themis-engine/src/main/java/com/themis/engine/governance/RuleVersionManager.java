package com.themis.engine.governance;

import com.themis.core.model.RuleDefinition;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 规则版本管理器
 * 支持规则多版本并存、版本切换、回滚等
 */
public class RuleVersionManager {
    
    /**
     * 规则版本存储（规则ID -> 版本列表）
     */
    private final Map<String, List<RuleVersion>> ruleVersions = new ConcurrentHashMap<>();
    
    /**
     * 当前激活版本（规则ID -> 版本号）
     */
    private final Map<String, String> activeVersions = new ConcurrentHashMap<>();
    
    /**
     * 创建新版本
     * 
     * @param ruleId 规则ID
     * @param rule 规则定义
     * @param versionComment 版本注释
     * @return 版本号
     */
    public String createVersion(String ruleId, RuleDefinition rule, String versionComment) {
        List<RuleVersion> versions = ruleVersions.computeIfAbsent(ruleId, k -> new ArrayList<>());
        
        // 生成新版本号
        String versionNumber = generateVersionNumber(versions);
        
        RuleVersion version = new RuleVersion(
            versionNumber,
            rule,
            versionComment,
            System.currentTimeMillis(),
            getCurrentUser()
        );
        
        versions.add(version);
        
        // 如果是第一个版本，自动激活
        if (versions.size() == 1) {
            activeVersions.put(ruleId, versionNumber);
        }
        
        return versionNumber;
    }
    
    /**
     * 获取指定版本
     */
    public RuleDefinition getVersion(String ruleId, String versionNumber) {
        List<RuleVersion> versions = ruleVersions.get(ruleId);
        if (versions == null) {
            return null;
        }
        
        return versions.stream()
            .filter(v -> v.getVersionNumber().equals(versionNumber))
            .map(RuleVersion::getRule)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * 获取当前激活版本
     */
    public RuleDefinition getActiveVersion(String ruleId) {
        String versionNumber = activeVersions.get(ruleId);
        if (versionNumber == null) {
            return null;
        }
        
        return getVersion(ruleId, versionNumber);
    }
    
    /**
     * 激活指定版本
     */
    public boolean activateVersion(String ruleId, String versionNumber) {
        RuleDefinition rule = getVersion(ruleId, versionNumber);
        if (rule == null) {
            return false;
        }
        
        activeVersions.put(ruleId, versionNumber);
        return true;
    }
    
    /**
     * 获取所有版本
     */
    public List<RuleVersion> getAllVersions(String ruleId) {
        List<RuleVersion> versions = ruleVersions.get(ruleId);
        return versions != null ? new ArrayList<>(versions) : Collections.emptyList();
    }
    
    /**
     * 获取版本历史
     */
    public List<VersionHistory> getVersionHistory(String ruleId) {
        List<RuleVersion> versions = getAllVersions(ruleId);
        String activeVersion = activeVersions.get(ruleId);
        
        List<VersionHistory> history = new ArrayList<>();
        for (RuleVersion version : versions) {
            boolean isActive = version.getVersionNumber().equals(activeVersion);
            history.add(new VersionHistory(version, isActive));
        }
        
        return history;
    }
    
    /**
     * 比较两个版本
     */
    public VersionDiff compareVersions(String ruleId, String version1, String version2) {
        RuleDefinition rule1 = getVersion(ruleId, version1);
        RuleDefinition rule2 = getVersion(ruleId, version2);
        
        if (rule1 == null || rule2 == null) {
            return null;
        }
        
        return new VersionDiff(version1, version2, rule1, rule2);
    }
    
    /**
     * 回滚到上一个版本
     */
    public boolean rollback(String ruleId) {
        List<RuleVersion> versions = ruleVersions.get(ruleId);
        if (versions == null || versions.size() < 2) {
            return false;
        }
        
        String currentVersion = activeVersions.get(ruleId);
        
        // 找到当前版本的索引
        int currentIndex = -1;
        for (int i = 0; i < versions.size(); i++) {
            if (versions.get(i).getVersionNumber().equals(currentVersion)) {
                currentIndex = i;
                break;
            }
        }
        
        if (currentIndex <= 0) {
            return false;
        }
        
        // 激活上一个版本
        String previousVersion = versions.get(currentIndex - 1).getVersionNumber();
        return activateVersion(ruleId, previousVersion);
    }
    
    /**
     * 删除指定版本
     */
    public boolean deleteVersion(String ruleId, String versionNumber) {
        List<RuleVersion> versions = ruleVersions.get(ruleId);
        if (versions == null) {
            return false;
        }
        
        // 不能删除激活的版本
        if (versionNumber.equals(activeVersions.get(ruleId))) {
            return false;
        }
        
        return versions.removeIf(v -> v.getVersionNumber().equals(versionNumber));
    }
    
    /**
     * 生成版本号
     */
    private String generateVersionNumber(List<RuleVersion> versions) {
        if (versions.isEmpty()) {
            return "1.0.0";
        }
        
        // 获取最新版本号并递增
        String lastVersion = versions.get(versions.size() - 1).getVersionNumber();
        String[] parts = lastVersion.split("\\.");
        
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = Integer.parseInt(parts[2]);
        
        // 简单递增patch版本
        patch++;
        
        return String.format("%d.%d.%d", major, minor, patch);
    }
    
    /**
     * 获取当前用户（框架实现）
     */
    private String getCurrentUser() {
        // TODO: 从上下文获取当前用户
        return "system";
    }
    
    /**
     * 规则版本
     */
    public static class RuleVersion {
        private final String versionNumber;
        private final RuleDefinition rule;
        private final String comment;
        private final long createTime;
        private final String createdBy;
        
        public RuleVersion(String versionNumber, RuleDefinition rule, String comment, 
                          long createTime, String createdBy) {
            this.versionNumber = versionNumber;
            this.rule = rule;
            this.comment = comment;
            this.createTime = createTime;
            this.createdBy = createdBy;
        }
        
        public String getVersionNumber() {
            return versionNumber;
        }
        
        public RuleDefinition getRule() {
            return rule;
        }
        
        public String getComment() {
            return comment;
        }
        
        public long getCreateTime() {
            return createTime;
        }
        
        public String getCreatedBy() {
            return createdBy;
        }
    }
    
    /**
     * 版本历史
     */
    public static class VersionHistory {
        private final RuleVersion version;
        private final boolean active;
        
        public VersionHistory(RuleVersion version, boolean active) {
            this.version = version;
            this.active = active;
        }
        
        public RuleVersion getVersion() {
            return version;
        }
        
        public boolean isActive() {
            return active;
        }
    }
    
    /**
     * 版本差异
     */
    public static class VersionDiff {
        private final String version1;
        private final String version2;
        private final RuleDefinition rule1;
        private final RuleDefinition rule2;
        
        public VersionDiff(String version1, String version2, 
                          RuleDefinition rule1, RuleDefinition rule2) {
            this.version1 = version1;
            this.version2 = version2;
            this.rule1 = rule1;
            this.rule2 = rule2;
        }
        
        public List<String> getDifferences() {
            List<String> diffs = new ArrayList<>();
            
            // 比较描述
            if (!Objects.equals(rule1.getDescription(), rule2.getDescription())) {
                diffs.add("Description changed");
            }
            
            // 比较状态
            if (!Objects.equals(rule1.getStatus(), rule2.getStatus())) {
                diffs.add("Status changed");
            }
            
            // 比较优先级
            if (!Objects.equals(rule1.getPriority(), rule2.getPriority())) {
                diffs.add("Priority changed");
            }
            
            // 比较元数据
            if (!Objects.equals(rule1.getMetadata(), rule2.getMetadata())) {
                diffs.add("Metadata changed");
            }
            
            return diffs;
        }
        
        public String getVersion1() {
            return version1;
        }
        
        public String getVersion2() {
            return version2;
        }
        
        public RuleDefinition getRule1() {
            return rule1;
        }
        
        public RuleDefinition getRule2() {
            return rule2;
        }
    }
}
