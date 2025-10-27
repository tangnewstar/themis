package com.themis.engine.governance;

import com.themis.core.model.RuleDefinition;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 规则冲突检测器
 * 检测规则之间的冲突和重复
 */
public class RuleConflictDetector {
    
    /**
     * 检测规则集中的冲突
     * 
     * @param rules 规则列表
     * @return 冲突列表
     */
    public List<RuleConflict> detectConflicts(List<RuleDefinition> rules) {
        List<RuleConflict> conflicts = new ArrayList<>();
        
        // 两两比较规则
        for (int i = 0; i < rules.size(); i++) {
            for (int j = i + 1; j < rules.size(); j++) {
                RuleDefinition rule1 = rules.get(i);
                RuleDefinition rule2 = rules.get(j);
                
                RuleConflict conflict = checkConflict(rule1, rule2);
                if (conflict != null) {
                    conflicts.add(conflict);
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * 检测两个规则之间的冲突
     */
    private RuleConflict checkConflict(RuleDefinition rule1, RuleDefinition rule2) {
        // 检查条件重叠
        if (hasConditionOverlap(rule1, rule2)) {
            // 检查动作冲突
            if (hasActionConflict(rule1, rule2)) {
                return new RuleConflict(
                    rule1.getRuleId(),
                    rule2.getRuleId(),
                    ConflictType.ACTION_CONFLICT,
                    "Rules have overlapping conditions but conflicting actions"
                );
            }
            
            // 检查完全重复
            if (isDuplicate(rule1, rule2)) {
                return new RuleConflict(
                    rule1.getRuleId(),
                    rule2.getRuleId(),
                    ConflictType.DUPLICATE,
                    "Rules are duplicates"
                );
            }
        }
        
        // 检查优先级冲突
        if (hasPriorityConflict(rule1, rule2)) {
            return new RuleConflict(
                rule1.getRuleId(),
                rule2.getRuleId(),
                ConflictType.PRIORITY_CONFLICT,
                "Rules have ambiguous priority ordering"
            );
        }
        
        return null;
    }
    
    /**
     * 检查条件重叠
     */
    private boolean hasConditionOverlap(RuleDefinition rule1, RuleDefinition rule2) {
        // 简化实现：检查条件字符串是否相似
        String cond1 = rule1.getCondition();
        String cond2 = rule2.getCondition();
        
        if (cond1 == null || cond2 == null) {
            return false;
        }
        
        // 条件完全相同
        if (cond1.equals(cond2)) {
            return true;
        }
        
        // 条件包含关系（简化检查）
        return cond1.contains(cond2) || cond2.contains(cond1);
    }
    
    /**
     * 检查动作冲突
     */
    private boolean hasActionConflict(RuleDefinition rule1, RuleDefinition rule2) {
        // 简化实现：检查动作是否互相矛盾
        List<String> actions1 = rule1.getActions();
        List<String> actions2 = rule2.getActions();
        
        if (actions1 == null || actions2 == null) {
            return false;
        }
        
        // 检查是否有相反的动作
        for (String action1 : actions1) {
            for (String action2 : actions2) {
                if (isConflictingAction(action1, action2)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 检查两个动作是否冲突
     */
    private boolean isConflictingAction(String action1, String action2) {
        // 简化实现：检查是否有明显冲突的动作
        // 例如：APPROVE vs REJECT, ALLOW vs DENY
        
        Map<String, String> conflictPairs = Map.of(
            "APPROVE", "REJECT",
            "ALLOW", "DENY",
            "PASS", "BLOCK"
        );
        
        for (Map.Entry<String, String> pair : conflictPairs.entrySet()) {
            if (action1.contains(pair.getKey()) && action2.contains(pair.getValue())) {
                return true;
            }
            if (action1.contains(pair.getValue()) && action2.contains(pair.getKey())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查是否为重复规则
     */
    private boolean isDuplicate(RuleDefinition rule1, RuleDefinition rule2) {
        return Objects.equals(rule1.getCondition(), rule2.getCondition()) &&
               Objects.equals(rule1.getActions(), rule2.getActions());
    }
    
    /**
     * 检查优先级冲突
     */
    private boolean hasPriorityConflict(RuleDefinition rule1, RuleDefinition rule2) {
        // 如果条件重叠但优先级相同，可能导致执行顺序不确定
        if (hasConditionOverlap(rule1, rule2)) {
            return Objects.equals(rule1.getPriority(), rule2.getPriority());
        }
        return false;
    }
    
    /**
     * 分析规则影响范围
     * 
     * @param rule 规则
     * @param allRules 所有规则
     * @return 受影响的规则ID列表
     */
    public List<String> analyzeImpact(RuleDefinition rule, List<RuleDefinition> allRules) {
        return allRules.stream()
            .filter(r -> !r.getRuleId().equals(rule.getRuleId()))
            .filter(r -> hasConditionOverlap(rule, r))
            .map(RuleDefinition::getRuleId)
            .collect(Collectors.toList());
    }
    
    /**
     * 规则冲突
     */
    public static class RuleConflict {
        private final String ruleId1;
        private final String ruleId2;
        private final ConflictType type;
        private final String description;
        
        public RuleConflict(String ruleId1, String ruleId2, ConflictType type, String description) {
            this.ruleId1 = ruleId1;
            this.ruleId2 = ruleId2;
            this.type = type;
            this.description = description;
        }
        
        public String getRuleId1() {
            return ruleId1;
        }
        
        public String getRuleId2() {
            return ruleId2;
        }
        
        public ConflictType getType() {
            return type;
        }
        
        public String getDescription() {
            return description;
        }
        
        @Override
        public String toString() {
            return String.format("Conflict [%s] between %s and %s: %s", 
                type, ruleId1, ruleId2, description);
        }
    }
    
    /**
     * 冲突类型
     */
    public enum ConflictType {
        /**
         * 动作冲突
         */
        ACTION_CONFLICT,
        
        /**
         * 重复规则
         */
        DUPLICATE,
        
        /**
         * 优先级冲突
         */
        PRIORITY_CONFLICT,
        
        /**
         * 逻辑矛盾
         */
        LOGICAL_CONTRADICTION
    }
}
