package com.themis.ai.service;

import com.themis.ai.translator.NaturalLanguageTranslator;
import com.themis.core.model.RuleDefinition;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for AI-powered rule generation and management
 */
@Service
public class AIRuleService {
    
    private final NaturalLanguageTranslator translator;
    
    public AIRuleService(NaturalLanguageTranslator translator) {
        this.translator = translator;
    }
    
    /**
     * Generate a single rule from natural language
     */
    public RuleDefinition generateRule(String description) {
        return translator.translate(description, Collections.emptyMap());
    }
    
    /**
     * Generate a rule with specific type
     */
    public RuleDefinition generateRule(String description, String ruleType) {
        return translator.translate(description, ruleType, Collections.emptyMap());
    }
    
    /**
     * Generate multiple rule candidates
     */
    public List<RuleDefinition> generateRuleCandidates(String description, int maxCandidates) {
        return translator.generateCandidates(description, maxCandidates, Collections.emptyMap());
    }
    
    /**
     * Validate natural language input
     */
    public NaturalLanguageTranslator.TranslationValidationResult validateInput(String description) {
        return translator.validate(description);
    }
    
    /**
     * Get supported rule types
     */
    public java.util.Set<String> getSupportedRuleTypes() {
        return translator.getSupportedRuleTypes();
    }
}