package com.themis.orchestration.workflow.builder;

import com.themis.orchestration.workflow.*;

import java.util.*;

/**
 * Builder for creating workflow definitions
 */
public class WorkflowBuilder {
    
    private String id;
    private String name;
    private String description;
    private String version;
    private List<WorkflowStep> steps;
    private Map<String, Object> metadata;
    
    public WorkflowBuilder() {
        this.steps = new ArrayList<>();
        this.metadata = new HashMap<>();
    }
    
    public static WorkflowBuilder create() {
        return new WorkflowBuilder();
    }
    
    public WorkflowBuilder id(String id) {
        this.id = id;
        return this;
    }
    
    public WorkflowBuilder name(String name) {
        this.name = name;
        return this;
    }
    
    public WorkflowBuilder description(String description) {
        this.description = description;
        return this;
    }
    
    public WorkflowBuilder version(String version) {
        this.version = version;
        return this;
    }
    
    public WorkflowBuilder addStep(WorkflowStep step) {
        this.steps.add(step);
        return this;
    }
    
    public WorkflowBuilder addStep(String stepId, String stepName, WorkflowStep.StepType stepType) {
        WorkflowStep step = new WorkflowStep();
        step.setId(stepId);
        step.setName(stepName);
        step.setType(stepType);
        step.setConfiguration(new HashMap<>());
        this.steps.add(step);
        return this;
    }
    
    public StepBuilder addRuleStep(String stepId, String stepName) {
        return new StepBuilder(this, stepId, stepName, WorkflowStep.StepType.RULE_EXECUTION);
    }
    
    public StepBuilder addDecisionStep(String stepId, String stepName) {
        return new StepBuilder(this, stepId, stepName, WorkflowStep.StepType.DECISION);
    }
    
    public StepBuilder addParallelStep(String stepId, String stepName) {
        return new StepBuilder(this, stepId, stepName, WorkflowStep.StepType.PARALLEL);
    }
    
    public StepBuilder addSequentialStep(String stepId, String stepName) {
        return new StepBuilder(this, stepId, stepName, WorkflowStep.StepType.SEQUENTIAL);
    }
    
    public StepBuilder addCustomStep(String stepId, String stepName) {
        return new StepBuilder(this, stepId, stepName, WorkflowStep.StepType.CUSTOM);
    }
    
    public WorkflowBuilder metadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
    
    public WorkflowBuilder metadata(Map<String, Object> metadata) {
        this.metadata.putAll(metadata);
        return this;
    }
    
