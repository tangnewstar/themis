package com.themis.engine.expression;

/**
 * 表达式编译异常
 */
public class ExpressionCompileException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final String expression;
    
    public ExpressionCompileException(String expression, String message) {
        super(String.format("Failed to compile expression '%s': %s", expression, message));
        this.expression = expression;
    }
    
    public ExpressionCompileException(String expression, String message, Throwable cause) {
        super(String.format("Failed to compile expression '%s': %s", expression, message), cause);
        this.expression = expression;
    }
    
    public ExpressionCompileException(String expression, Throwable cause) {
        super(String.format("Failed to compile expression '%s'", expression), cause);
        this.expression = expression;
    }
    
    public String getExpression() {
        return expression;
    }
}
