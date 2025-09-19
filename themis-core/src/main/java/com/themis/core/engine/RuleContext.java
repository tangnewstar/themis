package com.themis.core.engine;

import java.util.Map;
import java.util.Set;

/**
 * Interface representing the execution context for rule evaluation.
 * Contains input data, variables, and execution settings.
 */
public interface RuleContext {
    
    /**
     * Gets an attribute value from the context.
     * 
     * @param key The attribute key
     * @return The attribute value, or null if not found
     */
    Object getAttribute(String key);
    
    /**
     * Gets an attribute value with a specific type.
     * 
     * @param key The attribute key
     * @param type The expected type of the attribute
     * @param <T> The type parameter
     * @return The typed attribute value, or null if not found or type mismatch
     */
    <T> T getAttribute(String key, Class<T> type);
    
    /**
     * Sets an attribute value in the context.
     * 
     * @param key The attribute key
     * @param value The attribute value
     */
    void setAttribute(String key, Object value);
    
    /**
     * Removes an attribute from the context.
     * 
     * @param key The attribute key
     * @return The removed value, or null if not found
     */
    Object removeAttribute(String key);
    
    /**
     * Gets all attributes as an immutable map.
     * 
     * @return Map of all attributes
     */
    Map<String, Object> getAllAttributes();
    
    /**
     * Gets all attribute keys.
     * 
     * @return Set of all attribute keys
     */
    Set<String> getAttributeKeys();
    
    /**
     * Checks if an attribute exists in the context.
     * 
     * @param key The attribute key
     * @return true if the attribute exists, false otherwise
     */
    boolean hasAttribute(String key);
    
    /**
     * Clears all attributes from the context.
     */
    void clearAttributes();
    
    /**
     * Gets the execution mode for this context.
     * 
     * @return The execution mode
     */
    RuleExecutionMode getExecutionMode();
    
    /**
     * Sets the execution mode for this context.
     * 
     * @param mode The execution mode
     */
    void setExecutionMode(RuleExecutionMode mode);
    
    /**
     * Gets the execution settings for this context.
     * 
     * @return The execution settings
     */
    ExecutionSettings getExecutionSettings();
    
    /**
     * Sets the execution settings for this context.
     * 
     * @param settings The execution settings
     */
    void setExecutionSettings(ExecutionSettings settings);
    
    /**
     * Creates a copy of this context with the same attributes and settings.
     * 
     * @return A new context instance with copied data
     */
    RuleContext copy();
    
    /**
     * Creates a child context that inherits from this context.
     * Changes in the child context don't affect the parent.
     * 
     * @return A new child context
     */
    RuleContext createChildContext();
    
    /**
     * Gets the parent context if this is a child context.
     * 
     * @return The parent context, or null if this is a root context
     */
    RuleContext getParentContext();
    
    /**
     * Gets the unique identifier for this context instance.
     * 
     * @return Context ID
     */
    String getContextId();
    
    /**
     * Gets the timestamp when this context was created.
     * 
     * @return Creation timestamp in milliseconds
     */
    long getCreationTime();
}