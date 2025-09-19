package com.themis.orchestration.workflow;

import com.themis.core.model.ValidationResult;
import java.util.List;

/**
 * Represents the result of workflow validation.
 */
public class WorkflowValidationResult {
    
    private final boolean valid;
    private final List<ValidationResult.ValidationError> errors;
    private final List<ValidationResult.ValidationWarning> warnings;
    
    public WorkflowValidationResult(boolean valid, 
                                  List<ValidationResult.ValidationError> errors,
                                  List<ValidationResult.ValidationWarning> warnings) {
        this.valid = valid;
        this.errors = errors != null ? errors : java.util.Collections.emptyList();
        this.warnings = warnings != null ? warnings : java.util.Collections.emptyList();
    }
    
    public static WorkflowValidationResult success() {
        return new WorkflowValidationResult(true, null, null);
    }
    
    public static WorkflowValidationResult successWithWarnings(List<ValidationResult.ValidationWarning> warnings) {
        return new WorkflowValidationResult(true, null, warnings);
    }
    
    public static WorkflowValidationResult failure(List<ValidationResult.ValidationError> errors) {
        return new WorkflowValidationResult(false, errors, null);
    }
    
    public static WorkflowValidationResult failure(List<ValidationResult.ValidationError> errors, 
                                                 List<ValidationResult.ValidationWarning> warnings) {
        return new WorkflowValidationResult(false, errors, warnings);
    }
    
    public boolean isValid() { return valid; }
    public List<ValidationResult.ValidationError> getErrors() { return errors; }
    public List<ValidationResult.ValidationWarning> getWarnings() { return warnings; }
    
    public boolean hasErrors() { return !errors.isEmpty(); }
    public boolean hasWarnings() { return !warnings.isEmpty(); }
    
    public String getFirstErrorMessage() {
        return errors.isEmpty() ? null : errors.get(0).getMessage();
    }
}