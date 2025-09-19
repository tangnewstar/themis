package com.themis.api.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.themis.core.engine.RuleExecutionMode;
import com.themis.core.engine.RuleExecutionResult;
import com.themis.core.engine.BatchExecutionResult;
import com.themis.core.model.ValidationResult;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Request DTO for rule execution.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleExecutionRequest {
    
    @NotBlank(message = "Rule ID is required")
    private String ruleId;
    
    @NotNull(message = "Input data is required")
    private Map<String, Object> inputData;
    
    private RuleExecutionMode executionMode;
    
    private Map<String, Object> executionSettings;
    
    // Getters and Setters
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    
    public Map<String, Object> getInputData() { return inputData; }
    public void setInputData(Map<String, Object> inputData) { this.inputData = inputData; }
    
    public RuleExecutionMode getExecutionMode() { return executionMode; }
    public void setExecutionMode(RuleExecutionMode executionMode) { this.executionMode = executionMode; }
    
    public Map<String, Object> getExecutionSettings() { return executionSettings; }
    public void setExecutionSettings(Map<String, Object> executionSettings) { this.executionSettings = executionSettings; }
}

