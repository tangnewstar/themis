package com.themis.core.engine;

/**
 * Runtime exception thrown when rule execution fails.
 */
public class RuleExecutionException extends RuntimeException {
    
    private final String ruleId;
    private final String ruleName;
    private final String errorCode;
    
    public RuleExecutionException(String message) {
        super(message);
        this.ruleId = null;
        this.ruleName = null;
        this.errorCode = null;
    }
    
    public RuleExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.ruleId = null;
        this.ruleName = null;
        this.errorCode = null;
    }
    
    public RuleExecutionException(String ruleId, String ruleName, String message) {
        super(message);
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.errorCode = null;
    }
    
    public RuleExecutionException(String ruleId, String ruleName, String message, Throwable cause) {
        super(message, cause);
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.errorCode = null;
    }
    
    public RuleExecutionException(String ruleId, String ruleName, String errorCode, String message) {
        super(message);
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.errorCode = errorCode;
    }
    
    public RuleExecutionException(String ruleId, String ruleName, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.errorCode = errorCode;
    }
    
    public String getRuleId() {
        return ruleId;
    }
    
    public String getRuleName() {
        return ruleName;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RuleExecutionException");
        
        if (ruleId != null || ruleName != null) {
            sb.append(" [");
            if (ruleId != null) {
                sb.append("ruleId=").append(ruleId);
            }
            if (ruleName != null) {
                if (ruleId != null) {
                    sb.append(", ");
                }
                sb.append("ruleName=").append(ruleName);
            }
            if (errorCode != null) {
                sb.append(", errorCode=").append(errorCode);
            }
            sb.append("]");
        }
        
        sb.append(": ").append(getMessage());
        return sb.toString();
    }
}