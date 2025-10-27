package com.themis.core.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一规则元模型 - RuleMetaModel
 * 提供规则的完整元信息，支持扩展和版本管理
 */
public class RuleMetaModel {
    
    /**
     * 规则唯一标识
     */
    private String ruleId;
    
    /**
     * 规则名称
     */
    private String ruleName;
    
    /**
     * 规则类型
     */
    private RuleType ruleType;
    
    /**
     * 条件表达式
     */
    private RuleCondition condition;
    
    /**
     * 执行动作
     */
    private RuleAction action;
    
    /**
     * 优先级（数值越小优先级越高）
     */
    private Integer priority;
    
    /**
     * 版本号
     */
    private String version;
    
    /**
     * 生命周期状态
     */
    private Lifecycle lifecycle;
    
    /**
     * 扩展元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 规则描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 更新人
     */
    private String updatedBy;
    
    /**
     * 规则标签
     */
    private Map<String, String> tags;
    
    /**
     * 规则分组
     */
    private String ruleGroup;
    
    /**
     * 规则分类
     */
    private String category;
    
    public RuleMetaModel() {
        this.metadata = new HashMap<>();
        this.tags = new HashMap<>();
        this.lifecycle = Lifecycle.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    public String getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }
    
    public String getRuleName() {
        return ruleName;
    }
    
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
    
    public RuleType getRuleType() {
        return ruleType;
    }
    
    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }
    
    public RuleCondition getCondition() {
        return condition;
    }
    
    public void setCondition(RuleCondition condition) {
        this.condition = condition;
    }
    
    public RuleAction getAction() {
        return action;
    }
    
    public void setAction(RuleAction action) {
        this.action = action;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public Lifecycle getLifecycle() {
        return lifecycle;
    }
    
    public void setLifecycle(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public Map<String, String> getTags() {
        return tags;
    }
    
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
    
    public String getRuleGroup() {
        return ruleGroup;
    }
    
    public void setRuleGroup(String ruleGroup) {
        this.ruleGroup = ruleGroup;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
     * 添加元数据
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    /**
     * 添加标签
     */
    public void addTag(String key, String value) {
        this.tags.put(key, value);
    }
    
    /**
     * 转换为RuleDefinition
     */
    public RuleDefinition toRuleDefinition() {
        // 此方法需要根据ruleType创建对应的RuleDefinition子类实例
        // 具体实现取决于RuleDefinition的子类结构
        throw new UnsupportedOperationException("Subclass must implement toRuleDefinition()");
    }
    
    /**
     * 从RuleDefinition创建RuleMetaModel
     */
    public static RuleMetaModel fromRuleDefinition(RuleDefinition ruleDefinition) {
        RuleMetaModel model = new RuleMetaModel();
        model.setRuleId(ruleDefinition.getRuleId());
        model.setRuleName(ruleDefinition.getRuleName());
        model.setRuleType(ruleDefinition.getRuleType());
        model.setDescription(ruleDefinition.getDescription());
        model.setVersion(ruleDefinition.getVersion());
        model.setPriority(ruleDefinition.getPriority());
        model.setCreatedAt(ruleDefinition.getCreatedAt());
        model.setUpdatedAt(ruleDefinition.getUpdatedAt());
        model.setCreatedBy(ruleDefinition.getCreatedBy());
        model.setUpdatedBy(ruleDefinition.getUpdatedBy());
        model.setMetadata(ruleDefinition.getMetadata());
        model.setTags(ruleDefinition.getTags());
        
        // 转换RuleStatus到Lifecycle
        if (ruleDefinition.getStatus() != null) {
            model.setLifecycle(Lifecycle.fromRuleStatus(ruleDefinition.getStatus()));
        }
        
        return model;
    }
}
