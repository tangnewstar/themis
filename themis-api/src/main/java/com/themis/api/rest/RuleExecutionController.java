package com.themis.api.rest;

import com.themis.api.rest.dto.*;
import com.themis.core.engine.*;
import com.themis.core.model.RuleDefinition;
import com.themis.core.model.ValidationResult;
import com.themis.engine.impl.DefaultRuleEngine;
import com.themis.metadata.storage.MetadataStorage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * REST API controller for rule execution operations.
 */
@RestController
@RequestMapping("/api/v1/execution")
@Tag(name = "Rule Execution", description = "APIs for executing rules and managing execution contexts")
public class RuleExecutionController {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleExecutionController.class);
    
    private final RuleEngine ruleEngine;
    private final MetadataStorage metadataStorage;
    
    @Autowired
    public RuleExecutionController(RuleEngine ruleEngine, MetadataStorage metadataStorage) {
        this.ruleEngine = ruleEngine;
        this.metadataStorage = metadataStorage;
    }
    
    /**
     * Executes a single rule synchronously.
     */
    @PostMapping("/sync")
    @Operation(summary = "Execute a rule synchronously", 
               description = "Executes a single rule with the provided context data")
    public ResponseEntity<ApiResponse<RuleExecutionResponse>> executeRule(
            @Valid @RequestBody RuleExecutionRequest request) {
        
        try {
            // Load rule
            Optional<RuleDefinition> ruleOpt = metadataStorage.load(request.getRuleId());
            if (!ruleOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Rule not found: " + request.getRuleId()));
            }
            
            RuleDefinition rule = ruleOpt.get();
            
            // Create execution context
            RuleContext context = new DefaultRuleContext(request.getInputData());
            if (request.getExecutionMode() != null) {
                context.setExecutionMode(request.getExecutionMode());
            }
            
            // Execute rule
            RuleExecutionResult result = ruleEngine.execute(context, rule);
            
            // Convert to response
            RuleExecutionResponse response = RuleExecutionResponse.fromResult(result);
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            logger.error("Error executing rule: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Rule execution failed: " + e.getMessage()));
        }
    }
    
    /**
     * Executes multiple rules in batch.
     */
    @PostMapping("/batch")
    @Operation(summary = "Execute multiple rules in batch", 
               description = "Executes multiple rules with the provided context data")
    public ResponseEntity<ApiResponse<BatchExecutionResponse>> executeBatch(
            @Valid @RequestBody BatchExecutionRequest request) {
        
        try {
            // Load rules
            List<RuleDefinition> rules = new java.util.ArrayList<>();
            for (String ruleId : request.getRuleIds()) {
                Optional<RuleDefinition> ruleOpt = metadataStorage.load(ruleId);
                if (ruleOpt.isPresent()) {
                    rules.add(ruleOpt.get());
                } else {
                    logger.warn("Rule not found in batch execution: {}", ruleId);
                }
            }
            
            if (rules.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No valid rules found for execution"));
            }
            
            // Create execution context
            RuleContext context = new DefaultRuleContext(request.getInputData());
            if (request.getExecutionMode() != null) {
                context.setExecutionMode(request.getExecutionMode());
            }
            
            // Execute rules
            BatchExecutionResult result = ruleEngine.executeBatch(context, rules);
            
            // Convert to response
            BatchExecutionResponse response = BatchExecutionResponse.fromResult(result);
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            logger.error("Error executing batch: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Batch execution failed: " + e.getMessage()));
        }
    }
    
    /**
     * Executes a rule asynchronously.
     */
    @PostMapping("/async")
    @Operation(summary = "Execute a rule asynchronously", 
               description = "Starts asynchronous execution of a rule and returns a job ID")
    public ResponseEntity<ApiResponse<AsyncExecutionResponse>> executeRuleAsync(
            @Valid @RequestBody RuleExecutionRequest request) {
        
        try {
            // Generate job ID
            String jobId = UUID.randomUUID().toString();
            
            // Start async execution (placeholder implementation)
            // In a real implementation, you'd use a task queue or executor service
            
            AsyncExecutionResponse response = new AsyncExecutionResponse();
            response.setJobId(jobId);
            response.setStatus("STARTED");
            response.setMessage("Rule execution started asynchronously");
            
            return ResponseEntity.accepted().body(ApiResponse.success(response));
            
        } catch (Exception e) {
            logger.error("Error starting async execution: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to start async execution: " + e.getMessage()));
        }
    }
    
    /**
     * Gets the status of an asynchronous execution.
     */
    @GetMapping("/async/{jobId}/status")
    @Operation(summary = "Get async execution status", 
               description = "Retrieves the status of an asynchronous rule execution")
    public ResponseEntity<ApiResponse<AsyncExecutionStatus>> getAsyncStatus(
            @Parameter(description = "Job ID") @PathVariable String jobId) {
        
        try {
            // Placeholder implementation
            AsyncExecutionStatus status = new AsyncExecutionStatus();
            status.setJobId(jobId);
            status.setStatus("COMPLETED");
            status.setProgress(100);
            status.setMessage("Execution completed successfully");
            
            return ResponseEntity.ok(ApiResponse.success(status));
            
        } catch (Exception e) {
            logger.error("Error getting async status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get execution status: " + e.getMessage()));
        }
    }
    
    /**
     * Gets the result of an asynchronous execution.
     */
    @GetMapping("/async/{jobId}/result")
    @Operation(summary = "Get async execution result", 
               description = "Retrieves the result of an asynchronous rule execution")
    public ResponseEntity<ApiResponse<RuleExecutionResponse>> getAsyncResult(
            @Parameter(description = "Job ID") @PathVariable String jobId) {
        
        try {
            // Placeholder implementation
            // In a real implementation, you'd retrieve the actual result from storage
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Result not found for job: " + jobId));
            
        } catch (Exception e) {
            logger.error("Error getting async result: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get execution result: " + e.getMessage()));
        }
    }
    
    /**
     * Validates a rule without executing it.
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate a rule", 
               description = "Validates a rule definition for syntax and semantic correctness")
    public ResponseEntity<ApiResponse<ValidationResponse>> validateRule(
            @Parameter(description = "Rule ID") @RequestParam String ruleId) {
        
        try {
            // Load rule
            Optional<RuleDefinition> ruleOpt = metadataStorage.load(ruleId);
            if (!ruleOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Rule not found: " + ruleId));
            }
            
            RuleDefinition rule = ruleOpt.get();
            
            // Validate rule
            ValidationResult result = ruleEngine.validate(rule);
            
            // Convert to response
            ValidationResponse response = ValidationResponse.fromResult(result);
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            logger.error("Error validating rule: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Rule validation failed: " + e.getMessage()));
        }
    }
}