package com.themis.core.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of RuleContext.
 * Thread-safe implementation using ConcurrentHashMap.
 */
public class DefaultRuleContext implements RuleContext {
    
    private final String contextId;
    private final long creationTime;
    private final Map<String, Object> attributes;
    private final RuleContext parentContext;
    
    private RuleExecutionMode executionMode = RuleExecutionMode.SEQUENTIAL;
    private ExecutionSettings executionSettings = ExecutionSettings.defaultSettings();
    
    // Default constructor - creates root context
    public DefaultRuleContext() {
        this.contextId = UUID.randomUUID().toString();
        this.creationTime = System.currentTimeMillis();
        this.attributes = new ConcurrentHashMap<>();
        this.parentContext = null;
    }
    
    // Constructor for child contexts
    public DefaultRuleContext(RuleContext parentContext) {
        this.contextId = UUID.randomUUID().toString();
        this.creationTime = System.currentTimeMillis();
        this.attributes = new ConcurrentHashMap<>();
        this.parentContext = parentContext;
        
        // Inherit execution settings from parent
        if (parentContext != null) {
            this.executionMode = parentContext.getExecutionMode();
            this.executionSettings = parentContext.getExecutionSettings();
        }
    }
    
    // Constructor with initial attributes
    public DefaultRuleContext(Map<String, Object> initialAttributes) {
        this();
        if (initialAttributes != null) {
            this.attributes.putAll(initialAttributes);
        }
    }
    
    @Override
    public Object getAttribute(String key) {
        if (key == null) {
            return null;
        }
        
        // Check local attributes first
        Object value = attributes.get(key);
        
        // If not found and has parent, check parent context
        if (value == null && parentContext != null) {
            value = parentContext.getAttribute(key);
        }
        
        return value;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = getAttribute(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    @Override
    public void setAttribute(String key, Object value) {
        if (key != null) {
            if (value == null) {
                attributes.remove(key);
            } else {
                attributes.put(key, value);
            }
        }
    }
    
    @Override
    public Object removeAttribute(String key) {
        return key != null ? attributes.remove(key) : null;
    }
    
    @Override
    public Map<String, Object> getAllAttributes() {
        Map<String, Object> allAttributes = new HashMap<>();
        
        // Start with parent attributes if available
        if (parentContext != null) {
            allAttributes.putAll(parentContext.getAllAttributes());
        }
        
        // Override with local attributes
        allAttributes.putAll(attributes);
        
        return Collections.unmodifiableMap(allAttributes);
    }
    
    @Override
    public Set<String> getAttributeKeys() {
        return Collections.unmodifiableSet(getAllAttributes().keySet());
    }
    
    @Override
    public boolean hasAttribute(String key) {
        return getAttribute(key) != null;
    }
    
    @Override
    public void clearAttributes() {
        attributes.clear();
    }
    
    @Override
    public RuleExecutionMode getExecutionMode() {
        return executionMode;
    }
    
    @Override
    public void setExecutionMode(RuleExecutionMode mode) {
        this.executionMode = mode != null ? mode : RuleExecutionMode.SEQUENTIAL;
    }
    
    @Override
    public ExecutionSettings getExecutionSettings() {
        return executionSettings;
    }
    
    @Override
    public void setExecutionSettings(ExecutionSettings settings) {
        this.executionSettings = settings != null ? settings : ExecutionSettings.defaultSettings();
    }
    
    @Override
    public RuleContext copy() {
        DefaultRuleContext copy = new DefaultRuleContext();
        copy.attributes.putAll(this.attributes);
        copy.executionMode = this.executionMode;
        copy.executionSettings = this.executionSettings;
        return copy;
    }
    
    @Override
    public RuleContext createChildContext() {
        return new DefaultRuleContext(this);
    }
    
    @Override
    public RuleContext getParentContext() {
        return parentContext;
    }
    
    @Override
    public String getContextId() {
        return contextId;
    }
    
    @Override
    public long getCreationTime() {
        return creationTime;
    }
    
    @Override
    public String toString() {
        return String.format("DefaultRuleContext{id='%s', attributes=%d, mode=%s}", 
                           contextId, attributes.size(), executionMode);
    }
}