package com.themis.engine.optimization;

import com.themis.core.engine.DefaultRuleContext;
import com.themis.core.engine.RuleContext;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 规则上下文对象池
 * 通过对象池化减少对象创建和GC压力
 */
public class RuleContextPool {
    
    private final BlockingQueue<RuleContext> pool;
    private final int maxPoolSize;
    private final AtomicInteger createdCount = new AtomicInteger(0);
    private final AtomicInteger borrowedCount = new AtomicInteger(0);
    private final AtomicInteger returnedCount = new AtomicInteger(0);
    
    public RuleContextPool() {
        this(100); // 默认池大小100
    }
    
    public RuleContextPool(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        this.pool = new ArrayBlockingQueue<>(maxPoolSize);
        
        // 预创建一部分对象
        int initialSize = Math.min(10, maxPoolSize);
        for (int i = 0; i < initialSize; i++) {
            pool.offer(createNewContext());
        }
    }
    
    /**
     * 从池中借用上下文对象
     * 
     * @return 规则上下文对象
     */
    public RuleContext borrow() {
        RuleContext context = pool.poll();
        if (context == null) {
            context = createNewContext();
        } else {
            // 清理借用的对象
            context.clearAttributes();
        }
        
        borrowedCount.incrementAndGet();
        return context;
    }
    
    /**
     * 带超时的借用
     * 
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 规则上下文对象，超时返回null
     * @throws InterruptedException 线程中断时抛出
     */
    public RuleContext borrow(long timeout, TimeUnit unit) throws InterruptedException {
        RuleContext context = pool.poll(timeout, unit);
        if (context == null) {
            context = createNewContext();
        } else {
            context.clearAttributes();
        }
        
        borrowedCount.incrementAndGet();
        return context;
    }
    
    /**
     * 归还上下文对象到池中
     * 
     * @param context 规则上下文对象
     */
    public void returnObject(RuleContext context) {
        if (context == null) {
            return;
        }
        
        // 清理对象状态
        context.clearAttributes();
        
        // 尝试放回池中
        boolean returned = pool.offer(context);
        if (returned) {
            returnedCount.incrementAndGet();
        }
        // 如果池已满，对象会被GC回收
    }
    
    /**
     * 创建新的上下文对象
     */
    private RuleContext createNewContext() {
        createdCount.incrementAndGet();
        return new DefaultRuleContext();
    }
    
    /**
     * 清空池
     */
    public void clear() {
        pool.clear();
    }
    
    /**
     * 获取池统计信息
     */
    public PoolStats getStats() {
        return new PoolStats(
            pool.size(),
            maxPoolSize,
            createdCount.get(),
            borrowedCount.get(),
            returnedCount.get()
        );
    }
    
    /**
     * 池统计信息
     */
    public static class PoolStats {
        private final int currentSize;
        private final int maxSize;
        private final int createdCount;
        private final int borrowedCount;
        private final int returnedCount;
        
        public PoolStats(int currentSize, int maxSize, int createdCount, 
                        int borrowedCount, int returnedCount) {
            this.currentSize = currentSize;
            this.maxSize = maxSize;
            this.createdCount = createdCount;
            this.borrowedCount = borrowedCount;
            this.returnedCount = returnedCount;
        }
        
        public int getCurrentSize() {
            return currentSize;
        }
        
        public int getMaxSize() {
            return maxSize;
        }
        
        public int getCreatedCount() {
            return createdCount;
        }
        
        public int getBorrowedCount() {
            return borrowedCount;
        }
        
        public int getReturnedCount() {
            return returnedCount;
        }
        
        public double getHitRate() {
            return borrowedCount == 0 ? 0.0 : 
                (double) (borrowedCount - createdCount) / borrowedCount;
        }
        
        @Override
        public String toString() {
            return String.format("PoolStats{current=%d, max=%d, created=%d, " +
                               "borrowed=%d, returned=%d, hitRate=%.2f%%}",
                               currentSize, maxSize, createdCount, borrowedCount, 
                               returnedCount, getHitRate() * 100);
        }
    }
}
