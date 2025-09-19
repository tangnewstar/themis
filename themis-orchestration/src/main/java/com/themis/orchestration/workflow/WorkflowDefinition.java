package com.themis.orchestration.workflow;

import java.util.List;
import java.util.Map;
/**
 * Represents a workflow definition containing multiple steps and rules.
 */
public class WorkflowDefinition {
    
    private String workflowId;
    private String workflowName;
    private String description;
    private String version = "1.0.0";
    private List<WorkflowStep> steps;
    private Map<String, Object> globalVariables;
    private WorkflowSettings settings;
    
    // Constructors
    public WorkflowDefinition() {}
    
    public WorkflowDefinition(String workflowId, String workflowName) {
        this.workflowId = workflowId;
        this.workflowName = workflowName;
    }
    
    // Getters and Setters
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    
    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }
    
    /**
     * Alias for getWorkflowId() for backward compatibility.
     */
    public String getId() { return workflowId; }
    
    /**
     * Alias for getWorkflowName() for backward compatibility.
     */
    public String getName() { return workflowName; }
    
    /**
     * Alias methods for backward compatibility.
     */
    public void setId(String id) { this.workflowId = id; }
    public void setName(String name) { this.workflowName = name; }
    public void setMetadata(Map<String, Object> metadata) { this.globalVariables = metadata; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public List<WorkflowStep> getSteps() { return steps; }
    public void setSteps(List<WorkflowStep> steps) { this.steps = steps; }
    
    public Map<String, Object> getGlobalVariables() { return globalVariables; }
    public void setGlobalVariables(Map<String, Object> globalVariables) { this.globalVariables = globalVariables; }
    
    public WorkflowSettings getSettings() { return settings; }
    public void setSettings(WorkflowSettings settings) { this.settings = settings; }
}
