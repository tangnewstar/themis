package com.themis.api.fluent;

import com.themis.core.model.*;

import java.util.List;

/**
 * Builder for expression rules.
 */
public class ExpressionRuleBuilder {
    
    private final RuleBuilder ruleBuilder;
    private final RuleBuilder.ConditionBuilder conditionBuilder;
    
    ExpressionRuleBuilder(RuleBuilder ruleBuilder, RuleBuilder.ConditionBuilder conditionBuilder) {
        this.ruleBuilder = ruleBuilder;
        this.conditionBuilder = conditionBuilder;
    }
    
    /**
     * Defines the actions to execute when the condition is met.
     */
    public ExpressionRule then(RuleBuilder.ActionBuilder actionBuilder) {
        RuleCondition condition = conditionBuilder.build();
        List<RuleAction> actions = actionBuilder.build();
        
        ExpressionRule rule = new ExpressionRule(ruleBuilder.getRuleId(), ruleBuilder.getRuleName());
        
        // Set basic properties
        rule.setDescription(ruleBuilder.getDescription());
        rule.setStatus(ruleBuilder.getStatus());
        rule.setVersion(ruleBuilder.getVersion());
        rule.setPriority(ruleBuilder.getPriority());
        rule.setCreatedBy(ruleBuilder.getCreatedBy());
        rule.setUpdatedBy(ruleBuilder.getUpdatedBy());
        rule.setMetadata(ruleBuilder.getMetadata());
        rule.setTags(ruleBuilder.getTags());
        
        // Set expression rule specific properties
        rule.setCondition(condition);
        rule.setActions(actions);
        
        return rule;
    }
}

/**
 * Builder for branching rules.
 */
class BranchingRuleBuilder {
    
    private final RuleBuilder ruleBuilder;
    private final BranchingRule rule;
    
    BranchingRuleBuilder(RuleBuilder ruleBuilder) {
        this.ruleBuilder = ruleBuilder;
        this.rule = new BranchingRule(ruleBuilder.getRuleId(), ruleBuilder.getRuleName());
        
        // Set basic properties
        rule.setDescription(ruleBuilder.getDescription());
        rule.setStatus(ruleBuilder.getStatus());
        rule.setVersion(ruleBuilder.getVersion());
        rule.setPriority(ruleBuilder.getPriority());
        rule.setCreatedBy(ruleBuilder.getCreatedBy());
        rule.setUpdatedBy(ruleBuilder.getUpdatedBy());
        rule.setMetadata(ruleBuilder.getMetadata());
        rule.setTags(ruleBuilder.getTags());
    }
    
    /**
     * Adds a branch with condition and actions.
     */
    public BranchingRuleBuilder when(RuleBuilder.ConditionBuilder conditionBuilder) {
        return new BranchBuilder(this, conditionBuilder);
    }
    
    /**
     * Adds else actions to execute when no branch matches.
     */
    public BranchingRuleBuilder otherwise(RuleBuilder.ActionBuilder actionBuilder) {
        List<RuleAction> actions = actionBuilder.build();
        rule.setElseActions(actions);
        return this;
    }
    
    /**
     * Builds the branching rule.
     */
    public BranchingRule build() {
        return rule;
    }
    
    private void addBranch(RuleCondition condition, List<RuleAction> actions) {
        BranchingRule.RuleBranch branch = new BranchingRule.RuleBranch(condition, actions);
        rule.addBranch(branch);
    }
    
    /**
     * Helper class for building individual branches.
     */
    private static class BranchBuilder extends BranchingRuleBuilder {
        private final BranchingRuleBuilder parent;
        private final RuleBuilder.ConditionBuilder conditionBuilder;
        
        BranchBuilder(BranchingRuleBuilder parent, RuleBuilder.ConditionBuilder conditionBuilder) {
            super(parent.ruleBuilder);
            this.parent = parent;
            this.conditionBuilder = conditionBuilder;
        }
        
        /**
         * Defines the actions for this branch.
         */
        public BranchingRuleBuilder then(RuleBuilder.ActionBuilder actionBuilder) {
            RuleCondition condition = conditionBuilder.build();
            List<RuleAction> actions = actionBuilder.build();
            parent.addBranch(condition, actions);
            return parent;
        }
    }
}

/**
 * Builder for formula rules.
 */
class FormulaRuleBuilder {
    
    private final RuleBuilder ruleBuilder;
    private final String formula;
    private String resultField = "result";
    
    FormulaRuleBuilder(RuleBuilder ruleBuilder, String formula) {
        this.ruleBuilder = ruleBuilder;
        this.formula = formula;
    }
    
    /**
     * Sets the field where the formula result will be stored.
     */
    public FormulaRuleBuilder resultField(String resultField) {
        this.resultField = resultField;
        return this;
    }
    
