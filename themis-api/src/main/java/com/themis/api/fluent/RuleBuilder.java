package com.themis.api.fluent;

import com.themis.core.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fluent API for building rule definitions.
 * Provides a readable and intuitive way to construct rules programmatically.
 */
public class RuleBuilder {
    
    private String ruleId;
    private String ruleName;
    private String description;
    private RuleType ruleType;
    private RuleStatus status = RuleStatus.DRAFT;
    private String version = "1.0.0";
    private Integer priority = 100;
    private String createdBy;
    private String updatedBy;
    private Map<String, Object> metadata = new HashMap<>();
    private Map<String, String> tags = new HashMap<>();
    
    private RuleBuilder(String ruleId, String ruleName) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
    }
    
    /**
     * Creates a new rule builder.
     * 
     * @param ruleId The unique rule identifier
     * @param ruleName The rule display name
     * @return A new RuleBuilder instance
     */
    public static RuleBuilder create(String ruleId, String ruleName) {
        return new RuleBuilder(ruleId, ruleName);
    }
    
    /**
     * Creates a new rule builder with auto-generated ID.
     * 
     * @param ruleName The rule display name
     * @return A new RuleBuilder instance
     */
    public static RuleBuilder create(String ruleName) {
        String ruleId = "rule-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        return new RuleBuilder(ruleId, ruleName);
    }
    
    /**
     * Sets the rule description.
     */
    public RuleBuilder description(String description) {
        this.description = description;
        return this;
    }
    
    /**
     * Sets the rule status.
     */
    public RuleBuilder status(RuleStatus status) {
        this.status = status;
        return this;
    }
    
    /**
     * Sets the rule version.
     */
    public RuleBuilder version(String version) {
        this.version = version;
        return this;
    }
    
    /**
     * Sets the rule priority.
     */
    public RuleBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }
    
    /**
     * Sets the rule creator.
     */
    public RuleBuilder createdBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }
    
    /**
     * Sets the rule updater.
     */
    public RuleBuilder updatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
        return this;
    }
    
    /**
     * Adds metadata to the rule.
     */
    public RuleBuilder metadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
    
    /**
     * Adds a tag to the rule.
     */
    public RuleBuilder tag(String key, String value) {
        this.tags.put(key, value);
        return this;
    }
    
    /**
     * Starts building an expression rule.
     */
    public ExpressionRuleBuilder when(ConditionBuilder conditionBuilder) {
        this.ruleType = RuleType.EXPRESSION;
        return new ExpressionRuleBuilder(this, conditionBuilder);
    }
    
    /**
     * Starts building a branching rule.
     */
    public BranchingRuleBuilder branch() {
        this.ruleType = RuleType.BRANCHING;
        return new BranchingRuleBuilder(this);
    }
    
    /**
     * Starts building a formula rule.
     */
    public FormulaRuleBuilder formula(String formula) {
        this.ruleType = RuleType.FORMULA;
        return new FormulaRuleBuilder(this, formula);
    }
    
    /**
     * Starts building a scorecard rule.
     */
    public ScorecardRuleBuilder scorecard() {
        this.ruleType = RuleType.SCORECARD;
        return new ScorecardRuleBuilder(this);
    }
    
    /**
     * Starts building a table rule.
     */
    public TableRuleBuilder table() {
        this.ruleType = RuleType.TABLE;
        return new TableRuleBuilder(this);
    }
    
    // Package-private getters for builders
    String getRuleId() { return ruleId; }
    String getRuleName() { return ruleName; }
    String getDescription() { return description; }
    RuleStatus getStatus() { return status; }
    String getVersion() { return version; }
    Integer getPriority() { return priority; }
    String getCreatedBy() { return createdBy; }
    String getUpdatedBy() { return updatedBy; }
    Map<String, Object> getMetadata() { return metadata; }
    Map<String, String> getTags() { return tags; }
    
    /**
     * Interface for building conditions.
     */
    public static interface ConditionBuilder {
        ConditionBuilder field(String fieldName);
        ConditionBuilder equalTo(Object value);
        ConditionBuilder notEquals(Object value);
        ConditionBuilder greaterThan(Object value);
        ConditionBuilder greaterThanOrEqual(Object value);
        ConditionBuilder lessThan(Object value);
        ConditionBuilder lessThanOrEqual(Object value);
        ConditionBuilder contains(Object value);
        ConditionBuilder startsWith(Object value);
        ConditionBuilder endsWith(Object value);
        ConditionBuilder in(Object... values);
        ConditionBuilder between(Object min, Object max);
        ConditionBuilder isNull();
        ConditionBuilder isNotNull();
        ConditionBuilder and();
        ConditionBuilder or();
        ConditionBuilder not();
        RuleCondition build();
    }
    
    /**
     * Default implementation of ConditionBuilder.
     */
    public static class DefaultConditionBuilder implements ConditionBuilder {
        private String currentField;
        private ComparisonOperator currentOperator;
        private Object currentValue;
        private LogicalOperator logicalOperator;
        private List<RuleCondition> conditions = new ArrayList<>();
        
        @Override
        public ConditionBuilder field(String fieldName) {
            this.currentField = fieldName;
            return this;
        }
        
        @Override
        public ConditionBuilder equalTo(Object value) {
            this.currentOperator = ComparisonOperator.EQUALS;
            this.currentValue = value;
            addCurrentCondition();
            return this;
        }
        
        @Override
        public ConditionBuilder notEquals(Object value) {
            this.currentOperator = ComparisonOperator.NOT_EQUALS;
            this.currentValue = value;
            addCurrentCondition();
            return this;
        }
        
        @Override
        public ConditionBuilder greaterThan(Object value) {
            this.currentOperator = ComparisonOperator.GREATER_THAN;
            this.currentValue = value;
            addCurrentCondition();
            return this;
        }
        
        @Override
        public ConditionBuilder greaterThanOrEqual(Object value) {
            this.currentOperator = ComparisonOperator.GREATER_THAN_OR_EQUAL;
            this.currentValue = value;
            addCurrentCondition();
            return this;
        }
        
        @Override
        public ConditionBuilder lessThan(Object value) {
            this.currentOperator = ComparisonOperator.LESS_THAN;
            this.currentValue = value;
            addCurrentCondition();
            return this;
        }
        
        @Override
        public ConditionBuilder lessThanOrEqual(Object value) {
            this.currentOperator = ComparisonOperator.LESS_THAN_OR_EQUAL;
            this.currentValue = value;
            addCurrentCondition();
            return this;
        }
        
        @Override
        public ConditionBuilder contains(Object value) {
            this.currentOperator = ComparisonOperator.CONTAINS;
            this.currentValue = value;
            addCurrentCondition();
            return this;
        }
        
        @Override
        public ConditionBuilder startsWith(Object value) {
            this.currentOperator = ComparisonOperator.STARTS_WITH;
            this.currentValue = value;
            addCurrentCondition();
            return this;
        }
        
        @Override
        public ConditionBuilder endsWith(Object value) {
            this.currentOperator = ComparisonOperator.ENDS_WITH;
            this.currentValue = value;
            addCurrentCondition();
            return this;
        }
        
        @Override
        public ConditionBuilder in(Object... values) {
            this.currentOperator = ComparisonOperator.IN;
            this.currentValue = values;
            addCurrentCondition();
            return this;
        }
        
        @Override
        public ConditionBuilder between(Object min, Object max) {
            this.currentOperator = ComparisonOperator.BETWEEN;
            this.currentValue = new Object[]{min, max};
            addCurrentCondition();
            return this;
        }
        
        @Override
        public ConditionBuilder isNull() {
            this.currentOperator = ComparisonOperator.IS_NULL;
            this.currentValue = null;
            addCurrentCondition();
            return this;
        }
        
        @Override
        public ConditionBuilder isNotNull() {
            this.currentOperator = ComparisonOperator.IS_NOT_NULL;
            this.currentValue = null;
            addCurrentCondition();
            return this;
        }
        
        @Override
        public ConditionBuilder and() {
            this.logicalOperator = LogicalOperator.AND;
            return this;
        }
        
        @Override
        public ConditionBuilder or() {
            this.logicalOperator = LogicalOperator.OR;
            return this;
        }
        
        @Override
        public ConditionBuilder not() {
            this.logicalOperator = LogicalOperator.NOT;
            return this;
        }
        
        @Override
        public RuleCondition build() {
            if (conditions.size() == 1) {
                return conditions.get(0);
            } else if (conditions.size() > 1) {
                // Create composite condition
                // This is a simplified implementation - in practice, you'd need proper composite condition handling
                return conditions.get(0); // Return first condition for now
            }
            return null;
        }
        
        private void addCurrentCondition() {
            if (currentField != null && currentOperator != null) {
                // Create simple condition - this is a placeholder implementation
                // In practice, you'd create the actual SimpleCondition instance
                RuleCondition condition = new RuleCondition(RuleCondition.ConditionType.SIMPLE) {
                    @Override
                    public ValidationResult validate() {
                        return ValidationResult.success();
                    }
                    
                    @Override
                    public RuleCondition copy() {
                        return this;
                    }
                    
                    @Override
                    public String toHumanReadable() {
                        return currentField + " " + currentOperator + " " + currentValue;
                    }
                };
                
                conditions.add(condition);
                
                // Reset current values
                currentField = null;
                currentOperator = null;
                currentValue = null;
            }
        }
    }
    
    /**
     * Interface for building actions.
     */
    public static interface ActionBuilder {
        ActionBuilder set(String field, Object value);
        ActionBuilder setExpression(String field, String expression);
        ActionBuilder calculate(String resultField, String formula);
        ActionBuilder callService(String serviceName, String methodName);
        ActionBuilder log(String message);
        ActionBuilder logLevel(String level);
        List<RuleAction> build();
    }
    
    /**
     * Default implementation of ActionBuilder.
     */
    public static class DefaultActionBuilder implements ActionBuilder {
        private final List<RuleAction> actions = new ArrayList<>();
        
        @Override
        public ActionBuilder set(String field, Object value) {
            // Create SetFieldAction - placeholder implementation
            RuleAction action = new RuleAction(RuleAction.ActionType.SET_FIELD) {
                @Override
                public ValidationResult validate() {
                    return ValidationResult.success();
                }
                
                @Override
                public RuleAction copy() {
                    return this;
                }
                
                @Override
                public String toHumanReadable() {
                    return "Set " + field + " = " + value;
                }
            };
            actions.add(action);
            return this;
        }
        
        @Override
        public ActionBuilder setExpression(String field, String expression) {
            // Create SetFieldAction with expression - placeholder implementation
            RuleAction action = new RuleAction(RuleAction.ActionType.SET_FIELD) {
                @Override
                public ValidationResult validate() {
                    return ValidationResult.success();
                }
                
                @Override
                public RuleAction copy() {
                    return this;
                }
                
                @Override
                public String toHumanReadable() {
                    return "Set " + field + " = " + expression;
                }
            };
            actions.add(action);
            return this;
        }
        
        @Override
        public ActionBuilder calculate(String resultField, String formula) {
            // Create CalculateAction - placeholder implementation
            RuleAction action = new RuleAction(RuleAction.ActionType.CALCULATE) {
                @Override
                public ValidationResult validate() {
                    return ValidationResult.success();
                }
                
                @Override
                public RuleAction copy() {
                    return this;
                }
                
                @Override
                public String toHumanReadable() {
                    return "Calculate " + resultField + " = " + formula;
                }
            };
            actions.add(action);
            return this;
        }
        
        @Override
        public ActionBuilder callService(String serviceName, String methodName) {
            // Create CallServiceAction - placeholder implementation
            RuleAction action = new RuleAction(RuleAction.ActionType.CALL_SERVICE) {
                @Override
                public ValidationResult validate() {
                    return ValidationResult.success();
                }
                
                @Override
                public RuleAction copy() {
                    return this;
                }
                
                @Override
                public String toHumanReadable() {
                    return "Call " + serviceName + "." + methodName + "()";
                }
            };
            actions.add(action);
            return this;
        }
        
        @Override
        public ActionBuilder log(String message) {
            // Create LogAction - placeholder implementation
            RuleAction action = new RuleAction(RuleAction.ActionType.LOG) {
                @Override
                public ValidationResult validate() {
                    return ValidationResult.success();
                }
                
                @Override
                public RuleAction copy() {
                    return this;
                }
                
                @Override
                public String toHumanReadable() {
                    return "Log: " + message;
                }
            };
            actions.add(action);
            return this;
        }
        
        @Override
        public ActionBuilder logLevel(String level) {
            // Modify last log action's level - placeholder implementation
            return this;
        }
        
        @Override
        public List<RuleAction> build() {
            return new ArrayList<>(actions);
        }
    }
    
    /**
     * Creates a new condition builder.
     */
    public static ConditionBuilder condition() {
        return new DefaultConditionBuilder();
    }
    
    /**
     * Creates a new action builder.
     */
    public static ActionBuilder action() {
        return new DefaultActionBuilder();
    }
    
    /**
     * Creates a simple expression rule builder.
     */
    public static SimpleExpressionRuleBuilder expression() {
        return new SimpleExpressionRuleBuilder();
    }
    
    /**
     * Creates a simple formula rule builder.
     */
    public static SimpleFormulaRuleBuilder formula() {
        return new SimpleFormulaRuleBuilder();
    }
    
    /**
     * Simple builder for expression rules that matches test expectations.
     */
    public static class SimpleExpressionRuleBuilder {
        private String id;
        private String name;
        private String condition;
        private String action;
        
        public SimpleExpressionRuleBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public SimpleExpressionRuleBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public SimpleExpressionRuleBuilder condition(String condition) {
            this.condition = condition;
            return this;
        }
        
        public SimpleExpressionRuleBuilder action(String action) {
            this.action = action;
            return this;
        }
        
        public ExpressionRule build() {
            ExpressionRule rule = new ExpressionRule(id, name);
            
            // Create a simple condition from the string
            // This is a placeholder implementation - in practice would need proper parsing
            if (condition != null) {
                SimpleCondition simpleCondition = new SimpleCondition();
                simpleCondition.setField("condition");
                simpleCondition.setOperator(ComparisonOperator.EQUALS);
                simpleCondition.setValue(condition);
                rule.setCondition(simpleCondition);
            }
            
            // Create a simple action from the string
            if (action != null) {
                List<RuleAction> actions = new ArrayList<>();
                SimpleAction simpleAction = new SimpleAction(action);
                actions.add(simpleAction);
                rule.setActions(actions);
            }
            
            return rule;
        }
    }
    
    /**
     * Simple builder for formula rules that matches test expectations.
     */
    public static class SimpleFormulaRuleBuilder {
        private String id;
        private String name;
        private String outputVariable;
        private String formula;
        
        public SimpleFormulaRuleBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public SimpleFormulaRuleBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public SimpleFormulaRuleBuilder outputVariable(String outputVariable) {
            this.outputVariable = outputVariable;
            return this;
        }
        
        public SimpleFormulaRuleBuilder formula(String formula) {
            this.formula = formula;
            return this;
        }
        
        public FormulaRule build() {
            String resultField = outputVariable != null ? outputVariable : "result";
            return new FormulaRule(id, name, formula, resultField);
        }
    }
    
    /**
     * Simple condition implementation for string-based conditions.
     */
    private static class SimpleCondition extends RuleCondition {
        private String field;
        private ComparisonOperator operator;
        private Object value;
        
        public SimpleCondition() {
            super(ConditionType.SIMPLE);
        }
        
        public void setField(String field) {
            this.field = field;
        }
        
        public void setOperator(ComparisonOperator operator) {
            this.operator = operator;
        }
        
        public void setValue(Object value) {
            this.value = value;
        }
        
        @Override
        public ValidationResult validate() {
            return ValidationResult.success();
        }
        
        @Override
        public RuleCondition copy() {
            SimpleCondition copy = new SimpleCondition();
            copy.setField(this.field);
            copy.setOperator(this.operator);
            copy.setValue(this.value);
            return copy;
        }
        
        @Override
        public String toHumanReadable() {
            return value != null ? value.toString() : "";
        }
    }
    
    /**
     * Simple action implementation for string-based actions.
     */
    private static class SimpleAction extends RuleAction {
        private String actionString;
        
        public SimpleAction(String actionString) {
            super(ActionType.SET_FIELD);
            this.actionString = actionString;
        }
        
        public String getActionString() {
            return actionString;
        }
        
        @Override
        public ValidationResult validate() {
            return ValidationResult.success();
        }
        
        @Override
        public RuleAction copy() {
            return new SimpleAction(actionString);
        }
        
        @Override
        public String toHumanReadable() {
            return actionString;
        }
    }
}