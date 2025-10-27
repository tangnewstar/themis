package com.themis.core.protocol;

/**
 * 规则解析异常
 */
public class RuleParseException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final int line;
    private final int column;
    
    public RuleParseException(String message) {
        super(message);
        this.line = -1;
        this.column = -1;
    }
    
    public RuleParseException(String message, Throwable cause) {
        super(message, cause);
        this.line = -1;
        this.column = -1;
    }
    
    public RuleParseException(String message, int line, int column) {
        super(String.format("%s (line %d, column %d)", message, line, column));
        this.line = line;
        this.column = column;
    }
    
    public RuleParseException(String message, int line, int column, Throwable cause) {
        super(String.format("%s (line %d, column %d)", message, line, column), cause);
        this.line = line;
        this.column = column;
    }
    
    public int getLine() {
        return line;
    }
    
    public int getColumn() {
        return column;
    }
}
