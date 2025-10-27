package com.themis.core.engine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 分层规则上下文实现
 * 支持RequestScope、SessionScope、FactorScope、GlobalScope四个作用域
 */
public class LayeredRuleContext implements RuleContext {
    
    private final String contextId;
    private final long creationTime;
    
    // 请求作用域 - 当前请求的实时参数
    private final Map<String, Object> requestScope;
    
    // 会话作用域 - 当前执行会话的中间状态
    private final Map<String, Object> sessionScope;
    
    // 因子作用域 - 计算得到的因子值
    private final Map<String, Object> factorScope;
    
    // 全局作用域 - 全局配置和缓存数据
    private static final Map<String, Object> globalScope = new ConcurrentHashMap<>();
    
    private RuleExecutionMode executionMode;
    private ExecutionSettings executionSettings;
    private RuleContext parentContext;
    
    public LayeredRuleContext() {
        this.contextId = UUID.randomUUID().toString();
        this.creationTime = System.currentTimeMillis();
        this.requestScope = new ConcurrentHashMap<>();
        this.sessionScope = new ConcurrentHashMap<>();
        this.factorScope = new ConcurrentHashMap<>();
        this.executionMode = RuleExecutionMode.SEQUENTIAL;
        this.executionSettings = new ExecutionSettings();
    }
    
    @Override
    public Object getAttribute(String key) {
        // 按优先级查找：Request -> Session -> Factor -> Global
        Object value = requestScope.get(key);
        if (value != null) return value;
        
        value = sessionScope.get(key);
        if (value != null) return value;
        
        value = factorScope.get(key);
        if (value != null) return value;
        
        return globalScope.get(key);
    }
    
    @Override
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = getAttribute(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }
    
    @Override
    public void setAttribute(String key, Object value) {
        // 默认设置到请求作用域
        setRequestAttribute(key, value);
    }
    
    /**
     * 设置请求作用域属性
     */
    public void setRequestAttribute(String key, Object value) {
        requestScope.put(key, value);
    }
    
    /**
     * 设置会话作用域属性
     */
    public void setSessionAttribute(String key, Object value) {
        sessionScope.put(key, value);
    }
    
    /**
     * 设置因子作用域属性
     */
    public void setFactorAttribute(String key, Object value) {
        factorScope.put(key, value);
    }
    
    /**
     * 设置全局作用域属性
     */
    public static void setGlobalAttribute(String key, Object value) {
        globalScope.put(key, value);
    }
    
    /**
     * 获取请求作用域属性
     */
    public Object getRequestAttribute(String key) {
        return requestScope.get(key);
    }
    
    /**
     * 获取会话作用域属性
     */
    public Object getSessionAttribute(String key) {
        return sessionScope.get(key);
    }
    
    /**
     * 获取因子作用域属性
     */
    public Object getFactorAttribute(String key) {
        return factorScope.get(key);
    }
    
    /**
     * 获取全局作用域属性
     */
    public static Object getGlobalAttribute(String key) {
        return globalScope.get(key);
    }
    
    @Override
    public Object removeAttribute(String key) {
        // 从所有作用域中移除
        Object value = requestScope.remove(key);
        if (value == null) value = sessionScope.remove(key);
        if (value == null) value = factorScope.remove(key);
        return value;
    }
    
    @Override
    public Map<String, Object> getAllAttributes() {
        Map<String, Object> allAttrs = new HashMap<>();
        allAttrs.putAll(globalScope);
        allAttrs.putAll(factorScope);
        allAttrs.putAll(sessionScope);
        allAttrs.putAll(requestScope);
        return Collections.unmodifiableMap(allAttrs);
    }
    
    /**
     * 获取请求作用域的所有属性
     */
    public Map<String, Object> getRequestAttributes() {
        return Collections.unmodifiableMap(requestScope);
    }
    
    /**
     * 获取会话作用域的所有属性
     */
    public Map<String, Object> getSessionAttributes() {
        return Collections.unmodifiableMap(sessionScope);
    }
    
    /**
     * 获取因子作用域的所有属性
     */
    public Map<String, Object> getFactorAttributes() {
        return Collections.unmodifiableMap(factorScope);
    }
    
