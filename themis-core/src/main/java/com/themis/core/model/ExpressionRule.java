package com.themis.core.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Expression rule implementation for simple condition-action rules.
 * Example: if age > 18 then eligible = true
 */
@JsonTypeName("EXPRESSION")
public class ExpressionRule extends RuleDefinition {
    
    @Valid
    @NotNull(message = "Condition cannot be null")
    private RuleCondition condition;
    
    @Valid
    @NotNull(message = "Actions cannot be null")
    private List<RuleAction> actions = new ArrayList<>();
    
    // Default constructor for Jackson
    public ExpressionRule() {
        super();
        setRuleType(RuleType.EXPRESSION);
    }
    
    public ExpressionRule(String ruleId, String ruleName) {
        super(ruleId, ruleName, RuleType.EXPRESSION);
    }
    
    public ExpressionRule(String ruleId, String ruleName, RuleCondition condition, List<RuleAction> actions) {
        super(ruleId, ruleName, RuleType.EXPRESSION);
        this.condition = condition;
        this.actions = actions != null ? actions : new ArrayList<>();
    }
    
    // Getters and Setters
    public RuleCondition getCondition() {
        return condition;
    }
    
    public void setCondition(RuleCondition condition) {
        this.condition = condition;
    }
    
    public List<RuleAction> getActions() {
        return actions;
    }
    
    /**
     * Gets the first action as a string representation (for backward compatibility with tests).
     */
    public String getAction() {
        if (actions != null && !actions.isEmpty()) {
            RuleAction firstAction = actions.get(0);
            return firstAction.toHumanReadable();
        }
        return null;
    }
    
    public void setActions(List<RuleAction> actions) {
        this.actions = actions != null ? actions : new ArrayList<>();
    }
    
    /**
     * Adds an action to the rule.
     */
    public void addAction(RuleAction action) {
        if (action != null) {
            this.actions.add(action);
        }
    }
    
    /**
     * Removes an action from the rule.
     */
    public boolean removeAction(RuleAction action) {
        return this.actions.remove(action);
    }
    
    @Override
    public ValidationResult validate() {
        List<ValidationResult.ValidationError> errors = new ArrayList<>();
        List<ValidationResult.ValidationWarning> warnings = new ArrayList<>();
        
        // Validate basic rule definition
        if (getRuleId() == null || getRuleId().trim().isEmpty()) {
            errors.add(new ValidationResult.ValidationError("MISSING_RULE_ID", "Rule ID is required", "ruleId"));
        }
        
        if (getRuleName() == null || getRuleName().trim().isEmpty()) {
            errors.add(new ValidationResult.ValidationError("MISSING_RULE_NAME", "Rule name is required", "ruleName"));
        }
        
        // Validate condition
        if (condition == null) {
            errors.add(new ValidationResult.ValidationError("MISSING_CONDITION", "Condition is required for expression rules", "condition"));
        } else {
            ValidationResult conditionValidation = condition.validate();
            if (!conditionValidation.isValid()) {
                errors.addAll(conditionValidation.getErrors());
            }
            warnings.addAll(conditionValidation.getWarnings());
        }
        
        // Validate actions
        if (actions == null || actions.isEmpty()) {
            warnings.add(new ValidationResult.ValidationWarning("NO_ACTIONS", "Rule has no actions defined", "actions"));
        } else {
            for (int i = 0; i < actions.size(); i++) {
                RuleAction action = actions.get(i);
                if (action == null) {
                    errors.add(new ValidationResult.ValidationError("NULL_ACTION", "Action cannot be null", "actions[" + i + "]"));
                } else {
                    ValidationResult actionValidation = action.validate();
                    if (!actionValidation.isValid()) {
                        errors.addAll(actionValidation.getErrors());
                    }
                    warnings.addAll(actionValidation.getWarnings());
                }
            }
        }
        
        return errors.isEmpty() ? 
            (warnings.isEmpty() ? ValidationResult.success() : ValidationResult.successWithWarnings(warnings)) :
            ValidationResult.failure(errors, warnings);
    }
    
    @Override
    public RuleDefinition copy() {
        ExpressionRule copy = new ExpressionRule(getRuleId(), getRuleName());
        
        // Copy basic properties
        copy.setDescription(getDescription());
        copy.setStatus(getStatus());
        copy.setVersion(getVersion());
        copy.setCreatedBy(getCreatedBy());
        copy.setUpdatedBy(getUpdatedBy());
        copy.setPriority(getPriority());
        
        if (getMetadata() != null) {
            copy.setMetadata(new java.util.HashMap<>(getMetadata()));
        }
        
        if (getTags() != null) {
            copy.setTags(new java.util.HashMap<>(getTags()));
        }
        
        // Copy condition
        if (condition != null) {
            copy.setCondition(condition.copy());
        }
        
        // Copy actions
        if (actions != null) {
            List<RuleAction> copiedActions = new ArrayList<>();
            for (RuleAction action : actions) {
                if (action != null) {
                    copiedActions.add(action.copy());
                }
            }
            copy.setActions(copiedActions);
        }
        
        return copy;
    }
    
    @Override
    public String toString() {
        return String.format("ExpressionRule{ruleId='%s', condition=%s, actions=%d}", 
                           getRuleId(), condition, actions.size());
    }
}