package com.themis.orchestration.workflow;

import java.util.Map;

/**
 * Workflow execution settings.
 */
public class WorkflowSettings {
    
    private boolean failFast = true;
    private boolean enableLogging = true;
    private boolean enableMetrics = true;
    private long defaultTimeoutMillis = 300000; // 5 minutes
    private int maxParallelSteps = 10;
    private Map<String, Object> customSettings;
    
    // Getters and Setters
    public boolean isFailFast() { return failFast; }
    public void setFailFast(boolean failFast) { this.failFast = failFast; }
    
    public boolean isEnableLogging() { return enableLogging; }
    public void setEnableLogging(boolean enableLogging) { this.enableLogging = enableLogging; }
    
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    
    public long getDefaultTimeoutMillis() { return defaultTimeoutMillis; }
    public void setDefaultTimeoutMillis(long defaultTimeoutMillis) { this.defaultTimeoutMillis = defaultTimeoutMillis; }
    
    public int getMaxParallelSteps() { return maxParallelSteps; }
    public void setMaxParallelSteps(int maxParallelSteps) { this.maxParallelSteps = maxParallelSteps; }
    
    public Map<String, Object> getCustomSettings() { return customSettings; }
    public void setCustomSettings(Map<String, Object> customSettings) { this.customSettings = customSettings; }
}