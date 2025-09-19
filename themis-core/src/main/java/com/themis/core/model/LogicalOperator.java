package com.themis.core.model;

/**
 * Enumeration of logical operators for combining conditions.
 */
public enum LogicalOperator {
    
    /**
     * Logical AND - all conditions must be true.
     */
    AND("AND", "&&", "All conditions must be true"),
    
    /**
     * Logical OR - at least one condition must be true.
     */
    OR("OR", "||", "At least one condition must be true"),
    
    /**
     * Logical NOT - negates the result of conditions.
     */
    NOT("NOT", "!", "Negates the result");
    
    private final String keyword;
    private final String symbol;
    private final String description;
    
    LogicalOperator(String keyword, String symbol, String description) {
        this.keyword = keyword;
        this.symbol = symbol;
        this.description = description;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this is a binary operator (requires two operands).
     */
    public boolean isBinary() {
        return this == AND || this == OR;
    }
    
    /**
     * Checks if this is a unary operator (requires one operand).
     */
    public boolean isUnary() {
        return this == NOT;
    }
    
    /**
     * Parses a string representation to get the corresponding logical operator.
     */
    public static LogicalOperator fromString(String str) {
        if (str == null) {
            return null;
        }
        
        String normalized = str.trim().toLowerCase();
        
        for (LogicalOperator operator : values()) {
            if (operator.keyword.equalsIgnoreCase(str) || 
                operator.symbol.equals(str) ||
                operator.name().toLowerCase().equals(normalized)) {
                return operator;
            }
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return keyword;
    }
}