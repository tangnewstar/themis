package com.themis.engine.evaluator;

import com.themis.core.engine.RuleContext;

import java.util.Map;

/**
 * Interface for evaluating mathematical formulas.
 */
public interface FormulaEvaluator {
    
    /**
     * Evaluates a mathematical formula with the given context and variables.
     * 
     * @param context The rule execution context
     * @param formula The formula expression to evaluate
     * @param variables Additional variables for the formula
     * @return The result of the formula evaluation
     */
    Object evaluate(RuleContext context, String formula, Map<String, Object> variables);
    
    /**
     * Validates a formula for syntax correctness.
     * 
     * @param formula The formula to validate
     * @return true if the formula is valid, false otherwise
     */
    boolean isValidFormula(String formula);
}