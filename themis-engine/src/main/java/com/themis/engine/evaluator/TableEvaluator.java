package com.themis.engine.evaluator;

import com.themis.core.engine.RuleContext;
import com.themis.core.model.RuleTable;
import com.themis.core.model.Scorecard;

import java.util.Map;

/**
 * Interface for evaluating rule tables.
 */
public interface TableEvaluator {
    
    /**
     * Evaluates a rule table to find matching outputs.
     * 
     * @param context The rule execution context
     * @param ruleTable The rule table to evaluate
     * @return Map of output values
     */
    Map<String, Object> evaluate(RuleContext context, RuleTable ruleTable);
}

