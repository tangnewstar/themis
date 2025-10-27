package com.themis.app.compatibility;

import com.themis.core.model.*;

import java.util.*;

/**
 * 旧版规则适配器
 * 支持将旧版规则格式转换为新版规则定义
 */
public class LegacyRuleAdapter {
    
    /**
     * 将旧版规则转换为新版ExpressionRule
     * 
     * @param legacyRule 旧版规则（Map格式）
     * @return 新版规则定义
     */
    public RuleDefinition adaptLegacyRule(Map<String, Object> legacyRule) {
        if (legacyRule == null) {
            throw new IllegalArgumentException("Legacy rule cannot be null");
        }
        
        String ruleId = getString(legacyRule, "id", "rule_" + UUID.randomUUID());
        String ruleName = getString(legacyRule, "name", ruleId);
        
        // 创建ExpressionRule
        ExpressionRule rule = new ExpressionRule(ruleId, ruleName);
        
        // 转换基本属性
        rule.setDescription(getString(legacyRule, "description", ""));
        rule.setPriority(getInteger(legacyRule, "priority", 100));
        
        // 转换状态
        String status = getString(legacyRule, "status", "ACTIVE");
        rule.setStatus(convertStatus(status));
        
        // 转换条件
        Object condition = legacyRule.get("condition");
        if (condition != null) {
            rule.setCondition(adaptCondition(condition));
        }
        
        // 转换动作
        Object actions = legacyRule.get("actions");
        if (actions != null) {
            rule.setActions(adaptActions(actions));
        }
        
        // 转换元数据
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) legacyRule.get("metadata");
        if (metadata != null) {
            rule.setMetadata(new HashMap<>(metadata));
        }
        
        return rule;
    }
    
    /**
     * 适配条件
     */
    private RuleCondition adaptCondition(Object conditionObj) {
        if (conditionObj instanceof String) {
            // 字符串条件转换为SimpleCondition
            return new SimpleCondition((String) conditionObj);
        } else if (conditionObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> condMap = (Map<String, Object>) conditionObj;
            
            String operator = getString(condMap, "operator", "AND");
            String expression = getString(condMap, "expression", "true");
            
            return new SimpleCondition(expression);
        }
        
        return new SimpleCondition("true");
    }
    
    /**
     * 适配动作
     */
    private List<RuleAction> adaptActions(Object actionsObj) {
        List<RuleAction> actions = new ArrayList<>();
        
        if (actionsObj instanceof String) {
            actions.add(new SetVariableAction("result", actionsObj));
        } else if (actionsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> actionList = (List<Object>) actionsObj;
            
            for (Object actionObj : actionList) {
                if (actionObj instanceof String) {
                    actions.add(new SetVariableAction("action", actionObj));
                } else if (actionObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> actionMap = (Map<String, Object>) actionObj;
                    actions.add(adaptActionFromMap(actionMap));
                }
            }
        }
        
        return actions;
    }
    
    /**
     * 从Map适配动作
     */
    private RuleAction adaptActionFromMap(Map<String, Object> actionMap) {
        String type = getString(actionMap, "type", "SET_VARIABLE");
        
        switch (type) {
            case "SET_VARIABLE":
                String variable = getString(actionMap, "variable", "result");
                Object value = actionMap.get("value");
                return new SetVariableAction(variable, value);
                
            default:
                return new SetVariableAction("result", actionMap);
        }
    }
    
    /**
     * 转换状态
     */
    private RuleStatus convertStatus(String status) {
        if (status == null) {
            return RuleStatus.DRAFT;
        }
        
        switch (status.toUpperCase()) {
            case "ACTIVE":
            case "ENABLED":
                return RuleStatus.ACTIVE;
            case "INACTIVE":
            case "DISABLED":
                return RuleStatus.INACTIVE;
            case "ARCHIVED":
                return RuleStatus.ARCHIVED;
            default:
                return RuleStatus.DRAFT;
        }
    }
    
    /**
     * 批量适配规则
     */
    public List<RuleDefinition> adaptLegacyRules(List<Map<String, Object>> legacyRules) {
        List<RuleDefinition> rules = new ArrayList<>();
        
        for (Map<String, Object> legacyRule : legacyRules) {
            try {
                RuleDefinition rule = adaptLegacyRule(legacyRule);
                rules.add(rule);
            } catch (Exception e) {
                System.err.println("Failed to adapt legacy rule: " + e.getMessage());
            }
        }
        
        return rules;
    }
    
    /**
     * 获取字符串值
     */
    private String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? String.valueOf(value) : defaultValue;
    }
    
    /**
     * 获取整数值
     */
    private Integer getInteger(Map<String, Object> map, String key, Integer defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
}
