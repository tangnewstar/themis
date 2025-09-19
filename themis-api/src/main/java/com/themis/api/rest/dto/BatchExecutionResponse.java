package com.themis.api.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.themis.core.engine.BatchExecutionResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * Response DTO for batch execution.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchExecutionResponse {
    
    private String batchId;
    private boolean overallSuccess;
    private int totalRules;
    private int successfulRules;
    private int failedRules;
    private double successRate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    
    private long totalExecutionTimeMillis;
    
    private List<RuleExecutionResponse> results;
    
    private Map<String, Object> batchOutputs;
    
    private Map<String, Object> aggregatedMetrics;
    
    public static BatchExecutionResponse fromResult(BatchExecutionResult result) {
        BatchExecutionResponse response = new BatchExecutionResponse();
        response.setBatchId(result.getBatchId());
        response.setOverallSuccess(result.isOverallSuccess());
        response.setTotalRules(result.getTotalCount());
        response.setSuccessfulRules(result.getSuccessCount());
        response.setFailedRules(result.getFailureCount());
        response.setSuccessRate(result.getSuccessRate());
        response.setStartTime(result.getStartTime());
        response.setEndTime(result.getEndTime());
        response.setBatchOutputs(result.getBatchOutputs());
        
        if (result.getTotalExecutionTime() != null) {
            response.setTotalExecutionTimeMillis(result.getTotalExecutionTime().toMillis());
        }
        
        // Convert individual results
        List<RuleExecutionResponse> responseResults = result.getResults().stream()
            .map(RuleExecutionResponse::fromResult)
            .collect(Collectors.toList());
        response.setResults(responseResults);
        
        return response;
    }
    
    // Getters and Setters
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    
    public boolean isOverallSuccess() { return overallSuccess; }
    public void setOverallSuccess(boolean overallSuccess) { this.overallSuccess = overallSuccess; }
    
    public int getTotalRules() { return totalRules; }
    public void setTotalRules(int totalRules) { this.totalRules = totalRules; }
    
    public int getSuccessfulRules() { return successfulRules; }
    public void setSuccessfulRules(int successfulRules) { this.successfulRules = successfulRules; }
    
    public int getFailedRules() { return failedRules; }
    public void setFailedRules(int failedRules) { this.failedRules = failedRules; }
    
    public double getSuccessRate() { return successRate; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public long getTotalExecutionTimeMillis() { return totalExecutionTimeMillis; }
    public void setTotalExecutionTimeMillis(long totalExecutionTimeMillis) { this.totalExecutionTimeMillis = totalExecutionTimeMillis; }
    
    public List<RuleExecutionResponse> getResults() { return results; }
    public void setResults(List<RuleExecutionResponse> results) { this.results = results; }
    
    public Map<String, Object> getBatchOutputs() { return batchOutputs; }
    public void setBatchOutputs(Map<String, Object> batchOutputs) { this.batchOutputs = batchOutputs; }
    
    public Map<String, Object> getAggregatedMetrics() { return aggregatedMetrics; }
    public void setAggregatedMetrics(Map<String, Object> aggregatedMetrics) { this.aggregatedMetrics = aggregatedMetrics; }
}
