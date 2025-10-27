package com.themis.engine.selector;

import com.themis.core.engine.RuleEngine;
import com.themis.core.model.RuleDefinition;
import com.themis.core.model.RuleType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认引擎选择器实现
 * 基于规则特征自动选择最优引擎
 */
public class DefaultEngineSelector implements EngineSelector {
    
    private final Map<EngineType, RuleEngine> engines = new ConcurrentHashMap<>();
    private RuleEngine defaultEngine;
    
    @Override
    public RuleEngine selectEngine(RuleDefinition rule) {
        if (rule == null) {
            return defaultEngine;
        }
        
        // 选择策略
        EngineType selectedType = determineEngineType(rule);
        
        RuleEngine engine = engines.get(selectedType);
        return engine != null ? engine : defaultEngine;
    }
    
    /**
     * 确定引擎类型
     */
    private EngineType determineEngineType(RuleDefinition rule) {
        // 规则1: 脚本类型使用脚本引擎
        if (rule.getRuleType() == RuleType.FORMULA || 
            rule.getMetadata() != null && "script".equals(rule.getMetadata().get("type"))) {
            return EngineType.SCRIPT;
        }
        
        // 规则2: 高优先级或简单规则使用轻量级引擎
        if (rule.getPriority() != null && rule.getPriority() < 10) {
            return EngineType.LIGHTWEIGHT;
        }
        
        // 规则3: 复杂规则使用RETE引擎
        if (isComplexRule(rule)) {
            return EngineType.RETE;
        }
        
        // 默认使用轻量级引擎
        return EngineType.LIGHTWEIGHT;
    }
    
    /**
     * 判断是否为复杂规则
     */
    private boolean isComplexRule(RuleDefinition rule) {
        // 基于规则类型判断
        return rule.getRuleType() == RuleType.TABLE || 
               rule.getRuleType() == RuleType.SCORECARD;
    }
    
    @Override
    public EngineSelectionResult selectEngineForBatch(List<RuleDefinition> rules) {
        if (rules == null || rules.isEmpty()) {
            return new EngineSelectionResult(defaultEngine, rules);
        }
        
        // 统计不同引擎类型的规则数量
        Map<EngineType, List<RuleDefinition>> groupedRules = new HashMap<>();
        
        for (RuleDefinition rule : rules) {
            EngineType type = determineEngineType(rule);
            groupedRules.computeIfAbsent(type, k -> new ArrayList<>()).add(rule);
        }
        
        // 选择数量最多的引擎类型
        EngineType majorType = groupedRules.entrySet().stream()
            .max(Comparator.comparingInt(e -> e.getValue().size()))
            .map(Map.Entry::getKey)
            .orElse(EngineType.LIGHTWEIGHT);
        
        RuleEngine selectedEngine = engines.getOrDefault(majorType, defaultEngine);
        
        return new EngineSelectionResult(selectedEngine, rules, groupedRules);
    }
    
    @Override
    public void registerEngine(EngineType engineType, RuleEngine engine) {
        engines.put(engineType, engine);
        
        if (defaultEngine == null) {
            defaultEngine = engine;
        }
    }
    
    /**
     * 设置默认引擎
     */
    public void setDefaultEngine(RuleEngine engine) {
        this.defaultEngine = engine;
    }
}
