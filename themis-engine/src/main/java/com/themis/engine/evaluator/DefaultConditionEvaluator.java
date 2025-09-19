package com.themis.engine.evaluator;

import com.themis.core.engine.RuleContext;
import com.themis.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Default implementation of ConditionEvaluator.
 * Uses reflection to access condition fields since concrete classes are package-private.
 */
public class DefaultConditionEvaluator implements ConditionEvaluator {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultConditionEvaluator.class);
    
    @Override
    public boolean evaluate(RuleContext context, RuleCondition condition) {
        if (condition == null) {
            logger.warn("Condition is null, returning false");
            return false;
        }
        
        try {
            switch (condition.getConditionType()) {
                case SIMPLE:
                    return evaluateSimpleCondition(context, condition);
                case COMPOSITE:
                    return evaluateCompositeCondition(context, condition);
                default:
                    logger.warn("Unsupported condition type: {}", condition.getConditionType());
                    return false;
            }
        } catch (Exception e) {
            logger.error("Error evaluating condition {}: {}", condition, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean supports(RuleCondition.ConditionType conditionType) {
        return conditionType == RuleCondition.ConditionType.SIMPLE || 
               conditionType == RuleCondition.ConditionType.COMPOSITE;
    }
    
    private boolean evaluateSimpleCondition(RuleContext context, RuleCondition condition) {
        try {
            // Use reflection to access private fields since SimpleCondition is package-private
            Field fieldField = condition.getClass().getDeclaredField("field");
            fieldField.setAccessible(true);
            String field = (String) fieldField.get(condition);
            
            Field operatorField = condition.getClass().getDeclaredField("operator");
            operatorField.setAccessible(true);
            ComparisonOperator operator = (ComparisonOperator) operatorField.get(condition);
            
            Field valueField = condition.getClass().getDeclaredField("value");
            valueField.setAccessible(true);
            Object expectedValue = valueField.get(condition);
            
            Field ignoreCaseField = condition.getClass().getDeclaredField("ignoreCase");
            ignoreCaseField.setAccessible(true);
            boolean ignoreCase = (Boolean) ignoreCaseField.get(condition);
            
            // Get actual value from context
            Object actualValue = context.getAttribute(field);
            
            return compareValues(actualValue, operator, expectedValue, ignoreCase);
        } catch (Exception e) {
            logger.error("Error accessing SimpleCondition fields: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean evaluateCompositeCondition(RuleContext context, RuleCondition condition) {
        try {
            // Use reflection to access private fields
            Field logicalOperatorField = condition.getClass().getDeclaredField("logicalOperator");
            logicalOperatorField.setAccessible(true);
            LogicalOperator logicalOperator = (LogicalOperator) logicalOperatorField.get(condition);
            
            Field conditionsField = condition.getClass().getDeclaredField("conditions");
            conditionsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.List<RuleCondition> conditions = (java.util.List<RuleCondition>) conditionsField.get(condition);
            
            switch (logicalOperator) {
                case AND:
                    return conditions.stream()
                        .allMatch(c -> evaluate(context, c));
                case OR:
                    return conditions.stream()
                        .anyMatch(c -> evaluate(context, c));
                case NOT:
                    if (conditions.size() != 1) {
                        logger.warn("NOT operator should have exactly one condition");
                        return false;
                    }
                    return !evaluate(context, conditions.get(0));
                default:
                    logger.warn("Unsupported logical operator: {}", logicalOperator);
                    return false;
            }
        } catch (Exception e) {
            logger.error("Error accessing CompositeCondition fields: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean compareValues(Object actualValue, ComparisonOperator operator, Object expectedValue, boolean ignoreCase) {
        switch (operator) {
            case EQUALS:
                return equals(actualValue, expectedValue, ignoreCase);
            case NOT_EQUALS:
                return !equals(actualValue, expectedValue, ignoreCase);
            case GREATER_THAN:
                return compare(actualValue, expectedValue) > 0;
            case GREATER_THAN_OR_EQUAL:
                return compare(actualValue, expectedValue) >= 0;
            case LESS_THAN:
                return compare(actualValue, expectedValue) < 0;
            case LESS_THAN_OR_EQUAL:
                return compare(actualValue, expectedValue) <= 0;
            case CONTAINS:
                return contains(actualValue, expectedValue, ignoreCase);
            case NOT_CONTAINS:
                return !contains(actualValue, expectedValue, ignoreCase);
            case STARTS_WITH:
                return startsWith(actualValue, expectedValue, ignoreCase);
            case ENDS_WITH:
                return endsWith(actualValue, expectedValue, ignoreCase);
            case MATCHES:
                return matches(actualValue, expectedValue);
            case IN:
                return in(actualValue, expectedValue);
            case NOT_IN:
                return !in(actualValue, expectedValue);
            case BETWEEN:
                return between(actualValue, expectedValue);
            case NOT_BETWEEN:
                return !between(actualValue, expectedValue);
            case IS_NULL:
                return actualValue == null;
            case IS_NOT_NULL:
                return actualValue != null;
            case IS_TRUE:
                return Boolean.TRUE.equals(actualValue);
            case IS_FALSE:
                return Boolean.FALSE.equals(actualValue);
            default:
                logger.warn("Unsupported comparison operator: {}", operator);
                return false;
        }
    }
    
    private boolean equals(Object actual, Object expected, boolean ignoreCase) {
        if (actual == null && expected == null) {
            return true;
        }
        if (actual == null || expected == null) {
            return false;
        }
        
        if (ignoreCase && actual instanceof String && expected instanceof String) {
            return ((String) actual).equalsIgnoreCase((String) expected);
        }
        
        return actual.equals(expected);
    }
    
    @SuppressWarnings("unchecked")
    private int compare(Object actual, Object expected) {
        if (actual == null || expected == null) {
            throw new IllegalArgumentException("Cannot compare null values");
        }
        
        // Handle numeric comparisons
        if (actual instanceof Number && expected instanceof Number) {
            BigDecimal actualDecimal = new BigDecimal(actual.toString());
            BigDecimal expectedDecimal = new BigDecimal(expected.toString());
            return actualDecimal.compareTo(expectedDecimal);
        }
        
        // Handle date comparisons
        if (actual instanceof LocalDateTime && expected instanceof LocalDateTime) {
            return ((LocalDateTime) actual).compareTo((LocalDateTime) expected);
        }
        
        if (actual instanceof LocalDate && expected instanceof LocalDate) {
            return ((LocalDate) actual).compareTo((LocalDate) expected);
        }
        
        // Handle string comparisons
        if (actual instanceof String && expected instanceof String) {
            return ((String) actual).compareTo((String) expected);
        }
        
        // Generic comparable
        if (actual instanceof Comparable && expected instanceof Comparable && 
            actual.getClass().equals(expected.getClass())) {
            return ((Comparable<Object>) actual).compareTo(expected);
        }
        
        throw new IllegalArgumentException("Cannot compare values of types: " + 
                                         actual.getClass() + " and " + expected.getClass());
    }
    
    private boolean contains(Object actual, Object expected, boolean ignoreCase) {
        if (actual == null || expected == null) {
            return false;
        }
        
        String actualStr = actual.toString();
        String expectedStr = expected.toString();
        
        if (ignoreCase) {
            return actualStr.toLowerCase().contains(expectedStr.toLowerCase());
        }
        
        return actualStr.contains(expectedStr);
    }
    
    private boolean startsWith(Object actual, Object expected, boolean ignoreCase) {
        if (actual == null || expected == null) {
            return false;
        }
        
        String actualStr = actual.toString();
        String expectedStr = expected.toString();
        
        if (ignoreCase) {
            return actualStr.toLowerCase().startsWith(expectedStr.toLowerCase());
        }
        
        return actualStr.startsWith(expectedStr);
    }
    
    private boolean endsWith(Object actual, Object expected, boolean ignoreCase) {
        if (actual == null || expected == null) {
            return false;
        }
        
        String actualStr = actual.toString();
        String expectedStr = expected.toString();
        
        if (ignoreCase) {
            return actualStr.toLowerCase().endsWith(expectedStr.toLowerCase());
        }
        
        return actualStr.endsWith(expectedStr);
    }
    
    private boolean matches(Object actual, Object expected) {
        if (actual == null || expected == null) {
            return false;
        }
        
        String actualStr = actual.toString();
        String pattern = expected.toString();
        
        try {
            return Pattern.matches(pattern, actualStr);
        } catch (Exception e) {
            logger.warn("Invalid regex pattern '{}': {}", pattern, e.getMessage());
            return false;
        }
    }
    
    private boolean in(Object actual, Object expected) {
        if (actual == null || expected == null) {
            return false;
        }
        
        if (expected instanceof Collection) {
            return ((Collection<?>) expected).contains(actual);
        }
        
        if (expected instanceof Object[]) {
            Object[] array = (Object[]) expected;
            for (Object item : array) {
                if (actual.equals(item)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean between(Object actual, Object expected) {
        if (actual == null || expected == null) {
            return false;
        }
        
        if (!(expected instanceof Object[]) || ((Object[]) expected).length != 2) {
            logger.warn("BETWEEN operator requires an array of two values");
            return false;
        }
        
        Object[] range = (Object[]) expected;
        Object min = range[0];
        Object max = range[1];
        
        try {
            return compare(actual, min) >= 0 && compare(actual, max) <= 0;
        } catch (Exception e) {
            logger.warn("Error comparing values for BETWEEN operation: {}", e.getMessage());
            return false;
        }
    }
}