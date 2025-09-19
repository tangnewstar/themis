package com.themis.engine.evaluator;

import com.themis.core.engine.RuleContext;
import com.themis.core.model.Scorecard;

import java.util.Map;
/**
 * Interface for evaluating scorecards.
 */
public interface ScorecardEvaluator {
    
    /**
     * Evaluates a scorecard to calculate the final score.
     * 
     * @param context The rule execution context
     * @param scorecard The scorecard to evaluate
     * @return Map containing the calculated score and breakdown
     */
    Map<String, Object> evaluate(RuleContext context, Scorecard scorecard);
}
