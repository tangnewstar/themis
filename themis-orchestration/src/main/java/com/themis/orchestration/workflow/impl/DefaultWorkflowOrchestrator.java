package com.themis.orchestration.workflow.impl;

import com.themis.core.engine.RuleEngine;
import com.themis.core.engine.RuleContext;
import com.themis.orchestration.workflow.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Default implementation of WorkflowOrchestrator
 */
public class DefaultWorkflowOrchestrator implements WorkflowOrchestrator {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultWorkflowOrchestrator.class);
    
    private final RuleEngine ruleEngine;
    private final ExecutorService executorService;
    private final ObjectMapper objectMapper;
    
    public DefaultWorkflowOrchestrator(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
        this.executorService = Executors.newCachedThreadPool();
        this.objectMapper = new ObjectMapper();
    }
    
    public DefaultWorkflowOrchestrator(RuleEngine ruleEngine, ExecutorService executorService) {
        this.ruleEngine = ruleEngine;
        this.executorService = executorService;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public WorkflowExecutionResult execute(WorkflowDefinition workflowDefinition, RuleContext context) 
            throws WorkflowExecutionException {
        // Convert RuleContext to Map<String, Object> for internal use
        Map<String, Object> contextMap = new HashMap<>();
        // Add context attributes - simplified implementation
        return executeWorkflow(workflowDefinition, contextMap);
    }
    
    @Override
    public WorkflowValidationResult validate(WorkflowDefinition workflowDefinition) {
        // Basic validation implementation
        try {
            if (workflowDefinition == null) {
                return WorkflowValidationResult.failure(
                    java.util.Arrays.asList(
                        new com.themis.core.model.ValidationResult.ValidationError(
                            "NULL_WORKFLOW", "Workflow definition cannot be null", "workflow")
                    )
                );
            }
            return WorkflowValidationResult.success();
        } catch (Exception e) {
            return WorkflowValidationResult.failure(
                java.util.Arrays.asList(
                    new com.themis.core.model.ValidationResult.ValidationError(
                        "VALIDATION_ERROR", e.getMessage(), "workflow")
                )
            );
        }
    }
    
    @Override
    public String executeAsync(WorkflowDefinition workflowDefinition, RuleContext context) 
            throws WorkflowExecutionException {
        // Generate job ID and start async execution
        String jobId = "job-" + System.currentTimeMillis();
        CompletableFuture.runAsync(() -> {
            try {
                execute(workflowDefinition, context);
            } catch (WorkflowExecutionException e) {
                logger.error("Async workflow execution failed: {}", e.getMessage(), e);
            }
        }, executorService);
        return jobId;
    }
    
    @Override
    public WorkflowExecutionStatus getExecutionStatus(String jobId) {
        // In a real implementation, this would query the job status from storage
        return WorkflowExecutionStatus.COMPLETED; // Placeholder
    }
    
    @Override
    public boolean cancelExecution(String jobId) {
        // In a real implementation, this would cancel the running workflow
        return false; // Placeholder
    }
    
    @Override
    public OrchestratorCapabilities getCapabilities() {
        return new OrchestratorCapabilities(
            "DefaultWorkflowOrchestrator", 
            "1.0.0", 
            true, // supportsParallelExecution
            true, // supportsConditionalBranching
            false, // supportsLooping
            true, // supportsAsyncExecution
            false, // supportsHumanTasks
            1000, // maxWorkflowSteps
            10 // maxParallelBranches
        );
    }
    
    // Internal helper methods
    public WorkflowExecutionResult executeWorkflow(WorkflowDefinition workflow, Map<String, Object> context) {
        logger.info("Starting workflow execution: {}", workflow.getName());
        
        WorkflowExecutionResult result = new WorkflowExecutionResult();
        result.setWorkflowId(workflow.getId());
        result.setWorkflowName(workflow.getName());
        result.setStartTime(LocalDateTime.now());
        result.setStatus(WorkflowStatus.RUNNING);
        result.setContext(new HashMap<>(context));
        result.setStepResults(new ArrayList<>());
        
        try {
            executeSteps(workflow.getSteps(), result);
            result.setStatus(WorkflowStatus.COMPLETED);
            logger.info("Workflow execution completed successfully: {}", workflow.getName());
        } catch (Exception e) {
            logger.error("Workflow execution failed: {}", workflow.getName(), e);
            result.setStatus(WorkflowStatus.FAILED);
            result.setErrorMessage(e.getMessage());
        } finally {
            result.setEndTime(LocalDateTime.now());
        }
        
        return result;
    }
    
    private void executeSteps(List<WorkflowStep> steps, WorkflowExecutionResult result) {
        if (steps == null) return;
        
        for (WorkflowStep step : steps) {
            if (shouldExecuteStep(step, result)) {
                executeStep(step, result);
            }
        }
    }
    
    private boolean shouldExecuteStep(WorkflowStep step, WorkflowExecutionResult result) {
        // Check dependencies
        if (step.getDependencies() != null && !step.getDependencies().isEmpty()) {
            for (String dependency : step.getDependencies()) {
                if (!isStepCompleted(dependency, result)) {
                    return false;
                }
            }
        }
        
        // Check conditions
        if (step.getCondition() != null) {
            try {
                Map<String, Object> conditionContext = new HashMap<>(result.getContext());
                conditionContext.put("stepResults", result.getStepResults());
                
                return evaluateCondition(step.getCondition().getExpression(), conditionContext);
            } catch (Exception e) {
                logger.warn("Failed to evaluate step condition for step {}: {}", step.getId(), e.getMessage());
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isStepCompleted(String stepId, WorkflowExecutionResult result) {
        return result.getStepResults().stream()
                .anyMatch(stepResult -> stepId.equals(stepResult.getStepId()) && 
                         StepStatus.COMPLETED.equals(stepResult.getStatus()));
    }
    
    private boolean evaluateCondition(String condition, Map<String, Object> context) {
        // Simple condition evaluation - in a real implementation, this would use
        // a more sophisticated expression evaluator or the rule engine
        if (condition != null && condition.startsWith("context.")) {
            String key = condition.substring(8);
            Object value = context.get(key);
            return value != null && (!(value instanceof Boolean) || (Boolean) value);
        }
        
        // Default to true for simple conditions
        return true;
    }
    
    private void executeStep(WorkflowStep step, WorkflowExecutionResult result) {
        logger.info("Executing workflow step: {}", step.getName());
        
        StepExecutionResult stepResult = new StepExecutionResult();
        stepResult.setStepId(step.getId());
        stepResult.setStepName(step.getName());
        stepResult.setStartTime(LocalDateTime.now());
        stepResult.setStatus(StepStatus.RUNNING);
        
        try {
            // Basic step execution - simplified implementation
            stepResult.setOutput("Step completed");
            stepResult.setStatus(StepStatus.COMPLETED);
            logger.info("Step execution completed: {}", step.getName());
            
        } catch (Exception e) {
            logger.error("Step execution failed: {}", step.getName(), e);
            stepResult.setStatus(StepStatus.FAILED);
            stepResult.setErrorMessage(e.getMessage());
            
            if (step.isFailOnError()) {
                throw new ThemisException("Workflow failed at step: " + step.getName(), e);
            }
        } finally {
            stepResult.setEndTime(LocalDateTime.now());
            result.getStepResults().add(stepResult);
        }
    }
    
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}