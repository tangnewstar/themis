package com.themis.metadata.storage;

import com.themis.core.model.RuleDefinition;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 规则元数据仓库
 * 提供规则元数据的CRUD操作
 * 
 * 注意：这是内存实现，实际生产环境需要对接MySQL/Etcd/Redis
 */
public class RuleMetadataRepository {
    
    /**
     * 规则存储（规则ID -> 规则定义）
     */
    private final Map<String, RuleDefinition> rules = new ConcurrentHashMap<>();
    
    /**
     * 规则分类索引（分类 -> 规则ID列表）
     */
    private final Map<String, Set<String>> categoryIndex = new ConcurrentHashMap<>();
    
    /**
     * 规则标签索引（标签 -> 规则ID列表）
     */
    private final Map<String, Set<String>> tagIndex = new ConcurrentHashMap<>();
    
    /**
     * 保存规则
     */
    public void save(RuleDefinition rule) {
        if (rule == null || rule.getRuleId() == null) {
            throw new IllegalArgumentException("Rule and rule ID cannot be null");
        }
        
        rules.put(rule.getRuleId(), rule);
        
        // 更新索引
        updateIndexes(rule);
    }
    
    /**
     * 根据ID查找规则
     */
    public RuleDefinition findById(String ruleId) {
        return rules.get(ruleId);
    }
    
    /**
     * 查找所有规则
     */
    public List<RuleDefinition> findAll() {
        return new ArrayList<>(rules.values());
    }
    
    /**
     * 根据分类查找规则
     */
    public List<RuleDefinition> findByCategory(String category) {
        Set<String> ruleIds = categoryIndex.get(category);
        if (ruleIds == null) {
            return Collections.emptyList();
        }
        
        return ruleIds.stream()
            .map(rules::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * 根据标签查找规则
     */
    public List<RuleDefinition> findByTag(String tag) {
        Set<String> ruleIds = tagIndex.get(tag);
        if (ruleIds == null) {
            return Collections.emptyList();
        }
        
        return ruleIds.stream()
            .map(rules::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * 根据名称模糊查找
     */
    public List<RuleDefinition> findByNameLike(String namePattern) {
        return rules.values().stream()
            .filter(rule -> rule.getRuleName() != null && 
                           rule.getRuleName().contains(namePattern))
            .collect(Collectors.toList());
    }
    
    /**
     * 根据状态查找规则
     */
    public List<RuleDefinition> findByStatus(com.themis.core.model.RuleStatus status) {
        return rules.values().stream()
            .filter(rule -> rule.getStatus() == status)
            .collect(Collectors.toList());
    }
    
    /**
     * 删除规则
     */
    public boolean delete(String ruleId) {
        RuleDefinition removed = rules.remove(ruleId);
        
        if (removed != null) {
            // 清理索引
            removeFromIndexes(ruleId);
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查规则是否存在
     */
    public boolean exists(String ruleId) {
        return rules.containsKey(ruleId);
    }
    
    /**
     * 获取规则总数
     */
    public long count() {
        return rules.size();
    }
    
    /**
     * 清空所有规则
     */
    public void clear() {
        rules.clear();
        categoryIndex.clear();
        tagIndex.clear();
    }
    
    /**
     * 批量保存规则
     */
    public void saveAll(List<RuleDefinition> ruleList) {
        for (RuleDefinition rule : ruleList) {
            save(rule);
        }
    }
    
    /**
     * 更新索引
     */
    private void updateIndexes(RuleDefinition rule) {
        String ruleId = rule.getRuleId();
        
        // 更新分类索引
        if (rule.getMetadata() != null && rule.getMetadata().containsKey("category")) {
            String category = String.valueOf(rule.getMetadata().get("category"));
            categoryIndex.computeIfAbsent(category, k -> ConcurrentHashMap.newKeySet())
                        .add(ruleId);
        }
        
        // 更新标签索引
        if (rule.getTags() != null) {
            for (String tag : rule.getTags().values()) {
                tagIndex.computeIfAbsent(tag, k -> ConcurrentHashMap.newKeySet())
                       .add(ruleId);
            }
        }
    }
    
    /**
     * 从索引中移除
     */
    private void removeFromIndexes(String ruleId) {
        // 从分类索引中移除
        categoryIndex.values().forEach(set -> set.remove(ruleId));
        
        // 从标签索引中移除
        tagIndex.values().forEach(set -> set.remove(ruleId));
    }
    
    /**
     * 获取所有分类
     */
    public Set<String> getAllCategories() {
        return new HashSet<>(categoryIndex.keySet());
    }
    
    /**
     * 获取所有标签
     */
    public Set<String> getAllTags() {
        return new HashSet<>(tagIndex.keySet());
    }
}
