package com.themis.engine.executor;

import com.themis.core.engine.RuleContext;
import com.themis.core.model.RuleAction;
import com.themis.engine.evaluator.FormulaEvaluator;
import com.themis.engine.evaluator.DefaultFormulaEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of ActionExecutor.
 * Supports basic action types with reflection-based field access.
 */
public class DefaultActionExecutor implements ActionExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultActionExecutor.class);
    
    private final FormulaEvaluator formulaEvaluator;
    
    public DefaultActionExecutor() {
        this.formulaEvaluator = new DefaultFormulaEvaluator();
    }
    
    @Override
    public Map<String, Object> execute(RuleContext context, RuleAction action) {
        Map<String, Object> outputs = new HashMap<>();
        
        if (action == null) {
            logger.warn("Action is null, returning empty outputs");
            return outputs;
        }
        
        try {
            switch (action.getActionType()) {
                case SET_FIELD:
                    return executeSetFieldAction(context, action);
                case CALCULATE:
                    return executeCalculateAction(context, action);
                case CALL_SERVICE:
                    return executeCallServiceAction(context, action);
                case LOG:
                    return executeLogAction(context, action);
                default:
                    logger.warn("Unsupported action type: {}", action.getActionType());
                    break;
            }
        } catch (Exception e) {
            logger.error("Error executing action {}: {}", action.getActionType(), e.getMessage(), e);
        }
        
        return outputs;
    }
    
    @Override
    public boolean supports(RuleAction.ActionType actionType) {
        return actionType == RuleAction.ActionType.SET_FIELD ||
               actionType == RuleAction.ActionType.CALCULATE ||
               actionType == RuleAction.ActionType.CALL_SERVICE ||
               actionType == RuleAction.ActionType.LOG;
    }
    
    private Map<String, Object> executeSetFieldAction(RuleContext context, RuleAction action) {
        Map<String, Object> outputs = new HashMap<>();
        
        try {
            // Use reflection to access fields
            Field fieldNameField = action.getClass().getDeclaredField("fieldName");
            fieldNameField.setAccessible(true);
            String fieldName = (String) fieldNameField.get(action);
            
            Field valueField = action.getClass().getDeclaredField("value");
            valueField.setAccessible(true);
            Object value = valueField.get(action);
            
            Field expressionField = action.getClass().getDeclaredField("expression");
            expressionField.setAccessible(true);
            String expression = (String) expressionField.get(action);
            
            Object finalValue;
            if (expression != null && !expression.trim().isEmpty()) {
                // Evaluate expression
                finalValue = formulaEvaluator.evaluate(context, expression, null);
            } else {
                finalValue = value;
            }
            
            // Set the field in context
            context.setAttribute(fieldName, finalValue);
            outputs.put(fieldName, finalValue);
            
            logger.debug("Set field '{}' = {}", fieldName, finalValue);
            
        } catch (Exception e) {
            logger.error("Error executing SET_FIELD action: {}", e.getMessage());
        }
        
        return outputs;
    }
    
    private Map<String, Object> executeCalculateAction(RuleContext context, RuleAction action) {
        Map<String, Object> outputs = new HashMap<>();
        
        try {
            // Use reflection to access fields
            Field resultFieldField = action.getClass().getDeclaredField("resultField");
            resultFieldField.setAccessible(true);
            String resultField = (String) resultFieldField.get(action);
            
            Field formulaField = action.getClass().getDeclaredField("formula");
            formulaField.setAccessible(true);
            String formula = (String) formulaField.get(action);
            
            Field variablesField = action.getClass().getDeclaredField("variables");
            variablesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> variables = (Map<String, Object>) variablesField.get(action);
            
            // Evaluate the formula
            Object result = formulaEvaluator.evaluate(context, formula, variables);
            
            // Set the result in context
            context.setAttribute(resultField, result);
            outputs.put(resultField, result);
            
            logger.debug("Calculated '{}' = {}", resultField, result);
            
        } catch (Exception e) {
            logger.error("Error executing CALCULATE action: {}", e.getMessage());
        }
        
        return outputs;
    }
    
    private Map<String, Object> executeCallServiceAction(RuleContext context, RuleAction action) {
        Map<String, Object> outputs = new HashMap<>();
        
        try {
            // Use reflection to access fields
            Field serviceNameField = action.getClass().getDeclaredField("serviceName");
            serviceNameField.setAccessible(true);
            String serviceName = (String) serviceNameField.get(action);
            
            Field methodNameField = action.getClass().getDeclaredField("methodName");
            methodNameField.setAccessible(true);
            String methodName = (String) methodNameField.get(action);
            
            // Placeholder implementation - would integrate with actual service registry
            logger.info("Would call service: {}.{}", serviceName, methodName);
            
            // For now, just log the call
            outputs.put("serviceCallResult", "success");
            
        } catch (Exception e) {
            logger.error("Error executing CALL_SERVICE action: {}", e.getMessage());
        }
        
        return outputs;
    }
    
    private Map<String, Object> executeLogAction(RuleContext context, RuleAction action) {
        Map<String, Object> outputs = new HashMap<>();
        
        try {
            // Use reflection to access fields
            Field messageField = action.getClass().getDeclaredField("message");
            messageField.setAccessible(true);
            String message = (String) messageField.get(action);
            
            Field levelField = action.getClass().getDeclaredField("level");
            levelField.setAccessible(true);
            Object level = levelField.get(action);
            
            // Log the message at the appropriate level
            String logLevel = level != null ? level.toString() : "INFO";
            
            switch (logLevel.toUpperCase()) {
                case "ERROR":
                    logger.error("Rule Log: {}", message);
                    break;
                case "WARN":
                    logger.warn("Rule Log: {}", message);
                    break;
                case "DEBUG":
                    logger.debug("Rule Log: {}", message);
                    break;
                case "TRACE":
                    logger.trace("Rule Log: {}", message);
                    break;
                default:
                    logger.info("Rule Log: {}", message);
                    break;
            }
            
            outputs.put("logMessage", message);
            outputs.put("logLevel", logLevel);
            
        } catch (Exception e) {
            logger.error("Error executing LOG action: {}", e.getMessage());
        }
        
        return outputs;
    }
}