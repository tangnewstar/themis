package com.themis.engine.evaluator;

import com.themis.core.engine.RuleContext;
import com.themis.core.model.RuleCondition;

/**
 * Interface for evaluating rule conditions.
 */
public interface ConditionEvaluator {
    
    /**
     * Evaluates a rule condition against the given context.
     * 
     * @param context The rule execution context
     * @param condition The condition to evaluate
     * @return true if the condition is met, false otherwise
     */
    boolean evaluate(RuleContext context, RuleCondition condition);
    
    /**
     * Checks if this evaluator supports the given condition type.
     * 
     * @param conditionType The condition type to check
     * @return true if supported, false otherwise
     */
    boolean supports(RuleCondition.ConditionType conditionType);
}