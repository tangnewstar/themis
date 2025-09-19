package com.themis.engine.impl;

import com.themis.core.engine.*;
import com.themis.core.model.*;
import com.themis.engine.evaluator.*;
import com.themis.engine.executor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Default implementation of the rule engine.
 * Provides basic rule execution capabilities with support for all rule types.
 */
public class DefaultRuleEngine implements RuleEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultRuleEngine.class);
    
    private final ConditionEvaluator conditionEvaluator;
    private final ActionExecutor actionExecutor;
    private final FormulaEvaluator formulaEvaluator;
    private final TableEvaluator tableEvaluator;
    private final ScorecardEvaluator scorecardEvaluator;
    
    private ExecutorService executorService;
    private EngineConfiguration configuration;
    private boolean initialized = false;
    
    public DefaultRuleEngine() {
        this.conditionEvaluator = new DefaultConditionEvaluator();
        this.actionExecutor = new DefaultActionExecutor();
        this.formulaEvaluator = new DefaultFormulaEvaluator();
        this.tableEvaluator = new DefaultTableEvaluator();
        this.scorecardEvaluator = new DefaultScorecardEvaluator();
    }
    
    @Override
    public void initialize(EngineConfiguration configuration) {
        this.configuration = configuration;
        this.executorService = Executors.newFixedThreadPool(configuration.getThreadPoolSize());
        this.initialized = true;
        
        logger.info("DefaultRuleEngine initialized with configuration: {}", configuration);
    }
    
    @Override
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        this.initialized = false;
        
        logger.info("DefaultRuleEngine shutdown completed");
    }
    
    @Override
    public RuleExecutionResult execute(RuleContext context, RuleDefinition rule) throws RuleExecutionException {
        if (!initialized) {
            throw new RuleExecutionException("Engine not initialized");
        }
        
        if (context == null) {
            throw new RuleExecutionException("Rule context cannot be null");
        }
        
        if (rule == null) {
            throw new RuleExecutionException("Rule definition cannot be null");
        }
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            logger.debug("Executing rule: {} ({})", rule.getRuleId(), rule.getRuleType());
            
            // Validate rule before execution
            ValidationResult validation = validate(rule);
            if (!validation.isValid()) {
                String errorMsg = "Rule validation failed: " + validation.getFirstErrorMessage();
                logger.error("Rule validation failed for {}: {}", rule.getRuleId(), errorMsg);
                return RuleExecutionResult.failure(rule, errorMsg, startTime, LocalDateTime.now());
            }
            
            Map<String, Object> outputs = new HashMap<>();
            
            // Execute based on rule type
            switch (rule.getRuleType()) {
                case EXPRESSION:
                    outputs = executeExpressionRule(context, (ExpressionRule) rule);
                    break;
                case BRANCHING:
                    outputs = executeBranchingRule(context, (BranchingRule) rule);
                    break;
                case FORMULA:
                    outputs = executeFormulaRule(context, (FormulaRule) rule);
                    break;
                case TABLE:
                    outputs = executeTableRule(context, (RuleTable) rule);
                    break;
                case SCORECARD:
                    outputs = executeScorecardRule(context, (Scorecard) rule);
                    break;
                default:
                    throw new RuleExecutionException(rule.getRuleId(), rule.getRuleName(), 
                        "Unsupported rule type: " + rule.getRuleType());
            }
            
            LocalDateTime endTime = LocalDateTime.now();
            logger.debug("Rule execution completed: {} with {} outputs", rule.getRuleId(), outputs.size());
            
            return RuleExecutionResult.success(rule, outputs, startTime, endTime);
            
        } catch (Exception e) {
            LocalDateTime endTime = LocalDateTime.now();
            logger.error("Rule execution failed for {}: {}", rule.getRuleId(), e.getMessage(), e);
            return RuleExecutionResult.failure(rule, e, startTime, endTime);
        }
    }
    
    @Override
    public BatchExecutionResult executeBatch(RuleContext context, List<RuleDefinition> rules) throws RuleExecutionException {
        if (!initialized) {
            throw new RuleExecutionException("Engine not initialized");
        }
        
        if (context == null) {
            throw new RuleExecutionException("Rule context cannot be null");
        }
        
        if (rules == null || rules.isEmpty()) {
            throw new RuleExecutionException("Rules list cannot be null or empty");
        }
        
        LocalDateTime startTime = LocalDateTime.now();
        String batchId = UUID.randomUUID().toString();
        
        logger.debug("Executing batch of {} rules with ID: {}", rules.size(), batchId);
        
        try {
            List<RuleExecutionResult> results;
            
            if (context.getExecutionMode().allowsParallelExecution()) {
                results = executeRulesInParallel(context, rules);
            } else {
                results = executeRulesSequentially(context, rules);
            }
            
            LocalDateTime endTime = LocalDateTime.now();
            boolean overallSuccess = results.stream().allMatch(RuleExecutionResult::isSuccess);
            
            // Merge outputs from all successful executions
            Map<String, Object> batchOutputs = new HashMap<>();
            for (RuleExecutionResult result : results) {
                if (result.isSuccess() && result.hasOutputs()) {
                    batchOutputs.putAll(result.getOutputs());
                }
            }
            
            BatchExecutionResult batchResult = new BatchExecutionResult.Builder()
                .batchId(batchId)
                .results(results)
                .overallSuccess(overallSuccess)
                .startTime(startTime)
                .endTime(endTime)
                .totalExecutionTime(java.time.Duration.between(startTime, endTime))
                .batchOutputs(batchOutputs)
                .build();
            
            logger.debug("Batch execution completed: {} - Success: {}/{}", 
                       batchId, batchResult.getSuccessCount(), batchResult.getTotalCount());
            
            return batchResult;
            
        } catch (Exception e) {
            logger.error("Batch execution failed for batch {}: {}", batchId, e.getMessage(), e);
            throw new RuleExecutionException("Batch execution failed", e);
        }
    }
    
    private List<RuleExecutionResult> executeRulesSequentially(RuleContext context, List<RuleDefinition> rules) {
        List<RuleExecutionResult> results = new ArrayList<>();
        
        // Sort rules by priority
        List<RuleDefinition> sortedRules = rules.stream()
            .sorted(Comparator.comparing(RuleDefinition::getPriority))
            .collect(Collectors.toList());
        
        for (RuleDefinition rule : sortedRules) {
            try {
                RuleExecutionResult result = execute(context, rule);
                results.add(result);
                
                // Update context with outputs from successful execution
                if (result.isSuccess() && result.hasOutputs()) {
                    for (Map.Entry<String, Object> entry : result.getOutputs().entrySet()) {
                        context.setAttribute(entry.getKey(), entry.getValue());
                    }
                }
                
                // Stop on first error if configured
                if (result.isFailure() && !context.getExecutionMode().continuesOnError()) {
                    logger.debug("Stopping sequential execution due to error in rule: {}", rule.getRuleId());
                    break;
                }
                
            } catch (Exception e) {
                logger.error("Error executing rule {} in batch: {}", rule.getRuleId(), e.getMessage());
                results.add(RuleExecutionResult.failure(rule, e, LocalDateTime.now(), LocalDateTime.now()));
                
                if (!context.getExecutionMode().continuesOnError()) {
                    break;
                }
            }
        }
        
        return results;
    }
    
    private List<RuleExecutionResult> executeRulesInParallel(RuleContext context, List<RuleDefinition> rules) {
        List<CompletableFuture<RuleExecutionResult>> futures = new ArrayList<>();
        
        for (RuleDefinition rule : rules) {
            CompletableFuture<RuleExecutionResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    // Create a child context for parallel execution to avoid conflicts
                    RuleContext childContext = context.createChildContext();
                    return execute(childContext, rule);
                } catch (Exception e) {
                    logger.error("Error in parallel execution of rule {}: {}", rule.getRuleId(), e.getMessage());
                    return RuleExecutionResult.failure(rule, e, LocalDateTime.now(), LocalDateTime.now());
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // Wait for all executions to complete
        return futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    }
    
    private Map<String, Object> executeExpressionRule(RuleContext context, ExpressionRule rule) {
        Map<String, Object> outputs = new HashMap<>();
        
        // Evaluate condition
        boolean conditionResult = conditionEvaluator.evaluate(context, rule.getCondition());
        
        if (conditionResult) {
            // Execute actions
            for (RuleAction action : rule.getActions()) {
                Map<String, Object> actionOutputs = actionExecutor.execute(context, action);
                outputs.putAll(actionOutputs);
            }
        }
        
        return outputs;
    }
    
    private Map<String, Object> executeBranchingRule(RuleContext context, BranchingRule rule) {
        Map<String, Object> outputs = new HashMap<>();
        
        boolean anyBranchExecuted = false;
        
        // Evaluate branches in order
        for (BranchingRule.RuleBranch branch : rule.getBranches()) {
            boolean conditionResult = conditionEvaluator.evaluate(context, branch.getCondition());
            
            if (conditionResult) {
                // Execute branch actions
                for (RuleAction action : branch.getActions()) {
                    Map<String, Object> actionOutputs = actionExecutor.execute(context, action);
                    outputs.putAll(actionOutputs);
                }
                anyBranchExecuted = true;
                break; // Execute only the first matching branch
            }
        }
        
        // Execute else actions if no branch was executed
        if (!anyBranchExecuted && !rule.getElseActions().isEmpty()) {
            for (RuleAction action : rule.getElseActions()) {
                Map<String, Object> actionOutputs = actionExecutor.execute(context, action);
                outputs.putAll(actionOutputs);
            }
        }
        
        return outputs;
    }
    
    private Map<String, Object> executeFormulaRule(RuleContext context, FormulaRule rule) {
        Object result = formulaEvaluator.evaluate(context, rule.getFormula(), rule.getVariables());
        
        Map<String, Object> outputs = new HashMap<>();
        outputs.put(rule.getResultField(), result);
        
        return outputs;
    }
    
    private Map<String, Object> executeTableRule(RuleContext context, RuleTable rule) {
        return tableEvaluator.evaluate(context, rule);
    }
    
    private Map<String, Object> executeScorecardRule(RuleContext context, Scorecard rule) {
        return scorecardEvaluator.evaluate(context, rule);
    }
    
    @Override
    public ValidationResult validate(RuleDefinition rule) {
        if (rule == null) {
            return ValidationResult.failure("Rule definition cannot be null");
        }
        
        return rule.validate();
    }
    
    @Override
    public EngineCapabilities getCapabilities() {
        return new EngineCapabilities.Builder()
            .engineName(getEngineName())
            .engineVersion(getEngineVersion())
            .supportedRuleTypes(EnumSet.allOf(RuleType.class))
            .supportedFeatures(EnumSet.of(
                EngineCapabilities.ExecutionFeature.CONDITIONAL_LOGIC,
                EngineCapabilities.ExecutionFeature.FORMULA_EVALUATION,
                EngineCapabilities.ExecutionFeature.TABLE_LOOKUP,
                EngineCapabilities.ExecutionFeature.SCORECARD_CALCULATION,
                EngineCapabilities.ExecutionFeature.RULE_CHAINING,
                EngineCapabilities.ExecutionFeature.CUSTOM_FUNCTIONS
            ))
            .supportsParallelExecution(true)
            .supportsBatchExecution(true)
            .supportsDebugging(true)
            .supportsMetrics(true)
            .maxRulesPerBatch(1000)
            .build();
    }
    
    @Override
    public String getEngineName() {
        return "DefaultRuleEngine";
    }
    
    @Override
    public String getEngineVersion() {
        return "1.0.0";
    }
    
    @Override
    public boolean supportsRuleType(RuleType ruleType) {
        return ruleType != null && EnumSet.allOf(RuleType.class).contains(ruleType);
    }
}