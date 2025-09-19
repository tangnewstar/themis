package com.themis.orchestration.workflow;

/**
 * Orchestrator capabilities information.
 */
public class OrchestratorCapabilities {
    
    private final String orchestratorName;
    private final String version;
    private final boolean supportsParallelExecution;
    private final boolean supportsConditionalBranching;
    private final boolean supportsLooping;
    private final boolean supportsAsyncExecution;
    private final boolean supportsHumanTasks;
    private final int maxWorkflowSteps;
    private final int maxParallelBranches;
    
    public OrchestratorCapabilities(String orchestratorName, String version, 
                                  boolean supportsParallelExecution, boolean supportsConditionalBranching,
                                  boolean supportsLooping, boolean supportsAsyncExecution,
                                  boolean supportsHumanTasks, int maxWorkflowSteps, int maxParallelBranches) {
        this.orchestratorName = orchestratorName;
        this.version = version;
        this.supportsParallelExecution = supportsParallelExecution;
        this.supportsConditionalBranching = supportsConditionalBranching;
        this.supportsLooping = supportsLooping;
        this.supportsAsyncExecution = supportsAsyncExecution;
        this.supportsHumanTasks = supportsHumanTasks;
        this.maxWorkflowSteps = maxWorkflowSteps;
        this.maxParallelBranches = maxParallelBranches;
    }
    
    // Getters
    public String getOrchestratorName() { return orchestratorName; }
    public String getVersion() { return version; }
    public boolean supportsParallelExecution() { return supportsParallelExecution; }
    public boolean supportsConditionalBranching() { return supportsConditionalBranching; }
    public boolean supportsLooping() { return supportsLooping; }
    public boolean supportsAsyncExecution() { return supportsAsyncExecution; }
    public boolean supportsHumanTasks() { return supportsHumanTasks; }
    public int getMaxWorkflowSteps() { return maxWorkflowSteps; }
    public int getMaxParallelBranches() { return maxParallelBranches; }
}