    public WorkflowDefinition build() {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Workflow ID is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Workflow name is required");
        }
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("Workflow must have at least one step");
        }
        
        WorkflowDefinition workflow = new WorkflowDefinition();
        workflow.setId(id);
        workflow.setName(name);
        workflow.setDescription(description);
        workflow.setVersion(version != null ? version : "1.0");
        workflow.setSteps(new ArrayList<>(steps));
        workflow.setMetadata(new HashMap<>(metadata));
        
        return workflow;
    }
    
    /**
     * Builder for individual workflow steps
     */
    public static class StepBuilder {
        
        private final WorkflowBuilder workflowBuilder;
        private final WorkflowStep step;
        
        public StepBuilder(WorkflowBuilder workflowBuilder, String stepId, String stepName, WorkflowStep.StepType stepType) {
            this.workflowBuilder = workflowBuilder;
            this.step = new WorkflowStep();
            this.step.setId(stepId);
            this.step.setName(stepName);
            this.step.setType(stepType);
            this.step.setConfiguration(new HashMap<>());
            this.step.setDependencies(new ArrayList<>());
        }
        
        public StepBuilder description(String description) {
            this.step.setDescription(description);
            return this;
        }
        
        public StepBuilder condition(String condition) {
            this.step.setCondition(condition);
            return this;
        }
        
        public StepBuilder dependsOn(String... dependencies) {
            this.step.getDependencies().addAll(Arrays.asList(dependencies));
            return this;
        }
        
        public StepBuilder dependsOn(List<String> dependencies) {
            this.step.getDependencies().addAll(dependencies);
            return this;
        }
        
        public StepBuilder outputVariable(String outputVariable) {
            this.step.setOutputVariable(outputVariable);
            return this;
        }
        
        public StepBuilder failOnError(boolean failOnError) {
            this.step.setFailOnError(failOnError);
            return this;
        }
        
        public StepBuilder config(String key, Object value) {
            this.step.getConfiguration().put(key, value);
            return this;
        }
        
        public StepBuilder config(Map<String, Object> configuration) {
            this.step.getConfiguration().putAll(configuration);
            return this;
        }
        
        // Rule execution specific methods
        public StepBuilder ruleId(String ruleId) {
            if (this.step.getType() != WorkflowStep.StepType.RULE_EXECUTION) {
                throw new IllegalStateException("ruleId can only be set for RULE_EXECUTION steps");
            }
            this.step.getConfiguration().put("ruleId", ruleId);
            return this;
        }
        
        // Decision specific methods
        public StepBuilder conditionExpression(String condition) {
            if (this.step.getType() != WorkflowStep.StepType.DECISION) {
                throw new IllegalStateException("conditionExpression can only be set for DECISION steps");
            }
            this.step.getConfiguration().put("condition", condition);
            return this;
        }
        
        // Custom step specific methods
        public StepBuilder handlerClass(String handlerClass) {
            if (this.step.getType() != WorkflowStep.StepType.CUSTOM) {
                throw new IllegalStateException("handlerClass can only be set for CUSTOM steps");
            }
            this.step.getConfiguration().put("handlerClass", handlerClass);
            return this;
        }
        
        // Parallel/Sequential step specific methods
        public StepBuilder subSteps(List<Map<String, Object>> subSteps) {
            if (this.step.getType() != WorkflowStep.StepType.PARALLEL && this.step.getType() != WorkflowStep.StepType.SEQUENTIAL) {
                throw new IllegalStateException("subSteps can only be set for PARALLEL or SEQUENTIAL steps");
            }
            this.step.getConfiguration().put("subSteps", subSteps);
            return this;
        }
        
        public SubStepBuilder addSubStep() {
            if (this.step.getType() != WorkflowStep.StepType.PARALLEL && this.step.getType() != WorkflowStep.StepType.SEQUENTIAL) {
                throw new IllegalStateException("subSteps can only be added to PARALLEL or SEQUENTIAL steps");
            }
            return new SubStepBuilder(this);
        }
        
        public WorkflowBuilder and() {
            this.workflowBuilder.steps.add(this.step);
            return this.workflowBuilder;
        }
        
        public WorkflowDefinition build() {
            this.workflowBuilder.steps.add(this.step);
            return this.workflowBuilder.build();
        }
    }
    
    /**
     * Builder for sub-steps within parallel or sequential steps
     */
    public static class SubStepBuilder {
        
        private final StepBuilder stepBuilder;
        private final Map<String, Object> subStep;
        
        public SubStepBuilder(StepBuilder stepBuilder) {
            this.stepBuilder = stepBuilder;
            this.subStep = new HashMap<>();
        }
        
        public SubStepBuilder type(String type) {
            this.subStep.put("type", type);
            return this;
        }
        
        public SubStepBuilder name(String name) {
            this.subStep.put("name", name);
            return this;
        }
        
        public SubStepBuilder ruleId(String ruleId) {
            this.subStep.put("ruleId", ruleId);
            return this;
        }
        
        public SubStepBuilder config(String key, Object value) {
            this.subStep.put(key, value);
            return this;
        }
        
        @SuppressWarnings("unchecked")
        public StepBuilder and() {
            List<Map<String, Object>> subSteps = (List<Map<String, Object>>) 
                    this.stepBuilder.step.getConfiguration().get("subSteps");
            if (subSteps == null) {
                subSteps = new ArrayList<>();
                this.stepBuilder.step.getConfiguration().put("subSteps", subSteps);
            }
            subSteps.add(this.subStep);
            return this.stepBuilder;
        }
    }
}