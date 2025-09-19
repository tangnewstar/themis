package com.themis.core.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Branching rule implementation for multi-condition decision trees.
 * Supports multiple condition-action branches with else logic.
 */
@JsonTypeName("BRANCHING")
public class BranchingRule extends RuleDefinition {
    
    @Valid
    @NotNull(message = "Branches cannot be null")
    private List<RuleBranch> branches = new ArrayList<>();
    
    @Valid
    private List<RuleAction> elseActions = new ArrayList<>();
    
    // Default constructor for Jackson
    public BranchingRule() {
        super();
        setRuleType(RuleType.BRANCHING);
    }
    
    public BranchingRule(String ruleId, String ruleName) {
        super(ruleId, ruleName, RuleType.BRANCHING);
    }
    
    // Getters and Setters
    public List<RuleBranch> getBranches() { return branches; }
    public void setBranches(List<RuleBranch> branches) { 
        this.branches = branches != null ? branches : new ArrayList<>(); 
    }
    
    public List<RuleAction> getElseActions() { return elseActions; }
    public void setElseActions(List<RuleAction> elseActions) { 
        this.elseActions = elseActions != null ? elseActions : new ArrayList<>(); 
    }
    
    public void addBranch(RuleBranch branch) {
        if (branch != null) {
            this.branches.add(branch);
        }
    }
    
    public void addElseAction(RuleAction action) {
        if (action != null) {
            this.elseActions.add(action);
        }
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
        
        // Validate branches
        if (branches == null || branches.isEmpty()) {
            errors.add(new ValidationResult.ValidationError("MISSING_BRANCHES", 
                "At least one branch is required", "branches"));
        } else {
            for (int i = 0; i < branches.size(); i++) {
                RuleBranch branch = branches.get(i);
                if (branch == null) {
                    errors.add(new ValidationResult.ValidationError("NULL_BRANCH", 
                        "Branch cannot be null", "branches[" + i + "]"));
                } else {
                    ValidationResult branchValidation = branch.validate();
                    if (!branchValidation.isValid()) {
                        errors.addAll(branchValidation.getErrors());
                    }
                    warnings.addAll(branchValidation.getWarnings());
                }
            }
        }
        
        // Validate else actions
        for (int i = 0; i < elseActions.size(); i++) {
            RuleAction action = elseActions.get(i);
            if (action == null) {
                errors.add(new ValidationResult.ValidationError("NULL_ELSE_ACTION", 
                    "Else action cannot be null", "elseActions[" + i + "]"));
            } else {
                ValidationResult actionValidation = action.validate();
                if (!actionValidation.isValid()) {
                    errors.addAll(actionValidation.getErrors());
                }
                warnings.addAll(actionValidation.getWarnings());
            }
        }
        
        return errors.isEmpty() ? 
            (warnings.isEmpty() ? ValidationResult.success() : ValidationResult.successWithWarnings(warnings)) :
            ValidationResult.failure(errors, warnings);
    }
    
    @Override
    public RuleDefinition copy() {
        BranchingRule copy = new BranchingRule(getRuleId(), getRuleName());
        
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
        
        // Copy branches
        List<RuleBranch> copiedBranches = new ArrayList<>();
        for (RuleBranch branch : branches) {
            if (branch != null) {
                copiedBranches.add(branch.copy());
            }
        }
        copy.setBranches(copiedBranches);
        
        // Copy else actions
        List<RuleAction> copiedElseActions = new ArrayList<>();
        for (RuleAction action : elseActions) {
            if (action != null) {
                copiedElseActions.add(action.copy());
            }
        }
        copy.setElseActions(copiedElseActions);
        
        return copy;
    }
    
    @Override
    public String toString() {
        return String.format("BranchingRule{ruleId='%s', branches=%d, elseActions=%d}", 
                           getRuleId(), branches.size(), elseActions.size());
    }
    
    /**
     * Represents a single branch in a branching rule.
     */
    public static class RuleBranch {
        
        @Valid
        @NotNull(message = "Condition cannot be null")
        private RuleCondition condition;
        
        @Valid
        @NotNull(message = "Actions cannot be null")
        private List<RuleAction> actions = new ArrayList<>();
        
        private String description;
        private Integer priority = 100;
        
        // Default constructor
        public RuleBranch() {}
        
        public RuleBranch(RuleCondition condition, List<RuleAction> actions) {
            this.condition = condition;
            this.actions = actions != null ? actions : new ArrayList<>();
        }
        
        // Getters and Setters
        public RuleCondition getCondition() { return condition; }
        public void setCondition(RuleCondition condition) { this.condition = condition; }
        
        public List<RuleAction> getActions() { return actions; }
        public void setActions(List<RuleAction> actions) { 
            this.actions = actions != null ? actions : new ArrayList<>(); 
        }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
        
        public void addAction(RuleAction action) {
            if (action != null) {
                this.actions.add(action);
            }
        }
        
        public ValidationResult validate() {
            List<ValidationResult.ValidationError> errors = new ArrayList<>();
            List<ValidationResult.ValidationWarning> warnings = new ArrayList<>();
            
            if (condition == null) {
                errors.add(new ValidationResult.ValidationError("MISSING_CONDITION", 
                    "Branch condition is required", "condition"));
            } else {
                ValidationResult conditionValidation = condition.validate();
                if (!conditionValidation.isValid()) {
                    errors.addAll(conditionValidation.getErrors());
                }
                warnings.addAll(conditionValidation.getWarnings());
            }
            
            if (actions == null || actions.isEmpty()) {
                warnings.add(new ValidationResult.ValidationWarning("NO_ACTIONS", 
                    "Branch has no actions defined", "actions"));
            } else {
                for (int i = 0; i < actions.size(); i++) {
                    RuleAction action = actions.get(i);
                    if (action == null) {
                        errors.add(new ValidationResult.ValidationError("NULL_ACTION", 
                            "Action cannot be null", "actions[" + i + "]"));
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
        
        public RuleBranch copy() {
            RuleBranch copy = new RuleBranch();
            copy.setDescription(description);
            copy.setPriority(priority);
            
            if (condition != null) {
                copy.setCondition(condition.copy());
            }
            
            List<RuleAction> copiedActions = new ArrayList<>();
            for (RuleAction action : actions) {
                if (action != null) {
                    copiedActions.add(action.copy());
                }
            }
            copy.setActions(copiedActions);
            
            return copy;
        }
        
        @Override
        public String toString() {
            return String.format("RuleBranch{condition=%s, actions=%d}", condition, actions.size());
        }
    }
}