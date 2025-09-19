package com.themis.core.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Scorecard rule implementation for multi-factor scoring models.
 * Example: Credit score calculation with weighted factors
 */
@JsonTypeName("SCORECARD")
public class Scorecard extends RuleDefinition {
    
    @NotNull(message = "Score factors cannot be null")
    private List<ScoreFactor> factors = new ArrayList<>();
    
    @NotNull(message = "Scoring model cannot be null")
    private ScoringModel scoringModel;
    
    private String resultField = "score";
    
    private Double minScore;
    private Double maxScore;
    
    // Default constructor for Jackson
    public Scorecard() {
        super();
        setRuleType(RuleType.SCORECARD);
    }
    
    public Scorecard(String ruleId, String ruleName) {
        super(ruleId, ruleName, RuleType.SCORECARD);
    }
    
    // Getters and Setters
    public List<ScoreFactor> getFactors() { return factors; }
    public void setFactors(List<ScoreFactor> factors) { 
        this.factors = factors != null ? factors : new ArrayList<>(); 
    }
    
    public ScoringModel getScoringModel() { return scoringModel; }
    public void setScoringModel(ScoringModel scoringModel) { this.scoringModel = scoringModel; }
    
    public String getResultField() { return resultField; }
    public void setResultField(String resultField) { this.resultField = resultField; }
    
    public Double getMinScore() { return minScore; }
    public void setMinScore(Double minScore) { this.minScore = minScore; }
    
    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }
    
    public void addFactor(ScoreFactor factor) {
        if (factor != null) {
            this.factors.add(factor);
        }
    }
    
    @Override
    public ValidationResult validate() {
        List<ValidationResult.ValidationError> errors = new ArrayList<>();
        List<ValidationResult.ValidationWarning> warnings = new ArrayList<>();
        
        // Basic validation
        if (getRuleId() == null || getRuleId().trim().isEmpty()) {
            errors.add(new ValidationResult.ValidationError("MISSING_RULE_ID", "Rule ID is required", "ruleId"));
        }
        
        if (factors == null || factors.isEmpty()) {
            errors.add(new ValidationResult.ValidationError("MISSING_FACTORS", 
                "At least one score factor is required", "factors"));
        } else {
            double totalWeight = 0.0;
            for (int i = 0; i < factors.size(); i++) {
                ScoreFactor factor = factors.get(i);
                if (factor == null) {
                    errors.add(new ValidationResult.ValidationError("NULL_FACTOR", 
                        "Score factor cannot be null", "factors[" + i + "]"));
                } else {
                    ValidationResult factorValidation = factor.validate();
                    if (!factorValidation.isValid()) {
                        errors.addAll(factorValidation.getErrors());
                    }
                    warnings.addAll(factorValidation.getWarnings());
                    totalWeight += factor.getWeight();
                }
            }
            
            // Check if weights sum to approximately 1.0
            if (Math.abs(totalWeight - 1.0) > 0.01) {
                warnings.add(new ValidationResult.ValidationWarning("WEIGHT_SUM_WARNING", 
                    String.format("Factor weights sum to %.3f, consider normalizing to 1.0", totalWeight), "factors"));
            }
        }
        
        if (scoringModel == null) {
            errors.add(new ValidationResult.ValidationError("MISSING_SCORING_MODEL", 
                "Scoring model is required", "scoringModel"));
        }
        
        return errors.isEmpty() ? 
            (warnings.isEmpty() ? ValidationResult.success() : ValidationResult.successWithWarnings(warnings)) :
            ValidationResult.failure(errors, warnings);
    }
    
    @Override
    public RuleDefinition copy() {
        Scorecard copy = new Scorecard(getRuleId(), getRuleName());
        
        // Copy basic properties
        copy.setDescription(getDescription());
        copy.setStatus(getStatus());
        copy.setVersion(getVersion());
        copy.setCreatedBy(getCreatedBy());
        copy.setUpdatedBy(getUpdatedBy());
        copy.setPriority(getPriority());
        
        if (getMetadata() != null) {
            copy.setMetadata(new java.util.HashMap<>(getMetadata()));
        }
        
        if (getTags() != null) {
            copy.setTags(new java.util.HashMap<>(getTags()));
        }
        
        // Copy scorecard-specific data
        List<ScoreFactor> copiedFactors = new ArrayList<>();
        for (ScoreFactor factor : factors) {
            if (factor != null) {
                copiedFactors.add(factor.copy());
            }
        }
        copy.setFactors(copiedFactors);
        
        if (scoringModel != null) {
            copy.setScoringModel(scoringModel.copy());
        }
        
        copy.setResultField(resultField);
        copy.setMinScore(minScore);
        copy.setMaxScore(maxScore);
        
        return copy;
    }
    
    @Override
    public String toString() {
        return String.format("Scorecard{ruleId='%s', factors=%d, resultField='%s'}", 
                           getRuleId(), factors.size(), resultField);
    }
    
    /**
     * Represents a factor in the scorecard calculation.
     */
    public static class ScoreFactor {
        private String name;
        private String field;
        private double weight;
        private Map<String, Double> valueScores;
        private String defaultScore;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        
        public double getWeight() { return weight; }
        public void setWeight(double weight) { this.weight = weight; }
        
        public Map<String, Double> getValueScores() { return valueScores; }
        public void setValueScores(Map<String, Double> valueScores) { this.valueScores = valueScores; }
        
        public String getDefaultScore() { return defaultScore; }
        public void setDefaultScore(String defaultScore) { this.defaultScore = defaultScore; }
        
        public ValidationResult validate() {
            List<ValidationResult.ValidationError> errors = new ArrayList<>();
            
            if (name == null || name.trim().isEmpty()) {
                errors.add(new ValidationResult.ValidationError("MISSING_FACTOR_NAME", 
                    "Factor name is required", "name"));
            }
            
            if (field == null || field.trim().isEmpty()) {
                errors.add(new ValidationResult.ValidationError("MISSING_FACTOR_FIELD", 
                    "Factor field is required", "field"));
            }
            
            if (weight < 0 || weight > 1) {
                errors.add(new ValidationResult.ValidationError("INVALID_WEIGHT", 
                    "Factor weight must be between 0 and 1", "weight"));
            }
            
            return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
        }
        
        public ScoreFactor copy() {
            ScoreFactor copy = new ScoreFactor();
            copy.setName(name);
            copy.setField(field);
            copy.setWeight(weight);
            copy.setDefaultScore(defaultScore);
            
            if (valueScores != null) {
                copy.setValueScores(new java.util.HashMap<>(valueScores));
            }
            
            return copy;
        }
    }
    
    /**
     * Represents the scoring model configuration.
     */
    public static class ScoringModel {
        private String modelType = "WEIGHTED_SUM";
        private Map<String, Object> parameters;
        
        // Getters and Setters
        public String getModelType() { return modelType; }
        public void setModelType(String modelType) { this.modelType = modelType; }
        
        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
        
        public ScoringModel copy() {
            ScoringModel copy = new ScoringModel();
            copy.setModelType(modelType);
            
            if (parameters != null) {
                copy.setParameters(new java.util.HashMap<>(parameters));
            }
            
            return copy;
        }
    }
}