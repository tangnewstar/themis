package com.themis.engine.impl;

import com.themis.core.engine.*;
import com.themis.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultRuleEngineTest {

    private DefaultRuleEngine ruleEngine;
    private EngineConfiguration configuration;

    @BeforeEach
    void setUp() {
        // Create engine configuration
        configuration = new EngineConfiguration.Builder()
            .engineType("DefaultRuleEngine")
            .enableMetrics(true)
            .enableDebugging(true)
            .threadPoolSize(2)
            .build();
        
        // Create and initialize engine
        ruleEngine = new DefaultRuleEngine();
        ruleEngine.initialize(configuration);
    }

    @Test
    void shouldExecuteExpressionRule() throws RuleExecutionException {
        // Given - Create a test condition that always returns true
        RuleCondition testCondition = new TestCondition("age", ComparisonOperator.GREATER_THAN, 18);
        
        // Create a test action that sets eligible = true
        RuleAction testAction = new TestAction("eligible", true);
        
        // Create expression rule with proper setup
        ExpressionRule rule = new ExpressionRule("test-rule", "Test Rule");
        rule.setRuleType(RuleType.EXPRESSION);
        rule.setStatus(RuleStatus.ACTIVE);
        rule.setVersion("1.0");
        rule.setPriority(100);
        rule.setCondition(testCondition);
        
        List<RuleAction> actions = new ArrayList<>();
        actions.add(testAction);
        rule.setActions(actions);

        // Create rule context with input data
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("age", 25);
        DefaultRuleContext context = new DefaultRuleContext(inputData);

        // When
        RuleExecutionResult result = ruleEngine.execute(context, rule);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.hasOutputs());
        assertEquals(true, result.getOutputs().get("eligible"));
    }

    @Test
    void shouldThrowExceptionForNullRule() {
        // Given
        DefaultRuleContext context = new DefaultRuleContext();

        // When/Then
        RuleExecutionException exception = assertThrows(RuleExecutionException.class, 
            () -> ruleEngine.execute(context, null));
        assertTrue(exception.getMessage().contains("Rule definition cannot be null"));
    }
    
    @Test
    void shouldThrowExceptionForNullContext() {
        // Given
        ExpressionRule rule = new ExpressionRule("test-rule", "Test Rule");
        rule.setRuleType(RuleType.EXPRESSION);
        rule.setStatus(RuleStatus.ACTIVE);
        rule.setVersion("1.0");

        // When/Then
        RuleExecutionException exception = assertThrows(RuleExecutionException.class, 
            () -> ruleEngine.execute(null, rule));
        assertTrue(exception.getMessage().contains("Rule context cannot be null"));
    }
    
    @Test
    void shouldThrowExceptionForUninitializedEngine() {
        // Given
        DefaultRuleEngine uninitializedEngine = new DefaultRuleEngine();
        DefaultRuleContext context = new DefaultRuleContext();
        ExpressionRule rule = new ExpressionRule("test-rule", "Test Rule");
        rule.setRuleType(RuleType.EXPRESSION);
        rule.setStatus(RuleStatus.ACTIVE);
        rule.setVersion("1.0");

        // When/Then
        RuleExecutionException exception = assertThrows(RuleExecutionException.class, 
            () -> uninitializedEngine.execute(context, rule));
        assertTrue(exception.getMessage().contains("Engine not initialized"));
    }
    
    // Test helper classes
    private static class TestCondition extends RuleCondition {
        private String field;
        private ComparisonOperator operator;
        private Object value;
        private boolean ignoreCase = false;
        
        public TestCondition(String field, ComparisonOperator operator, Object value) {
            super(ConditionType.SIMPLE);
            this.field = field;
            this.operator = operator;
            this.value = value;
        }
        
        @Override
        public ValidationResult validate() {
            return ValidationResult.success();
        }
        
        @Override
        public RuleCondition copy() {
            return new TestCondition(field, operator, value);
        }
        
        @Override
        public String toHumanReadable() {
            return field + " " + operator.getSymbol() + " " + value;
        }
    }
    
    private static class TestAction extends RuleAction {
        private String fieldName;
        private Object value;
        private String expression;
        
        public TestAction(String fieldName, Object value) {
            super(ActionType.SET_FIELD);
            this.fieldName = fieldName;
            this.value = value;
        }
        
        public String getFieldName() {
            return fieldName;
        }
        
        public Object getValue() {
            return value;
        }
        
        @Override
        public ValidationResult validate() {
            return ValidationResult.success();
        }
        
        @Override
        public RuleAction copy() {
            return new TestAction(fieldName, value);
        }
        
        @Override
        public String toHumanReadable() {
            return "Set " + fieldName + " = " + value;
        }
    }
}