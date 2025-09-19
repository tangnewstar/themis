package com.themis.metadata.storage;

import com.themis.core.model.RuleStatus;
import com.themis.core.model.RuleType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Query object for flexible rule metadata searching.
 */
public class MetadataQuery {
    
    private List<String> ruleIds;
    private List<RuleType> ruleTypes;
    private List<RuleStatus> ruleStatuses;
    private String ruleNamePattern;
    private String descriptionPattern;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private LocalDateTime updatedAfter;
    private LocalDateTime updatedBefore;
    private String createdBy;
    private String updatedBy;
    private Map<String, String> tags;
    private Map<String, Object> metadata;
    private Integer priorityMin;
    private Integer priorityMax;
    private String version;
    private int offset = 0;
    private int limit = 100;
    private List<SortCriteria> sortCriteria;
    
    // Default constructor
    public MetadataQuery() {}
    
    // Getters and Setters
    public List<String> getRuleIds() { return ruleIds; }
    public void setRuleIds(List<String> ruleIds) { this.ruleIds = ruleIds; }
    
    public List<RuleType> getRuleTypes() { return ruleTypes; }
    public void setRuleTypes(List<RuleType> ruleTypes) { this.ruleTypes = ruleTypes; }
    
    public List<RuleStatus> getRuleStatuses() { return ruleStatuses; }
    public void setRuleStatuses(List<RuleStatus> ruleStatuses) { this.ruleStatuses = ruleStatuses; }
    
    public String getRuleNamePattern() { return ruleNamePattern; }
    public void setRuleNamePattern(String ruleNamePattern) { this.ruleNamePattern = ruleNamePattern; }
    
    public String getDescriptionPattern() { return descriptionPattern; }
    public void setDescriptionPattern(String descriptionPattern) { this.descriptionPattern = descriptionPattern; }
    
    public LocalDateTime getCreatedAfter() { return createdAfter; }
    public void setCreatedAfter(LocalDateTime createdAfter) { this.createdAfter = createdAfter; }
    
    public LocalDateTime getCreatedBefore() { return createdBefore; }
    public void setCreatedBefore(LocalDateTime createdBefore) { this.createdBefore = createdBefore; }
    
    public LocalDateTime getUpdatedAfter() { return updatedAfter; }
    public void setUpdatedAfter(LocalDateTime updatedAfter) { this.updatedAfter = updatedAfter; }
    
    public LocalDateTime getUpdatedBefore() { return updatedBefore; }
    public void setUpdatedBefore(LocalDateTime updatedBefore) { this.updatedBefore = updatedBefore; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    public Map<String, String> getTags() { return tags; }
    public void setTags(Map<String, String> tags) { this.tags = tags; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public Integer getPriorityMin() { return priorityMin; }
    public void setPriorityMin(Integer priorityMin) { this.priorityMin = priorityMin; }
    
    public Integer getPriorityMax() { return priorityMax; }
    public void setPriorityMax(Integer priorityMax) { this.priorityMax = priorityMax; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public int getOffset() { return offset; }
    public void setOffset(int offset) { this.offset = Math.max(0, offset); }
    
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = Math.max(1, Math.min(1000, limit)); }
    
    public List<SortCriteria> getSortCriteria() { return sortCriteria; }
    public void setSortCriteria(List<SortCriteria> sortCriteria) { this.sortCriteria = sortCriteria; }
    
    /**
     * Builder pattern for constructing queries.
     */
    public static class Builder {
        private final MetadataQuery query = new MetadataQuery();
        
        public Builder ruleIds(List<String> ruleIds) {
            query.setRuleIds(ruleIds);
            return this;
        }
        
        public Builder ruleTypes(List<RuleType> ruleTypes) {
            query.setRuleTypes(ruleTypes);
            return this;
        }
        
        public Builder ruleStatuses(List<RuleStatus> ruleStatuses) {
            query.setRuleStatuses(ruleStatuses);
            return this;
        }
        
        public Builder ruleNamePattern(String pattern) {
            query.setRuleNamePattern(pattern);
            return this;
        }
        
        public Builder descriptionPattern(String pattern) {
            query.setDescriptionPattern(pattern);
            return this;
        }
        
        public Builder createdAfter(LocalDateTime dateTime) {
            query.setCreatedAfter(dateTime);
            return this;
        }
        
        public Builder createdBefore(LocalDateTime dateTime) {
            query.setCreatedBefore(dateTime);
            return this;
        }
        
        public Builder updatedAfter(LocalDateTime dateTime) {
            query.setUpdatedAfter(dateTime);
            return this;
        }
        
        public Builder updatedBefore(LocalDateTime dateTime) {
            query.setUpdatedBefore(dateTime);
            return this;
        }
        
        public Builder createdBy(String user) {
            query.setCreatedBy(user);
            return this;
        }
        
        public Builder updatedBy(String user) {
            query.setUpdatedBy(user);
            return this;
        }
        
        public Builder tags(Map<String, String> tags) {
            query.setTags(tags);
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            query.setMetadata(metadata);
            return this;
        }
        
        public Builder priorityRange(Integer min, Integer max) {
            query.setPriorityMin(min);
            query.setPriorityMax(max);
            return this;
        }
        
        public Builder version(String version) {
            query.setVersion(version);
            return this;
        }
        
        public Builder pagination(int offset, int limit) {
            query.setOffset(offset);
            query.setLimit(limit);
            return this;
        }
        
        public Builder sortBy(String field, SortDirection direction) {
            if (query.getSortCriteria() == null) {
                query.setSortCriteria(new java.util.ArrayList<>());
            }
            query.getSortCriteria().add(new SortCriteria(field, direction));
            return this;
        }
        
        public MetadataQuery build() {
            return query;
        }
    }
    
    /**
     * Sort criteria for query results.
     */
    public static class SortCriteria {
        private final String field;
        private final SortDirection direction;
        
        public SortCriteria(String field, SortDirection direction) {
            this.field = field;
            this.direction = direction;
        }
        
        public String getField() { return field; }
        public SortDirection getDirection() { return direction; }
        
        @Override
        public String toString() {
            return field + " " + direction;
        }
    }
    
    /**
     * Sort direction enumeration.
     */
    public enum SortDirection {
        ASC, DESC
    }
    
    @Override
    public String toString() {
        return String.format("MetadataQuery{types=%s, statuses=%s, offset=%d, limit=%d}", 
                           ruleTypes, ruleStatuses, offset, limit);
    }
}