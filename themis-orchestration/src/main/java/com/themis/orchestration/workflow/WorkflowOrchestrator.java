package com.themis.orchestration.workflow;

import com.themis.core.engine.RuleContext;
import com.themis.core.model.RuleDefinition;

import java.util.List;
import java.util.Map;

/**
 * Interface for workflow orchestration in the Themis Rule Engine.
 * Manages the execution flow of multiple rules with dependencies and conditions.
 */
public interface WorkflowOrchestrator {
    
    /**
     * Executes a workflow definition with the given context.
     * 
     * @param workflowDefinition The workflow to execute
     * @param context The execution context
     * @return The workflow execution result
     * @throws WorkflowExecutionException if execution fails
     */
    WorkflowExecutionResult execute(WorkflowDefinition workflowDefinition, RuleContext context) 
        throws WorkflowExecutionException;
    
    /**
     * Validates a workflow definition for correctness.
     * 
     * @param workflowDefinition The workflow to validate
     * @return The validation result
     */
    WorkflowValidationResult validate(WorkflowDefinition workflowDefinition);
    
    /**
     * Starts asynchronous execution of a workflow.
     * 
     * @param workflowDefinition The workflow to execute
     * @param context The execution context
     * @return The execution job ID
     * @throws WorkflowExecutionException if execution startup fails
     */
    String executeAsync(WorkflowDefinition workflowDefinition, RuleContext context) 
        throws WorkflowExecutionException;
    
    /**
     * Gets the status of an asynchronous workflow execution.
     * 
     * @param jobId The execution job ID
     * @return The execution status
     */
    WorkflowExecutionStatus getExecutionStatus(String jobId);
    
    /**
     * Cancels an ongoing workflow execution.
     * 
     * @param jobId The execution job ID
     * @return true if cancellation was successful
     */
    boolean cancelExecution(String jobId);
    
    /**
     * Gets the orchestrator capabilities.
     * 
     * @return Orchestrator capabilities
     */
    OrchestratorCapabilities getCapabilities();
}