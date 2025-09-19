package com.themis.core.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Formula rule implementation for mathematical calculations.
 * Example: score = weight1 * factor1 + weight2 * factor2
 */
@JsonTypeName("FORMULA")
public class FormulaRule extends RuleDefinition {
    
    @NotBlank(message = "Formula expression cannot be blank")
    private String formula;
    
    @NotBlank(message = "Result field cannot be blank")
    private String resultField;
    
    private Map<String, Object> variables;
    
    private Map<String, String> functions;
    
    // Default constructor for Jackson
    public FormulaRule() {
        super();
        setRuleType(RuleType.FORMULA);
    }
    
    public FormulaRule(String ruleId, String ruleName) {
        super(ruleId, ruleName, RuleType.FORMULA);
    }
    
    public FormulaRule(String ruleId, String ruleName, String formula, String resultField) {
        super(ruleId, ruleName, RuleType.FORMULA);
        this.formula = formula;
        this.resultField = resultField;
    }
    
    // Getters and Setters
    public String getFormula() { return formula; }
    public void setFormula(String formula) { this.formula = formula; }
    
    public String getResultField() { return resultField; }
    public void setResultField(String resultField) { this.resultField = resultField; }
    
    /**
     * Gets the output variable (alias for resultField for backward compatibility with tests).
     */
    public String getOutputVariable() {
        return resultField;
    }
    
    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
    
    public Map<String, String> getFunctions() { return functions; }
    public void setFunctions(Map<String, String> functions) { this.functions = functions; }
    
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
        
        // Validate formula
        if (formula == null || formula.trim().isEmpty()) {
            errors.add(new ValidationResult.ValidationError("MISSING_FORMULA", 
                "Formula expression is required", "formula"));
        } else {
            // Basic formula validation
            if (!isValidFormula(formula)) {
                errors.add(new ValidationResult.ValidationError("INVALID_FORMULA", 
                    "Formula syntax is invalid", "formula"));
            }
        }
        
        // Validate result field
        if (resultField == null || resultField.trim().isEmpty()) {
            errors.add(new ValidationResult.ValidationError("MISSING_RESULT_FIELD", 
                "Result field is required", "resultField"));
        }
        
        return errors.isEmpty() ? 
            (warnings.isEmpty() ? ValidationResult.success() : ValidationResult.successWithWarnings(warnings)) :
            ValidationResult.failure(errors, warnings);
    }
    
    @Override
    public RuleDefinition copy() {
        FormulaRule copy = new FormulaRule(getRuleId(), getRuleName(), formula, resultField);
        
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
        
        if (variables != null) {
            copy.setVariables(new java.util.HashMap<>(variables));
        }
        
        if (functions != null) {
            copy.setFunctions(new java.util.HashMap<>(functions));
        }
        
        return copy;
    }
    
    /**
     * Basic validation of formula syntax.
     */
    private boolean isValidFormula(String formula) {
        if (formula == null || formula.trim().isEmpty()) {
            return false;
        }
        
        // Check for balanced parentheses
        int openParens = 0;
        for (char c : formula.toCharArray()) {
            if (c == '(') {
                openParens++;
            } else if (c == ')') {
                openParens--;
                if (openParens < 0) {
                    return false;
                }
            }
        }
        
        return openParens == 0;
    }
    
    @Override
    public String toString() {
        return String.format("FormulaRule{ruleId='%s', formula='%s', resultField='%s'}", 
                           getRuleId(), formula, resultField);
    }
}