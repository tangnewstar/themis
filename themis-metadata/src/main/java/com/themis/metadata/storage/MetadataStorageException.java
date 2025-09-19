package com.themis.metadata.storage;

/**
 * Exception thrown when metadata storage operations fail.
 */
public class MetadataStorageException extends Exception {
    
    private final StorageErrorCode errorCode;
    private final String operation;
    
    public MetadataStorageException(String message) {
        super(message);
        this.errorCode = StorageErrorCode.UNKNOWN_ERROR;
        this.operation = null;
    }
    
    public MetadataStorageException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = StorageErrorCode.UNKNOWN_ERROR;
        this.operation = null;
    }
    
    public MetadataStorageException(StorageErrorCode errorCode, String operation, String message) {
        super(message);
        this.errorCode = errorCode;
        this.operation = operation;
    }
    
    public MetadataStorageException(StorageErrorCode errorCode, String operation, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.operation = operation;
    }
    
    public StorageErrorCode getErrorCode() {
        return errorCode;
    }
    
    public String getOperation() {
        return operation;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MetadataStorageException");
        
        if (errorCode != null) {
            sb.append(" [").append(errorCode).append("]");
        }
        
        if (operation != null) {
            sb.append(" during operation: ").append(operation);
        }
        
        sb.append(" - ").append(getMessage());
        return sb.toString();
    }
    
    /**
     * Enumeration of storage error codes.
     */
    public enum StorageErrorCode {
        CONNECTION_FAILED("Connection to storage backend failed"),
        TIMEOUT("Operation timed out"),
        PERMISSION_DENIED("Permission denied"),
        RESOURCE_NOT_FOUND("Resource not found"),
        RESOURCE_ALREADY_EXISTS("Resource already exists"),
        INVALID_DATA("Invalid data format"),
        STORAGE_FULL("Storage is full"),
        SERIALIZATION_ERROR("Data serialization/deserialization error"),
        VALIDATION_ERROR("Data validation error"),
        CONFIGURATION_ERROR("Storage configuration error"),
        UNKNOWN_ERROR("Unknown error");
        
        private final String description;
        
        StorageErrorCode(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}