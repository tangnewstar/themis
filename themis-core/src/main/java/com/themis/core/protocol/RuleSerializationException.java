package com.themis.core.protocol;

/**
 * 规则序列化异常
 */
public class RuleSerializationException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public RuleSerializationException(String message) {
        super(message);
    }
    
    public RuleSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