    /**
     * Builds the formula rule.
     */
    public FormulaRule build() {
        FormulaRule rule = new FormulaRule(ruleBuilder.getRuleId(), ruleBuilder.getRuleName(), formula, resultField);
        
        // Set basic properties
        rule.setDescription(ruleBuilder.getDescription());
        rule.setStatus(ruleBuilder.getStatus());
        rule.setVersion(ruleBuilder.getVersion());
        rule.setPriority(ruleBuilder.getPriority());
        rule.setCreatedBy(ruleBuilder.getCreatedBy());
        rule.setUpdatedBy(ruleBuilder.getUpdatedBy());
        rule.setMetadata(ruleBuilder.getMetadata());
        rule.setTags(ruleBuilder.getTags());
        
        return rule;
    }
}

/**
 * Builder for scorecard rules.
 */
class ScorecardRuleBuilder {
    
    private final RuleBuilder ruleBuilder;
    private final Scorecard scorecard;
    
    ScorecardRuleBuilder(RuleBuilder ruleBuilder) {
        this.ruleBuilder = ruleBuilder;
        this.scorecard = new Scorecard(ruleBuilder.getRuleId(), ruleBuilder.getRuleName());
        
        // Set basic properties
        scorecard.setDescription(ruleBuilder.getDescription());
        scorecard.setStatus(ruleBuilder.getStatus());
        scorecard.setVersion(ruleBuilder.getVersion());
        scorecard.setPriority(ruleBuilder.getPriority());
        scorecard.setCreatedBy(ruleBuilder.getCreatedBy());
        scorecard.setUpdatedBy(ruleBuilder.getUpdatedBy());
        scorecard.setMetadata(ruleBuilder.getMetadata());
        scorecard.setTags(ruleBuilder.getTags());
    }
    
    /**
     * Adds a score factor to the scorecard.
     */
    public ScorecardRuleBuilder factor(String name, String field, double weight) {
        Scorecard.ScoreFactor factor = new Scorecard.ScoreFactor();
        factor.setName(name);
        factor.setField(field);
        factor.setWeight(weight);
        scorecard.addFactor(factor);
        return this;
    }
    
    /**
     * Sets the result field name.
     */
    public ScorecardRuleBuilder resultField(String resultField) {
        scorecard.setResultField(resultField);
        return this;
    }
    
    /**
     * Sets the minimum score.
     */
    public ScorecardRuleBuilder minScore(double minScore) {
        scorecard.setMinScore(minScore);
        return this;
    }
    
    /**
     * Sets the maximum score.
     */
    public ScorecardRuleBuilder maxScore(double maxScore) {
        scorecard.setMaxScore(maxScore);
        return this;
    }
    
    /**
     * Builds the scorecard rule.
     */
    public Scorecard build() {
        return scorecard;
    }
}

/**
 * Builder for table rules.
 */
class TableRuleBuilder {
    
    private final RuleBuilder ruleBuilder;
    private final RuleTable ruleTable;
    
    TableRuleBuilder(RuleBuilder ruleBuilder) {
        this.ruleBuilder = ruleBuilder;
        this.ruleTable = new RuleTable(ruleBuilder.getRuleId(), ruleBuilder.getRuleName());
        
        // Set basic properties
        ruleTable.setDescription(ruleBuilder.getDescription());
        ruleTable.setStatus(ruleBuilder.getStatus());
        ruleTable.setVersion(ruleBuilder.getVersion());
        ruleTable.setPriority(ruleBuilder.getPriority());
        ruleTable.setCreatedBy(ruleBuilder.getCreatedBy());
        ruleTable.setUpdatedBy(ruleBuilder.getUpdatedBy());
        ruleTable.setMetadata(ruleBuilder.getMetadata());
        ruleTable.setTags(ruleBuilder.getTags());
    }
    
    /**
     * Sets the table structure.
     */
    public TableRuleBuilder structure(java.util.List<String> inputColumns, java.util.List<String> outputColumns) {
        RuleTable.TableStructure structure = new RuleTable.TableStructure();
        structure.setInputColumns(inputColumns);
        structure.setOutputColumns(outputColumns);
        ruleTable.setTableStructure(structure);
        return this;
    }
    
    /**
     * Adds a row to the table.
     */
    public TableRuleBuilder row(java.util.Map<String, Object> rowData) {
        ruleTable.getTableData().add(rowData);
        return this;
    }
    
    /**
     * Sets the default value when no match is found.
     */
    public TableRuleBuilder defaultValue(String defaultValue) {
        ruleTable.setDefaultValue(defaultValue);
        return this;
    }
    
    /**
     * Builds the table rule.
     */
    public RuleTable build() {
        return ruleTable;
    }
}