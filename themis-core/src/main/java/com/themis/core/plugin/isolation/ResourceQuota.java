package com.themis.core.plugin.isolation;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 资源配额管理
 * 限制插件的CPU、内存、线程等资源使用
 */
public class ResourceQuota {
    
    /**
     * 插件ID
     */
    private final String pluginId;
    
    /**
     * 最大内存使用（字节）
     */
    private final long maxMemoryBytes;
    
    /**
     * 最大线程数
     */
    private final int maxThreads;
    
    /**
     * 最大执行时间（毫秒）
     */
    private final long maxExecutionTimeMillis;
    
    /**
     * 当前内存使用
     */
    private final AtomicLong currentMemoryUsage = new AtomicLong(0);
    
    /**
     * 当前线程数
     */
    private final AtomicLong currentThreadCount = new AtomicLong(0);
    
    /**
     * 执行次数统计
     */
    private final AtomicLong executionCount = new AtomicLong(0);
    
    /**
     * 超限次数
     */
    private final AtomicLong quotaViolationCount = new AtomicLong(0);
    
    public ResourceQuota(String pluginId, long maxMemoryBytes, int maxThreads, 
                        long maxExecutionTimeMillis) {
        this.pluginId = pluginId;
        this.maxMemoryBytes = maxMemoryBytes;
        this.maxThreads = maxThreads;
        this.maxExecutionTimeMillis = maxExecutionTimeMillis;
    }
    
    /**
     * 检查是否可以分配内存
     */
    public boolean canAllocateMemory(long bytes) {
        long newUsage = currentMemoryUsage.get() + bytes;
        return newUsage <= maxMemoryBytes;
    }
    
    /**
     * 分配内存
     */
    public boolean allocateMemory(long bytes) {
        if (canAllocateMemory(bytes)) {
            currentMemoryUsage.addAndGet(bytes);
            return true;
        }
        
        quotaViolationCount.incrementAndGet();
        return false;
    }
    
    /**
     * 释放内存
     */
    public void releaseMemory(long bytes) {
        currentMemoryUsage.addAndGet(-bytes);
    }
    
    /**
     * 检查是否可以创建线程
     */
    public boolean canCreateThread() {
        return currentThreadCount.get() < maxThreads;
    }
    
    /**
     * 增加线程计数
     */
    public boolean incrementThreadCount() {
        if (canCreateThread()) {
            currentThreadCount.incrementAndGet();
            return true;
        }
        
        quotaViolationCount.incrementAndGet();
        return false;
    }
    
    /**
     * 减少线程计数
     */
    public void decrementThreadCount() {
        currentThreadCount.decrementAndGet();
    }
    
    /**
     * 记录执行
     */
    public void recordExecution() {
        executionCount.incrementAndGet();
    }
    
    /**
     * 检查执行时间
     */
    public boolean checkExecutionTime(long executionTimeMillis) {
        if (executionTimeMillis > maxExecutionTimeMillis) {
            quotaViolationCount.incrementAndGet();
            return false;
        }
        return true;
    }
    
    /**
     * 重置统计
     */
    public void reset() {
        currentMemoryUsage.set(0);
        currentThreadCount.set(0);
        executionCount.set(0);
        quotaViolationCount.set(0);
    }
    
    /**
     * 获取资源使用情况
     */
    public ResourceUsage getUsage() {
        return new ResourceUsage(
            currentMemoryUsage.get(),
            maxMemoryBytes,
            (int) currentThreadCount.get(),
            maxThreads,
            executionCount.get(),
            quotaViolationCount.get()
        );
    }
    
    /**
     * 资源使用情况
     */
    public static class ResourceUsage {
        private final long currentMemory;
        private final long maxMemory;
        private final int currentThreads;
        private final int maxThreads;
        private final long executionCount;
        private final long violationCount;
        
        public ResourceUsage(long currentMemory, long maxMemory, 
                           int currentThreads, int maxThreads,
                           long executionCount, long violationCount) {
            this.currentMemory = currentMemory;
            this.maxMemory = maxMemory;
            this.currentThreads = currentThreads;
            this.maxThreads = maxThreads;
            this.executionCount = executionCount;
            this.violationCount = violationCount;
        }
        
        public double getMemoryUsagePercent() {
            return maxMemory > 0 ? (double) currentMemory / maxMemory * 100 : 0;
        }
        
        public double getThreadUsagePercent() {
            return maxThreads > 0 ? (double) currentThreads / maxThreads * 100 : 0;
        }
        
        public long getCurrentMemory() {
            return currentMemory;
        }
        
        public long getMaxMemory() {
            return maxMemory;
        }
        
        public int getCurrentThreads() {
            return currentThreads;
        }
        
        public int getMaxThreads() {
            return maxThreads;
        }
        
        public long getExecutionCount() {
            return executionCount;
        }
        
        public long getViolationCount() {
            return violationCount;
        }
    }
}
