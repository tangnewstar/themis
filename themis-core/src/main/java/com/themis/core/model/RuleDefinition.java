package com.themis.core.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base class for all rule definitions in the Themis Rule Engine.
 * This class provides the common structure and metadata for all rule types.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "ruleType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ExpressionRule.class, name = "EXPRESSION"),
    @JsonSubTypes.Type(value = BranchingRule.class, name = "BRANCHING"),
    @JsonSubTypes.Type(value = FormulaRule.class, name = "FORMULA"),
    @JsonSubTypes.Type(value = RuleTable.class, name = "TABLE"),
    @JsonSubTypes.Type(value = Scorecard.class, name = "SCORECARD")
})
public abstract class RuleDefinition {
    
    @NotBlank(message = "Rule ID cannot be blank")
    private String ruleId;
    
    @NotBlank(message = "Rule name cannot be blank")
    private String ruleName;
    
    @NotNull(message = "Rule type cannot be null")
    private RuleType ruleType;
    
    private String description;
    
    @NotNull(message = "Rule status cannot be null")
    private RuleStatus status = RuleStatus.DRAFT;
    
    private String version = "1.0.0";
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
    
    private Map<String, Object> metadata;
    
    private Map<String, String> tags;
    
    // Priority for rule execution order (lower number = higher priority)
    private Integer priority = 100;
    
    // Constructors
    protected RuleDefinition() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    protected RuleDefinition(String ruleId, String ruleName, RuleType ruleType) {
        this();
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.ruleType = ruleType;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public RuleStatus getStatus() {
        return status;
    }
    
    public void setStatus(RuleStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
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
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public Map<String, String> getTags() {
        return tags;
    }
    
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    /**
     * Validates the rule definition for correctness and completeness.
     * @return ValidationResult containing any validation errors
     */
    public abstract ValidationResult validate();
    
    /**
     * Creates a deep copy of this rule definition.
     * @return A new instance with copied values
     */
    public abstract RuleDefinition copy();
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        RuleDefinition that = (RuleDefinition) obj;
        return ruleId != null ? ruleId.equals(that.ruleId) : that.ruleId == null;
    }
    
    @Override
    public int hashCode() {
        return ruleId != null ? ruleId.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return String.format("RuleDefinition{ruleId='%s', ruleName='%s', ruleType=%s, status=%s}", 
                           ruleId, ruleName, ruleType, status);
    }
}