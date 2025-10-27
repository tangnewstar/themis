package com.themis.core.factor;

/**
 * 因子计算异常
 */
public class FactorComputeException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final String factorId;
    
    public FactorComputeException(String factorId, String message) {
        super(String.format("Factor '%s' compute failed: %s", factorId, message));
        this.factorId = factorId;
    }
    
    public FactorComputeException(String factorId, String message, Throwable cause) {
        super(String.format("Factor '%s' compute failed: %s", factorId, message), cause);
        this.factorId = factorId;
    }
    
    public FactorComputeException(String factorId, Throwable cause) {
        super(String.format("Factor '%s' compute failed", factorId), cause);
        this.factorId = factorId;
    }
    
    public String getFactorId() {
        return factorId;
    }
}
