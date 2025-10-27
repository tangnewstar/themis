package com.themis.core.function;

/**
 * 函数执行异常
 */
public class FunctionExecutionException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final String functionName;
    
    public FunctionExecutionException(String functionName, String message) {
        super(String.format("Function '%s' execution failed: %s", functionName, message));
        this.functionName = functionName;
    }
    
    public FunctionExecutionException(String functionName, String message, Throwable cause) {
        super(String.format("Function '%s' execution failed: %s", functionName, message), cause);
        this.functionName = functionName;
    }
    
    public FunctionExecutionException(String functionName, Throwable cause) {
        super(String.format("Function '%s' execution failed", functionName), cause);
        this.functionName = functionName;
    }
    
    public String getFunctionName() {
        return functionName;
    }
}
