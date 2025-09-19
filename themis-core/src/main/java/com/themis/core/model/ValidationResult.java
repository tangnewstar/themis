package com.themis.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the result of a validation operation.
 * Contains information about validation success/failure and any error messages.
 */
public class ValidationResult {
    
    private final boolean valid;
    private final List<ValidationError> errors;
    private final List<ValidationWarning> warnings;
    
    private ValidationResult(boolean valid, List<ValidationError> errors, List<ValidationWarning> warnings) {
        this.valid = valid;
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
    }
    
    /**
     * Creates a successful validation result.
     */
    public static ValidationResult success() {
        return new ValidationResult(true, null, null);
    }
    
    /**
     * Creates a successful validation result with warnings.
     */
    public static ValidationResult successWithWarnings(List<ValidationWarning> warnings) {
        return new ValidationResult(true, null, warnings);
    }
    
    /**
     * Creates a failed validation result with errors.
     */
    public static ValidationResult failure(List<ValidationError> errors) {
        return new ValidationResult(false, errors, null);
    }
    
    /**
     * Creates a failed validation result with errors and warnings.
     */
    public static ValidationResult failure(List<ValidationError> errors, List<ValidationWarning> warnings) {
        return new ValidationResult(false, errors, warnings);
    }
    
    /**
     * Creates a failed validation result with a single error.
     */
    public static ValidationResult failure(String errorMessage) {
        List<ValidationError> errors = new ArrayList<>();
        errors.add(new ValidationError("VALIDATION_ERROR", errorMessage, null));
        return new ValidationResult(false, errors, null);
    }
    
    /**
     * Creates a failed validation result with a single error and field.
     */
    public static ValidationResult failure(String errorCode, String errorMessage, String field) {
        List<ValidationError> errors = new ArrayList<>();
        errors.add(new ValidationError(errorCode, errorMessage, field));
        return new ValidationResult(false, errors, null);
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    public List<ValidationError> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    
    public List<ValidationWarning> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
    
    /**
     * Combines this validation result with another.
     * The combined result is valid only if both results are valid.
     */
    public ValidationResult combine(ValidationResult other) {
        if (other == null) {
            return this;
        }
        
        List<ValidationError> combinedErrors = new ArrayList<>(this.errors);
        combinedErrors.addAll(other.errors);
        
        List<ValidationWarning> combinedWarnings = new ArrayList<>(this.warnings);
        combinedWarnings.addAll(other.warnings);
        
        boolean combinedValid = this.valid && other.valid;
        
        return new ValidationResult(combinedValid, combinedErrors, combinedWarnings);
    }
    
    /**
     * Gets the first error message, if any.
     */
    public String getFirstErrorMessage() {
        return errors.isEmpty() ? null : errors.get(0).getMessage();
    }
    
    /**
     * Gets all error messages as a single string.
     */
    public String getAllErrorMessages() {
        if (errors.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < errors.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(errors.get(i).getMessage());
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("ValidationResult{valid=%s, errors=%d, warnings=%d}", 
                           valid, errors.size(), warnings.size());
    }
    
    /**
     * Represents a validation error.
     */
    public static class ValidationError {
        private final String code;
        private final String message;
        private final String field;
        
        public ValidationError(String code, String message, String field) {
            this.code = code;
            this.message = message;
            this.field = field;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getField() {
            return field;
        }
        
        @Override
        public String toString() {
            return String.format("ValidationError{code='%s', message='%s', field='%s'}", 
                               code, message, field);
        }
    }
    
    /**
     * Represents a validation warning.
     */
    public static class ValidationWarning {
        private final String code;
        private final String message;
        private final String field;
        
        public ValidationWarning(String code, String message, String field) {
            this.code = code;
            this.message = message;
            this.field = field;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getField() {
            return field;
        }
        
        @Override
        public String toString() {
            return String.format("ValidationWarning{code='%s', message='%s', field='%s'}", 
                               code, message, field);
        }
    }
}