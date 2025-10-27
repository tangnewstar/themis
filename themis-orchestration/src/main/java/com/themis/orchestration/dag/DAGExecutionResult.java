package com.themis.orchestration.dag;

import java.util.Map;

/**
 * DAG执行结果
 */
public class DAGExecutionResult {
    
    private boolean success;
    private Map<String, Object> results;
    private String errorMessage;
    private long executionTimeMs;
    
    private DAGExecutionResult() {
    }
    
    public static DAGExecutionResult success(Map<String, Object> results, long executionTimeMs) {
        DAGExecutionResult result = new DAGExecutionResult();
        result.success = true;
        result.results = results;
        result.executionTimeMs = executionTimeMs;
        return result;
    }
    
    public static DAGExecutionResult failure(String errorMessage, long executionTimeMs) {
        DAGExecutionResult result = new DAGExecutionResult();
        result.success = false;
        result.errorMessage = errorMessage;
        result.executionTimeMs = executionTimeMs;
        return result;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public Map<String, Object> getResults() {
        return results;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
}
