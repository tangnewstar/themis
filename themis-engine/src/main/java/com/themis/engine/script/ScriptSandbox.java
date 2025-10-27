package com.themis.engine.script;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 脚本沙箱
 * 提供安全隔离和资源限制
 */
public class ScriptSandbox {
    
    /**
     * 沙箱配置
     */
    private final SandboxConfig config;
    
    /**
     * 黑名单类（禁止访问的类）
     */
    private final Set<String> blacklistedClasses;
    
    /**
     * 白名单类（允许访问的类）
     */
    private final Set<String> whitelistedClasses;
    
    public ScriptSandbox(SandboxConfig config) {
        this.config = config;
        this.blacklistedClasses = new HashSet<>(config.getBlacklistedClasses());
        this.whitelistedClasses = new HashSet<>(config.getWhitelistedClasses());
        
        // 默认黑名单
        addDefaultBlacklist();
    }
    
    /**
     * 添加默认黑名单
     */
    private void addDefaultBlacklist() {
        // 禁止反射
        blacklistedClasses.add("java.lang.reflect.Method");
        blacklistedClasses.add("java.lang.reflect.Field");
        blacklistedClasses.add("java.lang.reflect.Constructor");
        
        // 禁止类加载
        blacklistedClasses.add("java.lang.ClassLoader");
        
        // 禁止文件操作
        blacklistedClasses.add("java.io.File");
        blacklistedClasses.add("java.io.FileInputStream");
        blacklistedClasses.add("java.io.FileOutputStream");
        
        // 禁止网络操作
        blacklistedClasses.add("java.net.Socket");
        blacklistedClasses.add("java.net.ServerSocket");
        blacklistedClasses.add("java.net.URL");
        
        // 禁止进程操作
        blacklistedClasses.add("java.lang.Runtime");
        blacklistedClasses.add("java.lang.ProcessBuilder");
        
        // 禁止系统操作
        blacklistedClasses.add("java.lang.System");
    }
    
    /**
     * 检查类是否允许访问
     */
    public boolean isClassAllowed(String className) {
        // 白名单优先
        if (!whitelistedClasses.isEmpty()) {
            return whitelistedClasses.contains(className);
        }
        
        // 检查黑名单
        return !blacklistedClasses.contains(className);
    }
    
    /**
     * 执行脚本（带超时控制）
     */
    public <T> T executeWithTimeout(ScriptExecutor<T> executor) throws Exception {
        if (config.getTimeoutSeconds() <= 0) {
            return executor.execute();
        }
        
        // TODO: 实现超时控制
        // 可以使用Future.get(timeout)或自定义超时机制
        return executor.execute();
    }
    
    /**
     * 检查内存使用
     */
    public boolean checkMemoryLimit() {
        if (config.getMaxMemoryMB() <= 0) {
            return true;
        }
        
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        
        return usedMemory < config.getMaxMemoryMB();
    }
    
    /**
     * 脚本执行器接口
     */
    @FunctionalInterface
    public interface ScriptExecutor<T> {
        T execute() throws Exception;
    }
    
    /**
     * 沙箱配置
     */
    public static class SandboxConfig {
        /**
         * 超时时间（秒）
         */
        private int timeoutSeconds = 10;
        
        /**
         * 最大内存（MB）
         */
        private long maxMemoryMB = 512;
        
        /**
         * 黑名单类
         */
        private List<String> blacklistedClasses = new ArrayList<>();
        
        /**
         * 白名单类
         */
        private List<String> whitelistedClasses = new ArrayList<>();
        
        /**
         * 是否启用安全管理器
         */
        private boolean enableSecurityManager = false;
        
        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }
        
        public SandboxConfig setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }
        
        public long getMaxMemoryMB() {
            return maxMemoryMB;
        }
        
        public SandboxConfig setMaxMemoryMB(long maxMemoryMB) {
            this.maxMemoryMB = maxMemoryMB;
            return this;
        }
        
        public List<String> getBlacklistedClasses() {
            return blacklistedClasses;
        }
        
        public SandboxConfig setBlacklistedClasses(List<String> blacklistedClasses) {
            this.blacklistedClasses = blacklistedClasses;
            return this;
        }
        
        public List<String> getWhitelistedClasses() {
            return whitelistedClasses;
        }
        
        public SandboxConfig setWhitelistedClasses(List<String> whitelistedClasses) {
            this.whitelistedClasses = whitelistedClasses;
            return this;
        }
        
        public boolean isEnableSecurityManager() {
            return enableSecurityManager;
        }
        
        public SandboxConfig setEnableSecurityManager(boolean enableSecurityManager) {
            this.enableSecurityManager = enableSecurityManager;
            return this;
        }
    }
}
