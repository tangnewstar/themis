package com.themis.orchestration.workflow;

/**
 * General exception for Themis orchestration module.
 */
public class ThemisException extends RuntimeException {
    
    public ThemisException(String message) {
        super(message);
    }
    
    public ThemisException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ThemisException(Throwable cause) {
        super(cause);
    }
}