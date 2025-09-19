package com.themis.api.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.themis.core.engine.RuleExecutionResult;

import java.time.LocalDateTime;
import java.util.Map; /**
 * Response DTO for rule execution.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleExecutionResponse {
    
    private String ruleId;
    private String ruleName;
    private boolean success;
    private Map<String, Object> outputs;
    private String errorMessage;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    
    private long executionTimeMillis;
    
    private Map<String, Object> metrics;
    
    private Map<String, Object> debugInfo;
    
    public static RuleExecutionResponse fromResult(RuleExecutionResult result) {
        RuleExecutionResponse response = new RuleExecutionResponse();
        response.setRuleId(result.getRuleId());
        response.setRuleName(result.getRuleName());
        response.setSuccess(result.isSuccess());
        response.setOutputs(result.getOutputs());
        response.setErrorMessage(result.getErrorMessage());
        response.setStartTime(result.getStartTime());
        response.setEndTime(result.getEndTime());
        response.setExecutionTimeMillis(result.getExecutionTimeMillis());
        response.setDebugInfo(result.getDebugInfo());
        
        // Convert metrics if available
        if (result.getMetrics() != null) {
            Map<String, Object> metricsMap = new java.util.HashMap<>();
            metricsMap.put("executionTime", result.getMetrics().getExecutionTimeMillis());
            metricsMap.put("conditionsEvaluated", result.getMetrics().getConditionsEvaluated());
            metricsMap.put("actionsExecuted", result.getMetrics().getActionsExecuted());
            metricsMap.put("cacheHitRatio", result.getMetrics().getCacheHitRatio());
            response.setMetrics(metricsMap);
        }
        
        return response;
    }
    
    // Getters and Setters
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public Map<String, Object> getOutputs() { return outputs; }
    public void setOutputs(Map<String, Object> outputs) { this.outputs = outputs; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public long getExecutionTimeMillis() { return executionTimeMillis; }
    public void setExecutionTimeMillis(long executionTimeMillis) { this.executionTimeMillis = executionTimeMillis; }
    
    public Map<String, Object> getMetrics() { return metrics; }
    public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }
    
    public Map<String, Object> getDebugInfo() { return debugInfo; }
    public void setDebugInfo(Map<String, Object> debugInfo) { this.debugInfo = debugInfo; }
}