    /**
     * 获取全局作用域的所有属性
     */
    public static Map<String, Object> getGlobalAttributes() {
        return Collections.unmodifiableMap(globalScope);
    }
    
    @Override
    public Set<String> getAttributeKeys() {
        Set<String> keys = new HashSet<>();
        keys.addAll(globalScope.keySet());
        keys.addAll(factorScope.keySet());
        keys.addAll(sessionScope.keySet());
        keys.addAll(requestScope.keySet());
        return keys;
    }
    
    @Override
    public boolean hasAttribute(String key) {
        return requestScope.containsKey(key) ||
               sessionScope.containsKey(key) ||
               factorScope.containsKey(key) ||
               globalScope.containsKey(key);
    }
    
    @Override
    public void clearAttributes() {
        requestScope.clear();
        sessionScope.clear();
        factorScope.clear();
    }
    
    /**
     * 清除请求作用域
     */
    public void clearRequestScope() {
        requestScope.clear();
    }
    
    /**
     * 清除会话作用域
     */
    public void clearSessionScope() {
        sessionScope.clear();
    }
    
    /**
     * 清除因子作用域
     */
    public void clearFactorScope() {
        factorScope.clear();
    }
    
    /**
     * 清除全局作用域
     */
    public static void clearGlobalScope() {
        globalScope.clear();
    }
    
    @Override
    public RuleExecutionMode getExecutionMode() {
        return executionMode;
    }
    
    @Override
    public void setExecutionMode(RuleExecutionMode mode) {
        this.executionMode = mode;
    }
    
    @Override
    public ExecutionSettings getExecutionSettings() {
        return executionSettings;
    }
    
    @Override
    public void setExecutionSettings(ExecutionSettings settings) {
        this.executionSettings = settings;
    }
    
    @Override
    public RuleContext copy() {
        LayeredRuleContext newContext = new LayeredRuleContext();
        newContext.requestScope.putAll(this.requestScope);
        newContext.sessionScope.putAll(this.sessionScope);
        newContext.factorScope.putAll(this.factorScope);
        newContext.executionMode = this.executionMode;
        newContext.executionSettings = this.executionSettings.copy();
        return newContext;
    }
    
    @Override
    public RuleContext createChildContext() {
        LayeredRuleContext childContext = new LayeredRuleContext();
        childContext.parentContext = this;
        // 子上下文继承父上下文的会话和因子作用域
        childContext.sessionScope.putAll(this.sessionScope);
        childContext.factorScope.putAll(this.factorScope);
        childContext.executionMode = this.executionMode;
        childContext.executionSettings = this.executionSettings.copy();
        return childContext;
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
    
    /**
     * 获取作用域统计信息
     */
    public ScopeStats getScopeStats() {
        return new ScopeStats(
            requestScope.size(),
            sessionScope.size(),
            factorScope.size(),
            globalScope.size()
        );
    }
    
    /**
     * 作用域统计信息
     */
    public static class ScopeStats {
        private final int requestScopeSize;
        private final int sessionScopeSize;
        private final int factorScopeSize;
        private final int globalScopeSize;
        
        public ScopeStats(int requestScopeSize, int sessionScopeSize, 
                         int factorScopeSize, int globalScopeSize) {
            this.requestScopeSize = requestScopeSize;
            this.sessionScopeSize = sessionScopeSize;
            this.factorScopeSize = factorScopeSize;
            this.globalScopeSize = globalScopeSize;
        }
        
        public int getRequestScopeSize() {
            return requestScopeSize;
        }
        
        public int getSessionScopeSize() {
            return sessionScopeSize;
        }
        
        public int getFactorScopeSize() {
            return factorScopeSize;
        }
        
        public int getGlobalScopeSize() {
            return globalScopeSize;
        }
        
        public int getTotalSize() {
            return requestScopeSize + sessionScopeSize + factorScopeSize + globalScopeSize;
        }
        
        @Override
        public String toString() {
            return String.format("ScopeStats{request=%d, session=%d, factor=%d, global=%d, total=%d}",
                               requestScopeSize, sessionScopeSize, factorScopeSize, 
                               globalScopeSize, getTotalSize());
        }
    }
    
    @Override
    public String toString() {
        return String.format("LayeredRuleContext{id='%s', stats=%s}", 
                           contextId, getScopeStats());
    }
}
