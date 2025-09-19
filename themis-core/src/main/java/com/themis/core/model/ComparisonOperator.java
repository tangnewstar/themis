package com.themis.core.model;

/**
 * Enumeration of comparison operators for rule conditions.
 */
public enum ComparisonOperator {
    
    // Equality operators
    EQUALS("==", "equals", "Equal to"),
    NOT_EQUALS("!=", "not equals", "Not equal to"),
    
    // Comparison operators
    GREATER_THAN(">", "greater than", "Greater than"),
    GREATER_THAN_OR_EQUAL(">=", "greater than or equal", "Greater than or equal to"),
    LESS_THAN("<", "less than", "Less than"),
    LESS_THAN_OR_EQUAL("<=", "less than or equal", "Less than or equal to"),
    
    // String operators
    CONTAINS("contains", "contains", "Contains"),
    NOT_CONTAINS("not contains", "does not contain", "Does not contain"),
    STARTS_WITH("starts with", "starts with", "Starts with"),
    ENDS_WITH("ends with", "ends with", "Ends with"),
    MATCHES("matches", "matches", "Matches pattern"),
    
    // Collection operators
    IN("in", "in", "In collection"),
    NOT_IN("not in", "not in", "Not in collection"),
    
    // Range operators
    BETWEEN("between", "between", "Between values"),
    NOT_BETWEEN("not between", "not between", "Not between values"),
    
    // Null operators
    IS_NULL("is null", "is null", "Is null"),
    IS_NOT_NULL("is not null", "is not null", "Is not null"),
    
    // Boolean operators
    IS_TRUE("is true", "is true", "Is true"),
    IS_FALSE("is false", "is false", "Is false");
    
    private final String symbol;
    private final String keyword;
    private final String description;
    
    ComparisonOperator(String symbol, String keyword, String description) {
        this.symbol = symbol;
        this.keyword = keyword;
        this.description = description;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this operator requires a value for comparison.
     */
    public boolean requiresValue() {
        return this != IS_NULL && this != IS_NOT_NULL;
    }
    
    /**
     * Checks if this operator works with numeric values.
     */
    public boolean isNumericOperator() {
        return this == GREATER_THAN || this == GREATER_THAN_OR_EQUAL ||
               this == LESS_THAN || this == LESS_THAN_OR_EQUAL ||
               this == BETWEEN || this == NOT_BETWEEN;
    }
    
    /**
     * Checks if this operator works with string values.
     */
    public boolean isStringOperator() {
        return this == CONTAINS || this == NOT_CONTAINS ||
               this == STARTS_WITH || this == ENDS_WITH ||
               this == MATCHES;
    }
    
    /**
     * Checks if this operator works with collections.
     */
    public boolean isCollectionOperator() {
        return this == IN || this == NOT_IN;
    }
    
    /**
     * Checks if this operator works with boolean values.
     */
    public boolean isBooleanOperator() {
        return this == IS_TRUE || this == IS_FALSE;
    }
    
    /**
     * Checks if this operator checks for null values.
     */
    public boolean isNullOperator() {
        return this == IS_NULL || this == IS_NOT_NULL;
    }
    
    /**
     * Gets the inverse operator if one exists.
     */
    public ComparisonOperator getInverse() {
        switch (this) {
            case EQUALS: return NOT_EQUALS;
            case NOT_EQUALS: return EQUALS;
            case GREATER_THAN: return LESS_THAN_OR_EQUAL;
            case GREATER_THAN_OR_EQUAL: return LESS_THAN;
            case LESS_THAN: return GREATER_THAN_OR_EQUAL;
            case LESS_THAN_OR_EQUAL: return GREATER_THAN;
            case CONTAINS: return NOT_CONTAINS;
            case NOT_CONTAINS: return CONTAINS;
            case IN: return NOT_IN;
            case NOT_IN: return IN;
            case BETWEEN: return NOT_BETWEEN;
            case NOT_BETWEEN: return BETWEEN;
            case IS_NULL: return IS_NOT_NULL;
            case IS_NOT_NULL: return IS_NULL;
            case IS_TRUE: return IS_FALSE;
            case IS_FALSE: return IS_TRUE;
            default: return this;
        }
    }
    
    /**
     * Parses a string representation to get the corresponding operator.
     */
    public static ComparisonOperator fromString(String str) {
        if (str == null) {
            return null;
        }
        
        String normalized = str.trim().toLowerCase();
        
        for (ComparisonOperator operator : values()) {
            if (operator.symbol.equals(str) || 
                operator.keyword.equals(normalized) ||
                operator.name().toLowerCase().equals(normalized)) {
                return operator;
            }
        }
        
        return null;
    }
}