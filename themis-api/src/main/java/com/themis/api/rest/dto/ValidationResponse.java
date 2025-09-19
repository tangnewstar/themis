package com.themis.api.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.themis.core.model.ValidationResult;

import java.util.List;
import java.util.stream.Collectors; /**
 * Response DTO for validation results.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationResponse {
    
    private boolean valid;
    private List<ValidationErrorDto> errors;
    private List<ValidationWarningDto> warnings;
    
    public static ValidationResponse fromResult(ValidationResult result) {
        ValidationResponse response = new ValidationResponse();
        response.setValid(result.isValid());
        
        // Convert errors
        if (result.hasErrors()) {
            List<ValidationErrorDto> errorDtos = result.getErrors().stream()
                .map(error -> new ValidationErrorDto(error.getCode(), error.getMessage(), error.getField()))
                .collect(Collectors.toList());
            response.setErrors(errorDtos);
        }
        
        // Convert warnings
        if (result.hasWarnings()) {
            List<ValidationWarningDto> warningDtos = result.getWarnings().stream()
                .map(warning -> new ValidationWarningDto(warning.getCode(), warning.getMessage(), warning.getField()))
                .collect(Collectors.toList());
            response.setWarnings(warningDtos);
        }
        
        return response;
    }
    
    // Getters and Setters
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    
    public List<ValidationErrorDto> getErrors() { return errors; }
    public void setErrors(List<ValidationErrorDto> errors) { this.errors = errors; }
    
    public List<ValidationWarningDto> getWarnings() { return warnings; }
    public void setWarnings(List<ValidationWarningDto> warnings) { this.warnings = warnings; }
    
    /**
     * DTO for validation errors.
     */
    public static class ValidationErrorDto {
        private String code;
        private String message;
        private String field;
        
        public ValidationErrorDto(String code, String message, String field) {
            this.code = code;
            this.message = message;
            this.field = field;
        }
        
        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
    }
    
    /**
     * DTO for validation warnings.
     */
    public static class ValidationWarningDto {
        private String code;
        private String message;
        private String field;
        
        public ValidationWarningDto(String code, String message, String field) {
            this.code = code;
            this.message = message;
            this.field = field;
        }
        
        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
    }
}
