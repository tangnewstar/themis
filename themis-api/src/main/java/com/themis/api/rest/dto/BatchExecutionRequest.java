package com.themis.api.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.themis.core.engine.RuleExecutionMode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
/**
 * Request DTO for batch execution.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchExecutionRequest {
    
    @NotEmpty(message = "Rule IDs list cannot be empty")
    private List<String> ruleIds;
    
    @NotNull(message = "Input data is required")
    private Map<String, Object> inputData;
    
    private RuleExecutionMode executionMode;
    
    private Map<String, Object> executionSettings;
    
    // Getters and Setters
    public List<String> getRuleIds() { return ruleIds; }
    public void setRuleIds(List<String> ruleIds) { this.ruleIds = ruleIds; }
    
    public Map<String, Object> getInputData() { return inputData; }
    public void setInputData(Map<String, Object> inputData) { this.inputData = inputData; }
    
    public RuleExecutionMode getExecutionMode() { return executionMode; }
    public void setExecutionMode(RuleExecutionMode executionMode) { this.executionMode = executionMode; }
    
    public Map<String, Object> getExecutionSettings() { return executionSettings; }
    public void setExecutionSettings(Map<String, Object> executionSettings) { this.executionSettings = executionSettings; }
}
