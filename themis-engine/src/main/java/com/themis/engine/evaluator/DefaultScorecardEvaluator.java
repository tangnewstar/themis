package com.themis.engine.evaluator;

import com.themis.core.engine.RuleContext;
import com.themis.core.model.Scorecard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of ScorecardEvaluator.
 */
public class DefaultScorecardEvaluator implements ScorecardEvaluator {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultScorecardEvaluator.class);
    
    @Override
    public Map<String, Object> evaluate(RuleContext context, Scorecard scorecard) {
        Map<String, Object> outputs = new HashMap<>();
        
        if (scorecard == null || scorecard.getFactors() == null || scorecard.getFactors().isEmpty()) {
            logger.warn("Scorecard is null or has no factors");
            return outputs;
        }
        
        try {
            double totalScore = 0.0;
            Map<String, Double> factorScores = new HashMap<>();
            
            // Calculate weighted score for each factor
            for (Scorecard.ScoreFactor factor : scorecard.getFactors()) {
                Object fieldValue = context.getAttribute(factor.getField());
                double factorScore = calculateFactorScore(factor, fieldValue);
                double weightedScore = factorScore * factor.getWeight();
                
                factorScores.put(factor.getName(), factorScore);
                totalScore += weightedScore;
                
                logger.debug("Factor '{}': value={}, score={}, weighted={}", 
                           factor.getName(), fieldValue, factorScore, weightedScore);
            }
            
            // Apply min/max bounds if defined
            if (scorecard.getMinScore() != null && totalScore < scorecard.getMinScore()) {
                totalScore = scorecard.getMinScore();
            }
            if (scorecard.getMaxScore() != null && totalScore > scorecard.getMaxScore()) {
                totalScore = scorecard.getMaxScore();
            }
            
            // Set outputs
            outputs.put(scorecard.getResultField(), totalScore);
            outputs.put(scorecard.getResultField() + "_breakdown", factorScores);
            
            logger.debug("Scorecard evaluation completed with total score: {}", totalScore);
            
        } catch (Exception e) {
            logger.error("Error evaluating scorecard: {}", e.getMessage(), e);
        }
        
        return outputs;
    }
    
    private double calculateFactorScore(Scorecard.ScoreFactor factor, Object fieldValue) {
        if (fieldValue == null) {
            // Use default score if available
            if (factor.getDefaultScore() != null) {
                try {
                    return Double.parseDouble(factor.getDefaultScore());
                } catch (NumberFormatException e) {
                    logger.warn("Invalid default score for factor '{}': {}", factor.getName(), factor.getDefaultScore());
                }
            }
            return 0.0;
        }
        
        // Look up score in value mapping
        if (factor.getValueScores() != null) {
            String valueKey = fieldValue.toString();
            Double score = factor.getValueScores().get(valueKey);
            if (score != null) {
                return score;
            }
        }
        
        // If no mapping found, try to convert value to numeric score
        if (fieldValue instanceof Number) {
            return ((Number) fieldValue).doubleValue();
        }
        
        // Default to 0 if no score can be determined
        logger.warn("No score mapping found for factor '{}' with value: {}", factor.getName(), fieldValue);
        return 0.0;
    }
}
