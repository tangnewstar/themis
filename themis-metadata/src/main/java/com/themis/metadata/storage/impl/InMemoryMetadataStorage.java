package com.themis.metadata.storage.impl;

import com.themis.core.model.RuleDefinition;
import com.themis.core.model.RuleStatus;
import com.themis.core.model.RuleType;
import com.themis.metadata.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of MetadataStorage.
 * Suitable for testing and development environments.
 */
public class InMemoryMetadataStorage implements MetadataStorage {
    
    private static final Logger logger = LoggerFactory.getLogger(InMemoryMetadataStorage.class);
    
    private final Map<String, RuleDefinition> ruleStore = new ConcurrentHashMap<>();
    private boolean initialized = false;
    
    @Override
    public void initialize(StorageConfiguration config) throws MetadataStorageException {
        this.initialized = true;
        logger.info("In-memory metadata storage initialized successfully");
    }
    
    @Override
    public void save(RuleDefinition rule) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        if (ruleStore.containsKey(rule.getRuleId())) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.RESOURCE_ALREADY_EXISTS,
                "save",
                "Rule with ID " + rule.getRuleId() + " already exists"
            );
        }
        
        ruleStore.put(rule.getRuleId(), rule.copy());
        logger.debug("Saved rule to memory: {}", rule.getRuleId());
    }
    
    @Override
    public Optional<RuleDefinition> load(String ruleId) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        RuleDefinition rule = ruleStore.get(ruleId);
        return Optional.ofNullable(rule != null ? rule.copy() : null);
    }
    
    @Override
    public List<RuleDefinition> loadByType(RuleType ruleType) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        return ruleStore.values().stream()
            .filter(rule -> rule.getRuleType() == ruleType)
            .map(RuleDefinition::copy)
            .sorted(Comparator.comparing(RuleDefinition::getPriority)
                             .thenComparing(RuleDefinition::getRuleName))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<RuleDefinition> loadByStatus(RuleStatus status) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        return ruleStore.values().stream()
            .filter(rule -> rule.getStatus() == status)
            .map(RuleDefinition::copy)
            .sorted(Comparator.comparing(RuleDefinition::getUpdatedAt, Comparator.reverseOrder()))
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean delete(String ruleId) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        RuleDefinition removed = ruleStore.remove(ruleId);
        boolean deleted = removed != null;
        
        if (deleted) {
            logger.debug("Deleted rule from memory: {}", ruleId);
        }
        
        return deleted;
    }
    
    @Override
    public List<RuleDefinition> search(MetadataQuery query) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        return ruleStore.values().stream()
            .filter(rule -> matchesQuery(rule, query))
            .map(RuleDefinition::copy)
            .sorted(createComparator(query.getSortCriteria()))
            .skip(query.getOffset())
            .limit(query.getLimit())
            .collect(Collectors.toList());
    }
    
    @Override
    public List<String> listAllRuleIds() throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        return new ArrayList<>(ruleStore.keySet());
    }
    
    @Override
    public boolean exists(String ruleId) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        return ruleStore.containsKey(ruleId);
    }
    
    @Override
    public void update(RuleDefinition rule) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        if (!ruleStore.containsKey(rule.getRuleId())) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.RESOURCE_NOT_FOUND,
                "update",
                "Rule not found for update: " + rule.getRuleId()
            );
        }
        
        ruleStore.put(rule.getRuleId(), rule.copy());
        logger.debug("Updated rule in memory: {}", rule.getRuleId());
    }
    
    @Override
    public long count() throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        return ruleStore.size();
    }
    
    @Override
    public Map<RuleType, Long> countByType() throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        return ruleStore.values().stream()
            .collect(Collectors.groupingBy(
                RuleDefinition::getRuleType,
                Collectors.counting()
            ));
    }
    
    @Override
    public Map<RuleStatus, Long> countByStatus() throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        return ruleStore.values().stream()
            .collect(Collectors.groupingBy(
                RuleDefinition::getStatus,
                Collectors.counting()
            ));
    }
    
    @Override
    public boolean healthCheck() {
        return initialized;
    }
    
    @Override
    public Map<String, Object> getStorageMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            metrics.put("type", "MEMORY");
            metrics.put("initialized", initialized);
            metrics.put("healthy", true);
            
            if (initialized) {
                metrics.put("totalRules", ruleStore.size());
                metrics.put("rulesByType", countByType());
                metrics.put("rulesByStatus", countByStatus());
                
                // Memory-specific metrics
                Runtime runtime = Runtime.getRuntime();
                metrics.put("memoryUsedMB", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024);
                metrics.put("memoryTotalMB", runtime.totalMemory() / 1024 / 1024);
                metrics.put("memoryMaxMB", runtime.maxMemory() / 1024 / 1024);
            }
            
        } catch (Exception e) {
            logger.warn("Failed to collect memory storage metrics: {}", e.getMessage());
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }
    
    @Override
    public void shutdown() throws MetadataStorageException {
        ruleStore.clear();
        this.initialized = false;
        logger.info("In-memory metadata storage shutdown completed");
    }
    
    private boolean matchesQuery(RuleDefinition rule, MetadataQuery query) {
        // Filter by rule IDs
        if (query.getRuleIds() != null && !query.getRuleIds().isEmpty()) {
            if (!query.getRuleIds().contains(rule.getRuleId())) {
                return false;
            }
        }
        
        // Filter by rule types
        if (query.getRuleTypes() != null && !query.getRuleTypes().isEmpty()) {
            if (!query.getRuleTypes().contains(rule.getRuleType())) {
                return false;
            }
        }
        
        // Filter by rule statuses
        if (query.getRuleStatuses() != null && !query.getRuleStatuses().isEmpty()) {
            if (!query.getRuleStatuses().contains(rule.getStatus())) {
                return false;
            }
        }
        
        // Filter by rule name pattern
        if (query.getRuleNamePattern() != null) {
            if (rule.getRuleName() == null || 
                !rule.getRuleName().toLowerCase().contains(query.getRuleNamePattern().toLowerCase())) {
                return false;
            }
        }
        
        // Filter by description pattern
        if (query.getDescriptionPattern() != null) {
            if (rule.getDescription() == null || 
                !rule.getDescription().toLowerCase().contains(query.getDescriptionPattern().toLowerCase())) {
                return false;
            }
        }
        
        // Filter by created date range
        if (query.getCreatedAfter() != null) {
            if (rule.getCreatedAt() == null || rule.getCreatedAt().isBefore(query.getCreatedAfter())) {
                return false;
            }
        }
        
        if (query.getCreatedBefore() != null) {
            if (rule.getCreatedAt() == null || rule.getCreatedAt().isAfter(query.getCreatedBefore())) {
                return false;
            }
        }
        
        // Filter by updated date range
        if (query.getUpdatedAfter() != null) {
            if (rule.getUpdatedAt() == null || rule.getUpdatedAt().isBefore(query.getUpdatedAfter())) {
                return false;
            }
        }
        
        if (query.getUpdatedBefore() != null) {
            if (rule.getUpdatedAt() == null || rule.getUpdatedAt().isAfter(query.getUpdatedBefore())) {
                return false;
            }
        }
        
        // Filter by created by
        if (query.getCreatedBy() != null) {
            if (!query.getCreatedBy().equals(rule.getCreatedBy())) {
                return false;
            }
        }
        
        // Filter by updated by
        if (query.getUpdatedBy() != null) {
            if (!query.getUpdatedBy().equals(rule.getUpdatedBy())) {
                return false;
            }
        }
        
        // Filter by priority range
        if (query.getPriorityMin() != null) {
            if (rule.getPriority() == null || rule.getPriority() < query.getPriorityMin()) {
                return false;
            }
        }
        
        if (query.getPriorityMax() != null) {
            if (rule.getPriority() == null || rule.getPriority() > query.getPriorityMax()) {
                return false;
            }
        }
        
        // Filter by version
        if (query.getVersion() != null) {
            if (!query.getVersion().equals(rule.getVersion())) {
                return false;
            }
        }
        
        // Filter by tags
        if (query.getTags() != null && !query.getTags().isEmpty()) {
            if (rule.getTags() == null) {
                return false;
            }
            for (Map.Entry<String, String> tag : query.getTags().entrySet()) {
                if (!tag.getValue().equals(rule.getTags().get(tag.getKey()))) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private Comparator<RuleDefinition> createComparator(List<MetadataQuery.SortCriteria> sortCriteria) {
        if (sortCriteria == null || sortCriteria.isEmpty()) {
            return Comparator.comparing(RuleDefinition::getUpdatedAt, Comparator.reverseOrder());
        }
        
        Comparator<RuleDefinition> comparator = null;
        
        for (MetadataQuery.SortCriteria criteria : sortCriteria) {
            Comparator<RuleDefinition> fieldComparator = createFieldComparator(criteria.getField());
            
            if (criteria.getDirection() == MetadataQuery.SortDirection.DESC) {
                fieldComparator = fieldComparator.reversed();
            }
            
            if (comparator == null) {
                comparator = fieldComparator;
            } else {
                comparator = comparator.thenComparing(fieldComparator);
            }
        }
        
        return comparator;
    }
    
    private Comparator<RuleDefinition> createFieldComparator(String field) {
        switch (field.toLowerCase()) {
            case "ruleid":
            case "rule_id":
                return Comparator.comparing(RuleDefinition::getRuleId, Comparator.nullsLast(Comparator.naturalOrder()));
            case "rulename":
            case "rule_name":
                return Comparator.comparing(RuleDefinition::getRuleName, Comparator.nullsLast(Comparator.naturalOrder()));
            case "ruletype":
            case "rule_type":
                return Comparator.comparing(rule -> rule.getRuleType().name(), Comparator.nullsLast(Comparator.naturalOrder()));
            case "status":
                return Comparator.comparing(rule -> rule.getStatus().name(), Comparator.nullsLast(Comparator.naturalOrder()));
            case "createdat":
            case "created_at":
                return Comparator.comparing(RuleDefinition::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
            case "updatedat":
            case "updated_at":
                return Comparator.comparing(RuleDefinition::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
            case "priority":
                return Comparator.comparing(RuleDefinition::getPriority, Comparator.nullsLast(Comparator.naturalOrder()));
            case "version":
                return Comparator.comparing(RuleDefinition::getVersion, Comparator.nullsLast(Comparator.naturalOrder()));
            default:
                return Comparator.comparing(RuleDefinition::getRuleId);
        }
    }
}