package com.themis.ai.translator;

import com.themis.core.model.RuleDefinition;

import java.util.Map;

/**
 * Interface for translating natural language to rule definitions
 */
public interface NaturalLanguageTranslator {
    
    /**
     * Translate natural language text to a rule definition
     * 
     * @param naturalLanguageText The natural language description of the rule
     * @param context Additional context information for translation
     * @return The generated rule definition
     */
    RuleDefinition translate(String naturalLanguageText, Map<String, Object> context);
    
    /**
     * Translate natural language text to a rule definition with specified rule type
     * 
     * @param naturalLanguageText The natural language description of the rule
     * @param ruleType The target rule type (expression, branching, formula, table, scorecard)
     * @param context Additional context information for translation
     * @return The generated rule definition
     */
    RuleDefinition translate(String naturalLanguageText, String ruleType, Map<String, Object> context);
    
    /**
     * Generate multiple rule candidates and let user choose
     * 
     * @param naturalLanguageText The natural language description of the rule
     * @param maxCandidates Maximum number of rule candidates to generate
     * @param context Additional context information for translation
     * @return List of possible rule definitions
     */
    java.util.List<RuleDefinition> generateCandidates(String naturalLanguageText, int maxCandidates, Map<String, Object> context);
    
    /**
     * Validate if the natural language text can be translated to a rule
     * 
     * @param naturalLanguageText The natural language text to validate
     * @return Validation result with confidence score and issues
     */
    TranslationValidationResult validate(String naturalLanguageText);
    
    /**
     * Get supported rule types for translation
     * 
     * @return Set of supported rule type names
     */
    java.util.Set<String> getSupportedRuleTypes();
    
    /**
     * Translation validation result
     */
    class TranslationValidationResult {
        private final boolean valid;
        private final double confidence;
        private final java.util.List<String> issues;
        private final String suggestedRuleType;
        
        public TranslationValidationResult(boolean valid, double confidence, java.util.List<String> issues, String suggestedRuleType) {
            this.valid = valid;
            this.confidence = confidence;
            this.issues = issues != null ? issues : new java.util.ArrayList<>();
            this.suggestedRuleType = suggestedRuleType;
        }
        
        public boolean isValid() { return valid; }
        public double getConfidence() { return confidence; }
        public java.util.List<String> getIssues() { return new java.util.ArrayList<>(issues); }
        public String getSuggestedRuleType() { return suggestedRuleType; }
    }
}