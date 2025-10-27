package com.themis.api.sdk;

import com.themis.core.engine.*;
import com.themis.core.model.RuleDefinition;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Themis规则引擎SDK客户端
 * 提供同步和异步API调用
 */
public class ThemisRuleClient {
    
    private final RuleEngine ruleEngine;
    private final String baseUrl;
    private final ClientConfig config;
    
    public ThemisRuleClient(RuleEngine ruleEngine) {
        this(ruleEngine, null, ClientConfig.defaultConfig());
    }
    
    public ThemisRuleClient(String baseUrl, ClientConfig config) {
        this(null, baseUrl, config);
    }
    
    private ThemisRuleClient(RuleEngine ruleEngine, String baseUrl, ClientConfig config) {
        this.ruleEngine = ruleEngine;
        this.baseUrl = baseUrl;
        this.config = config;
    }
    
    /**
     * 同步执行规则
     */
    public RuleExecutionResult executeSync(RuleDefinition rule, RuleContext context) 
            throws RuleExecutionException {
        if (ruleEngine != null) {
            return ruleEngine.execute(context, rule);
        } else {
            // HTTP调用
            return executeViaHttp(rule, context);
        }
    }
    
    /**
     * 异步执行规则
     */
    public CompletableFuture<RuleExecutionResult> executeAsync(RuleDefinition rule, 
                                                                RuleContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeSync(rule, context);
            } catch (RuleExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * 批量同步执行
     */
    public BatchExecutionResult executeBatchSync(List<RuleDefinition> rules, RuleContext context)
            throws RuleExecutionException {
        if (ruleEngine != null) {
            return ruleEngine.executeBatch(context, rules);
        } else {
            return executeBatchViaHttp(rules, context);
        }
    }
    
    /**
     * 批量异步执行
     */
    public CompletableFuture<BatchExecutionResult> executeBatchAsync(List<RuleDefinition> rules,
                                                                      RuleContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeBatchSync(rules, context);
            } catch (RuleExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * 通过HTTP执行规则
     */
    private RuleExecutionResult executeViaHttp(RuleDefinition rule, RuleContext context) {
        // HTTP调用实现(简化版)
        throw new UnsupportedOperationException("HTTP client not implemented");
    }
    
    /**
     * 通过HTTP批量执行
     */
    private BatchExecutionResult executeBatchViaHttp(List<RuleDefinition> rules, 
                                                     RuleContext context) {
        throw new UnsupportedOperationException("HTTP client not implemented");
    }
    
    /**
     * 客户端配置
     */
    public static class ClientConfig {
        private int timeout = 5000;
        private int maxRetries = 3;
        private boolean enableCache = true;
        
        public static ClientConfig defaultConfig() {
            return new ClientConfig();
        }
        
        public int getTimeout() {
            return timeout;
        }
        
        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
        
        public int getMaxRetries() {
            return maxRetries;
        }
        
        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }
        
        public boolean isEnableCache() {
            return enableCache;
        }
        
        public void setEnableCache(boolean enableCache) {
            this.enableCache = enableCache;
        }
    }
}
