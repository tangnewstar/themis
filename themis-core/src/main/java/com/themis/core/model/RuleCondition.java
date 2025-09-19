package com.themis.core.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a condition in a rule that can be evaluated to true or false.
 * Supports simple conditions and complex logical expressions.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "conditionType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = SimpleCondition.class, name = "SIMPLE"),
    @JsonSubTypes.Type(value = CompositeCondition.class, name = "COMPOSITE")
})
public abstract class RuleCondition {
    
    @NotNull(message = "Condition type cannot be null")
    private ConditionType conditionType;
    
    private String description;
    
    private Map<String, Object> metadata;
    
    // Default constructor
    protected RuleCondition() {}
    
    protected RuleCondition(ConditionType conditionType) {
        this.conditionType = conditionType;
    }
    
    // Getters and Setters
    public ConditionType getConditionType() {
        return conditionType;
    }
    
    public void setConditionType(ConditionType conditionType) {
        this.conditionType = conditionType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    /**
     * Validates this condition for correctness.
     */
    public abstract ValidationResult validate();
    
    /**
     * Creates a copy of this condition.
     */
    public abstract RuleCondition copy();
    
    /**
     * Gets a human-readable representation of this condition.
     */
    public abstract String toHumanReadable();
    
    /**
     * Enumeration of condition types.
     */
    public enum ConditionType {
        SIMPLE("Simple Condition", "Single field comparison"),
        COMPOSITE("Composite Condition", "Multiple conditions with logical operators");
        
        private final String displayName;
        private final String description;
        
        ConditionType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
}

/**
 * Simple condition that compares a field value with an expected value using an operator.
 */
class SimpleCondition extends RuleCondition {
    
    @NotBlank(message = "Field name cannot be blank")
    private String field;
    
    @NotNull(message = "Operator cannot be null")
    private ComparisonOperator operator;
    
    private Object value;
    
    private boolean ignoreCase = false;
    
    // Default constructor
    public SimpleCondition() {
        super(ConditionType.SIMPLE);
    }
    
    public SimpleCondition(String field, ComparisonOperator operator, Object value) {
        super(ConditionType.SIMPLE);
        this.field = field;
        this.operator = operator;
        this.value = value;
    }
    
    // Getters and Setters
    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
    
    public ComparisonOperator getOperator() { return operator; }
    public void setOperator(ComparisonOperator operator) { this.operator = operator; }
    
    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
    
    public boolean isIgnoreCase() { return ignoreCase; }
    public void setIgnoreCase(boolean ignoreCase) { this.ignoreCase = ignoreCase; }
    
    @Override
    public ValidationResult validate() {
        List<ValidationResult.ValidationError> errors = new ArrayList<>();
        
        if (field == null || field.trim().isEmpty()) {
            errors.add(new ValidationResult.ValidationError("MISSING_FIELD", "Field name is required", "field"));
        }
        
        if (operator == null) {
            errors.add(new ValidationResult.ValidationError("MISSING_OPERATOR", "Operator is required", "operator"));
        }
        
        // Validate value based on operator
        if (operator != null) {
            switch (operator) {
                case IS_NULL:
                case IS_NOT_NULL:
                    // No value needed
                    break;
                case IN:
                case NOT_IN:
                    if (value == null || !(value instanceof Iterable)) {
                        errors.add(new ValidationResult.ValidationError("INVALID_VALUE", 
                            "IN/NOT_IN operators require a collection value", "value"));
                    }
                    break;
                case BETWEEN:
                    if (value == null || !(value instanceof Object[]) || ((Object[]) value).length != 2) {
                        errors.add(new ValidationResult.ValidationError("INVALID_VALUE", 
                            "BETWEEN operator requires an array of two values", "value"));
                    }
                    break;
                default:
                    if (value == null) {
                        errors.add(new ValidationResult.ValidationError("MISSING_VALUE", 
                            "Value is required for operator " + operator, "value"));
                    }
                    break;
            }
        }
        
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }
    
    @Override
    public RuleCondition copy() {
        SimpleCondition copy = new SimpleCondition(field, operator, value);
        copy.setDescription(getDescription());
        copy.setIgnoreCase(ignoreCase);
        
        if (getMetadata() != null) {
            copy.setMetadata(new java.util.HashMap<>(getMetadata()));
        }
        
        return copy;
    }
    
    @Override
    public String toHumanReadable() {
        StringBuilder sb = new StringBuilder();
        sb.append(field);
        
        if (operator != null) {
            sb.append(" ").append(operator.getSymbol()).append(" ");
            
            switch (operator) {
                case IS_NULL:
                case IS_NOT_NULL:
                    // No value
                    break;
                case IN:
                case NOT_IN:
                    sb.append("(").append(value).append(")");
                    break;
                case BETWEEN:
                    if (value instanceof Object[]) {
                        Object[] range = (Object[]) value;
                        sb.append(range[0]).append(" and ").append(range[1]);
                    }
                    break;
                default:
                    sb.append(value);
                    break;
            }
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("SimpleCondition{field='%s', operator=%s, value=%s}", field, operator, value);
    }
}

/**
 * Composite condition that combines multiple conditions with logical operators.
 */
class CompositeCondition extends RuleCondition {
    
    @NotNull(message = "Logical operator cannot be null")
    private LogicalOperator logicalOperator;
    
    @NotNull(message = "Conditions list cannot be null")
    private List<RuleCondition> conditions = new ArrayList<>();
    
    // Default constructor
    public CompositeCondition() {
        super(ConditionType.COMPOSITE);
    }
    
    public CompositeCondition(LogicalOperator logicalOperator, List<RuleCondition> conditions) {
        super(ConditionType.COMPOSITE);
        this.logicalOperator = logicalOperator;
        this.conditions = conditions != null ? conditions : new ArrayList<>();
    }
    
    // Getters and Setters
    public LogicalOperator getLogicalOperator() { return logicalOperator; }
    public void setLogicalOperator(LogicalOperator logicalOperator) { this.logicalOperator = logicalOperator; }
    
    public List<RuleCondition> getConditions() { return conditions; }
    public void setConditions(List<RuleCondition> conditions) { 
        this.conditions = conditions != null ? conditions : new ArrayList<>(); 
    }
    
    public void addCondition(RuleCondition condition) {
        if (condition != null) {
            this.conditions.add(condition);
        }
    }
    
    @Override
    public ValidationResult validate() {
        List<ValidationResult.ValidationError> errors = new ArrayList<>();
        List<ValidationResult.ValidationWarning> warnings = new ArrayList<>();
        
        if (logicalOperator == null) {
            errors.add(new ValidationResult.ValidationError("MISSING_LOGICAL_OPERATOR", 
                "Logical operator is required", "logicalOperator"));
        }
        
        if (conditions == null || conditions.isEmpty()) {
            errors.add(new ValidationResult.ValidationError("MISSING_CONDITIONS", 
                "At least one condition is required", "conditions"));
        } else {
            for (int i = 0; i < conditions.size(); i++) {
                RuleCondition condition = conditions.get(i);
                if (condition == null) {
                    errors.add(new ValidationResult.ValidationError("NULL_CONDITION", 
                        "Condition cannot be null", "conditions[" + i + "]"));
                } else {
                    ValidationResult conditionValidation = condition.validate();
                    if (!conditionValidation.isValid()) {
                        errors.addAll(conditionValidation.getErrors());
                    }
                    warnings.addAll(conditionValidation.getWarnings());
                }
            }
        }
        
        return errors.isEmpty() ? 
            (warnings.isEmpty() ? ValidationResult.success() : ValidationResult.successWithWarnings(warnings)) :
            ValidationResult.failure(errors, warnings);
    }
    
    @Override
    public RuleCondition copy() {
        List<RuleCondition> copiedConditions = new ArrayList<>();
        for (RuleCondition condition : conditions) {
            if (condition != null) {
                copiedConditions.add(condition.copy());
            }
        }
        
        CompositeCondition copy = new CompositeCondition(logicalOperator, copiedConditions);
        copy.setDescription(getDescription());
        
        if (getMetadata() != null) {
            copy.setMetadata(new java.util.HashMap<>(getMetadata()));
        }
        
        return copy;
    }
    
    @Override
    public String toHumanReadable() {
        if (conditions.isEmpty()) {
            return "()";
        }
        
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) {
                sb.append(" ").append(logicalOperator.getSymbol()).append(" ");
            }
            sb.append(conditions.get(i).toHumanReadable());
        }
        sb.append(")");
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("CompositeCondition{operator=%s, conditions=%d}", logicalOperator, conditions.size());
    }
}