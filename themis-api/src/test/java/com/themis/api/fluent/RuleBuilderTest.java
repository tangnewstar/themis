package com.themis.api.fluent;

//import com.themis.core.model.rules.ExpressionRule;
//import com.themis.core.model.rules.FormulaRule;
import com.themis.core.model.ExpressionRule;
import com.themis.core.model.FormulaRule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuleBuilderTest {

    @Test
    void shouldBuildExpressionRule() {
        // When
        ExpressionRule rule = RuleBuilder.expression()
                .id("test-expr")
                .name("Test Expression")
                .condition("age > 18")
                .action("eligible = true")
                .build();

        // Then
        assertThat(rule.getRuleId()).isEqualTo("test-expr");
        assertThat(rule.getRuleName()).isEqualTo("Test Expression");
        assertThat(rule.getCondition()).isEqualTo("age > 18");
        assertThat(rule.getAction()).isEqualTo("eligible = true");
    }

    @Test
    void shouldBuildFormulaRule() {
        // When
        FormulaRule rule = RuleBuilder.formula()
                .id("test-formula")
                .name("Test Formula")
                .outputVariable("result")
                .formula("a + b * 2")
                .build();

        // Then
        assertThat(rule.getRuleId()).isEqualTo("test-formula");
        assertThat(rule.getRuleName()).isEqualTo("Test Formula");
        assertThat(rule.getOutputVariable()).isEqualTo("result");
        assertThat(rule.getFormula()).isEqualTo("a + b * 2");
    }
}