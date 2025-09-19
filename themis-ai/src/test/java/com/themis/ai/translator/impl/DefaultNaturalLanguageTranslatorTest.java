package com.themis.ai.translator.impl;

import com.themis.ai.translator.NaturalLanguageTranslator;
import com.themis.core.model.RuleDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultNaturalLanguageTranslatorTest {

    private DefaultNaturalLanguageTranslator translator;

    @BeforeEach
    void setUp() {
        translator = new DefaultNaturalLanguageTranslator();
    }

    @Test
    void shouldValidateInput() {
        // When
        NaturalLanguageTranslator.TranslationValidationResult result = 
                translator.validate("if age is greater than 18 then set eligible to true");

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getConfidence()).isGreaterThan(0.0);
        assertThat(result.getSuggestedRuleType()).isEqualTo("expression");
    }

    @Test
    void shouldTranslateToExpressionRule() {
        // Given
        String text = "if age is greater than 18 then set eligible to true";
        Map<String, Object> context = new HashMap<>();

        // When
        RuleDefinition rule = translator.translate(text, context);

        // Then
        assertThat(rule).isNotNull();
        assertThat(rule.getType()).isEqualTo("expression");
    }

    @Test
    void shouldGenerateCandidates() {
        // Given
        String text = "calculate total as price plus tax";
        Map<String, Object> context = new HashMap<>();

        // When
        List<RuleDefinition> candidates = translator.generateCandidates(text, 3, context);

        // Then
        assertThat(candidates).isNotEmpty();
        assertThat(candidates.size()).isLessThanOrEqualTo(3);
    }

    @Test
    void shouldReturnSupportedRuleTypes() {
        // When
        var supportedTypes = translator.getSupportedRuleTypes();

        // Then
        assertThat(supportedTypes).contains("expression", "formula", "branching", "table", "scorecard");
    }
}