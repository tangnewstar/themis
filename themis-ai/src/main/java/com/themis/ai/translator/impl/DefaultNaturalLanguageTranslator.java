package com.themis.ai.translator.impl;

import com.themis.ai.translator.NaturalLanguageTranslator;
import com.themis.api.fluent.RuleBuilder;
import com.themis.core.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Default implementation of NaturalLanguageTranslator using Stanford CoreNLP
 */
@Component
public class DefaultNaturalLanguageTranslator implements NaturalLanguageTranslator {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultNaturalLanguageTranslator.class);
    
    private final StanfordCoreNLP pipeline;
    private final ObjectMapper objectMapper;
    private final Map<String, RulePattern> rulePatterns;
    
    public DefaultNaturalLanguageTranslator() {
        this.pipeline = initializeNLPPipeline();
        this.objectMapper = new ObjectMapper();
        this.rulePatterns = initializeRulePatterns();
    }
    
    private StanfordCoreNLP initializeNLPPipeline() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,sentiment");
        props.setProperty("coref.algorithm", "neural");
        return new StanfordCoreNLP(props);
    }
    
    private Map<String, RulePattern> initializeRulePatterns() {
        Map<String, RulePattern> patterns = new HashMap<>();
        
        // Expression Rule Patterns
        patterns.put("expression_if_then", new RulePattern(
                "expression",
                Pattern.compile("if\\s+(.+?)\\s+then\\s+(.+)", Pattern.CASE_INSENSITIVE),
                Arrays.asList("condition", "action")
        ));
        
        patterns.put("expression_when", new RulePattern(
                "expression", 
                Pattern.compile("when\\s+(.+?)\\s+(?:then|do)\\s+(.+)", Pattern.CASE_INSENSITIVE),
                Arrays.asList("condition", "action")
        ));
        
        // Formula Rule Patterns
        patterns.put("formula_calculate", new RulePattern(
                "formula",
                Pattern.compile("calculate\\s+(.+?)\\s+(?:as|=)\\s+(.+)", Pattern.CASE_INSENSITIVE),
                Arrays.asList("variable", "formula")
        ));
        
        patterns.put("formula_set", new RulePattern(
                "formula",
                Pattern.compile("set\\s+(.+?)\\s+(?:to|=)\\s+(.+)", Pattern.CASE_INSENSITIVE),
                Arrays.asList("variable", "formula")
        ));
        
        // Branching Rule Patterns
        patterns.put("branching_switch", new RulePattern(
                "branching",
                Pattern.compile("(?:switch|choose)\\s+based\\s+on\\s+(.+)", Pattern.CASE_INSENSITIVE),
                Arrays.asList("condition")
        ));
        
        // Table Rule Patterns
        patterns.put("table_lookup", new RulePattern(
                "table",
                Pattern.compile("lookup\\s+(.+?)\\s+in\\s+(?:table|from)\\s+(.+)", Pattern.CASE_INSENSITIVE),
                Arrays.asList("input", "table")
        ));
        
        // Scorecard Rule Patterns
        patterns.put("scorecard_score", new RulePattern(
                "scorecard",
                Pattern.compile("(?:score|rate|evaluate)\\s+(.+?)\\s+based\\s+on\\s+(.+)", Pattern.CASE_INSENSITIVE),
                Arrays.asList("subject", "criteria")
        ));
        
        return patterns;
    }
    
    @Override
    public RuleDefinition translate(String naturalLanguageText, Map<String, Object> context) {
        logger.info("Translating natural language text: {}", naturalLanguageText);
        
        // Validate input
        TranslationValidationResult validation = validate(naturalLanguageText);
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid natural language text: " + 
                    String.join(", ", validation.getIssues()));
        }
        
        // Determine rule type
        String ruleType = validation.getSuggestedRuleType();
        if (ruleType == null) {
            ruleType = detectRuleType(naturalLanguageText);
        }
        
        return translate(naturalLanguageText, ruleType, context);
    }
    
    @Override
    public RuleDefinition translate(String naturalLanguageText, String ruleType, Map<String, Object> context) {
        logger.info("Translating to {} rule: {}", ruleType, naturalLanguageText);
        
        try {
            switch (ruleType.toLowerCase()) {
                case "expression":
                    return translateToExpressionRule(naturalLanguageText, context);
                case "formula":
                    return translateToFormulaRule(naturalLanguageText, context);
                case "branching":
                    return translateToBranchingRule(naturalLanguageText, context);
                case "table":
                    return translateToTableRule(naturalLanguageText, context);
                case "scorecard":
                    return translateToScorecardRule(naturalLanguageText, context);
                default:
                    throw new IllegalArgumentException("Unsupported rule type: " + ruleType);
            }
        } catch (Exception e) {
            logger.error("Translation failed for rule type {}: {}", ruleType, e.getMessage(), e);
            throw new RuntimeException("Translation failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<RuleDefinition> generateCandidates(String naturalLanguageText, int maxCandidates, Map<String, Object> context) {
        List<RuleDefinition> candidates = new ArrayList<>();
        
        // Try different rule types and return successful translations
        for (String ruleType : getSupportedRuleTypes()) {
            try {
                RuleDefinition candidate = translate(naturalLanguageText, ruleType, context);
                candidates.add(candidate);
                
                if (candidates.size() >= maxCandidates) {
                    break;
                }
            } catch (Exception e) {
                logger.debug("Failed to translate to {} rule: {}", ruleType, e.getMessage());
            }
        }
        
        return candidates;
    }
    
    @Override
    public TranslationValidationResult validate(String naturalLanguageText) {
        if (naturalLanguageText == null || naturalLanguageText.trim().isEmpty()) {
            return new TranslationValidationResult(false, 0.0, 
                    Arrays.asList("Natural language text is empty"), null);
        }
        
        List<String> issues = new ArrayList<>();
        double confidence = 0.0;
        String suggestedRuleType = null;
        
        // Basic validation
        if (naturalLanguageText.length() < 10) {
            issues.add("Text too short for meaningful rule translation");
        }
        
        // Detect rule type and calculate confidence
        String detectedType = detectRuleType(naturalLanguageText);
        if (detectedType != null) {
            suggestedRuleType = detectedType;
            confidence = calculateTranslationConfidence(naturalLanguageText, detectedType);
        } else {
            issues.add("Could not detect appropriate rule type");
        }
        
        // NLP analysis
        try {
            Annotation annotation = new Annotation(naturalLanguageText);
            pipeline.annotate(annotation);
            
            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            if (sentences.isEmpty()) {
                issues.add("No valid sentences detected");
            }
            
            // Check sentiment for confidence adjustment
            for (CoreMap sentence : sentences) {
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                if (tree != null) {
                    int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                    if (sentiment == 0 || sentiment == 4) { // Very negative or very positive
                        confidence *= 0.8; // Reduce confidence for extreme sentiments
                    }
                }
            }
            
        } catch (Exception e) {
            logger.warn("NLP analysis failed: {}", e.getMessage());
            issues.add("NLP analysis failed");
        }
        
        boolean valid = issues.isEmpty() && confidence > 0.3;
        
        return new TranslationValidationResult(valid, confidence, issues, suggestedRuleType);
    }
    
    @Override
    public Set<String> getSupportedRuleTypes() {
        Set<String> supportedTypes = new HashSet<>();
        supportedTypes.add("expression");
        supportedTypes.add("formula");
        supportedTypes.add("branching");
        supportedTypes.add("table");
        supportedTypes.add("scorecard");
        return supportedTypes;
    }
    
    private String detectRuleType(String text) {
        String lowerText = text.toLowerCase();
        
        // Score each rule type based on keywords
        Map<String, Integer> typeScores = new HashMap<>();
        typeScores.put("expression", 0);
        typeScores.put("formula", 0);
        typeScores.put("branching", 0);
        typeScores.put("table", 0);
        typeScores.put("scorecard", 0);
        
        // Expression rule keywords
        if (lowerText.contains("if") || lowerText.contains("when") || lowerText.contains("then")) {
            typeScores.put("expression", typeScores.get("expression") + 3);
        }
        
        // Formula rule keywords
        if (lowerText.contains("calculate") || lowerText.contains("formula") || 
            lowerText.contains("compute") || text.contains("=")) {
            typeScores.put("formula", typeScores.get("formula") + 3);
        }
        
        // Branching rule keywords
        if (lowerText.contains("switch") || lowerText.contains("choose") || 
            lowerText.contains("branch") || lowerText.contains("case")) {
            typeScores.put("branching", typeScores.get("branching") + 3);
        }
        
        // Table rule keywords
        if (lowerText.contains("lookup") || lowerText.contains("table") || 
            lowerText.contains("matrix") || lowerText.contains("grid")) {
            typeScores.put("table", typeScores.get("table") + 3);
        }
        
        // Scorecard rule keywords
        if (lowerText.contains("score") || lowerText.contains("rate") || 
            lowerText.contains("evaluate") || lowerText.contains("assess")) {
            typeScores.put("scorecard", typeScores.get("scorecard") + 3);
        }
        
        // Return type with highest score
        return typeScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .orElse("expression"); // Default to expression
    }
    
    private double calculateTranslationConfidence(String text, String ruleType) {
        double baseConfidence = 0.5;
        
        // Check for pattern matches
        for (RulePattern pattern : rulePatterns.values()) {
            if (pattern.getRuleType().equals(ruleType) && pattern.getPattern().matcher(text).find()) {
                baseConfidence += 0.3;
            }
        }
        
        // Adjust based on text length and complexity
        if (text.length() > 50) {
            baseConfidence += 0.1;
        }
        if (text.split("\\s+").length > 10) {
            baseConfidence += 0.1;
        }
        
        return Math.min(1.0, baseConfidence);
    }
    
    private ExpressionRule translateToExpressionRule(String text, Map<String, Object> context) {
        // Simplified implementation - create basic expression rule
        ExpressionRule rule = new ExpressionRule(generateRuleId("expr", context), extractRuleName(text));
        rule.setDescription("Generated from: " + text);
        return rule;
    }
    
    private FormulaRule translateToFormulaRule(String text, Map<String, Object> context) {
        // Simplified implementation - create basic formula rule
        String ruleId = generateRuleId("formula", context);
        String ruleName = extractRuleName(text);
        FormulaRule rule = new FormulaRule(ruleId, ruleName, text, "result");
        rule.setDescription("Generated from: " + text);
        return rule;
    }
    
    private BranchingRule translateToBranchingRule(String text, Map<String, Object> context) {
        // Simplified implementation - create basic branching rule
        BranchingRule rule = new BranchingRule(generateRuleId("branch", context), extractRuleName(text));
        rule.setDescription("Generated from: " + text);
        return rule;
    }
    
    private RuleTable translateToTableRule(String text, Map<String, Object> context) {
        // Simplified implementation - create basic table rule
        RuleTable rule = new RuleTable(generateRuleId("table", context), extractRuleName(text));
        rule.setDescription("Generated from: " + text);
        return rule;
    }
    
    private Scorecard translateToScorecardRule(String text, Map<String, Object> context) {
        // Simplified implementation - create basic scorecard rule
        Scorecard rule = new Scorecard(generateRuleId("scorecard", context), extractRuleName(text));
        rule.setDescription("Generated from: " + text);
        return rule;
    }
    
    private String generateRuleId(String prefix, Map<String, Object> context) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + "_" + timestamp.substring(timestamp.length() - 6);
    }
    
    private String extractRuleName(String text) {
        // Extract a meaningful name from the text (first few words)
        String[] words = text.split("\\s+");
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < Math.min(4, words.length); i++) {
            if (i > 0) name.append(" ");
            name.append(words[i]);
        }
        return name.toString();
    }
    
    /**
     * Helper class for rule pattern matching
     */
    private static class RulePattern {
        private final String ruleType;
        private final Pattern pattern;
        private final List<String> groups;
        
        public RulePattern(String ruleType, Pattern pattern, List<String> groups) {
            this.ruleType = ruleType;
            this.pattern = pattern;
            this.groups = groups;
        }
        
        public String getRuleType() { return ruleType; }
        public Pattern getPattern() { return pattern; }
        public List<String> getGroups() { return groups; }
    }
}