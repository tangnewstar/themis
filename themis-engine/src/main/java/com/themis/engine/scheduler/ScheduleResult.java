package com.themis.engine.scheduler;

import com.themis.core.engine.RuleExecutionResult;

import java.util.List;

/**
 * 调度执行结果
 */
public class ScheduleResult {
    
    private final RuleSetScheduler.ExecutionMode executionMode;
    private final List<RuleExecutionResult> results;
    private final long executionTimeMs;
    
    public ScheduleResult(RuleSetScheduler.ExecutionMode executionMode,
                         List<RuleExecutionResult> results,
                         long executionTimeMs) {
        this.executionMode = executionMode;
        this.results = results;
        this.executionTimeMs = executionTimeMs;
    }
    
    public RuleSetScheduler.ExecutionMode getExecutionMode() {
        return executionMode;
    }
    
    public List<RuleExecutionResult> getResults() {
        return results;
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public int getSuccessCount() {
        return (int) results.stream().filter(RuleExecutionResult::isSuccess).count();
    }
    
    public int getFailureCount() {
        return (int) results.stream().filter(r -> !r.isSuccess()).count();
    }
}
