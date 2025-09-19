package com.themis.engine.evaluator;

import com.themis.core.engine.RuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

/**
 * Default implementation of FormulaEvaluator using JavaScript engine.
 */
public class DefaultFormulaEvaluator implements FormulaEvaluator {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultFormulaEvaluator.class);
    
    private final ScriptEngineManager scriptEngineManager;
    
    public DefaultFormulaEvaluator() {
        this.scriptEngineManager = new ScriptEngineManager();
    }
    
    @Override
    public Object evaluate(RuleContext context, String formula, Map<String, Object> variables) {
        if (formula == null || formula.trim().isEmpty()) {
            logger.warn("Formula is null or empty");
            return null;
        }
        
        try {
            ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("javascript");
            if (scriptEngine == null) {
                logger.error("JavaScript engine not available");
                return null;
            }
            
            // Add context attributes to script engine
            for (String key : context.getAttributeKeys()) {
                Object value = context.getAttribute(key);
                if (value instanceof Number || value instanceof String || value instanceof Boolean) {
                    scriptEngine.put(key, value);
                }
            }
            
            // Add additional variables
            if (variables != null) {
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    scriptEngine.put(entry.getKey(), entry.getValue());
                }
            }
            
            // Evaluate the formula
            Object result = scriptEngine.eval(formula);
            logger.debug("Formula '{}' evaluated to: {}", formula, result);
            
            return result;
            
        } catch (ScriptException e) {
            logger.error("Error evaluating formula '{}': {}", formula, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error evaluating formula '{}': {}", formula, e.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean isValidFormula(String formula) {
        if (formula == null || formula.trim().isEmpty()) {
            return false;
        }
        
        try {
            ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("javascript");
            if (scriptEngine == null) {
                return false;
            }
            
            // Try to compile the formula without executing it
            scriptEngine.eval("function validateFormula() { return (" + formula + "); }");
            return true;
            
        } catch (Exception e) {
            logger.debug("Formula validation failed for '{}': {}", formula, e.getMessage());
            return false;
        }
    }
}