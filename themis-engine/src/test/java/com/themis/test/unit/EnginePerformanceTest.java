package com.themis.test.unit;

import com.themis.core.model.*;
import com.themis.engine.impl.LightweightRuleEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 引擎性能单元测试
 * 
 * 测试覆盖:
 * - 引擎执行性能
 * - 表达式预编译效果
 * - 批量执行性能
 */
public class EnginePerformanceTest {
    
    private LightweightRuleEngine engine;
    private ExpressionRule testRule;
    
    @BeforeEach
    public void setUp() {
        engine = new LightweightRuleEngine();
        
        testRule = new ExpressionRule("PERF_001", "性能测试规则");
        testRule.setCondition(new SimpleCondition("amount > 1000 && userId != null"));
        testRule.addAction(new SetVariableAction("result", "PASS"));
    }
    
    @Test
    public void testSingleRuleExecution() {
        // 测试单次执行性能
        long startTime = System.nanoTime();
        
        RuleExecutionResult result = engine.execute(testRule, createTestContext());
        
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒
        
        assertTrue(result.isSuccess());
        assertTrue(duration < 100, "执行时间应小于100ms,实际: " + duration + "ms");
    }
    
    @Test
    public void testBatchRuleExecution() {
        // 测试批量执行性能
        int iterations = 1000;
        long totalTime = 0;
        
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            engine.execute(testRule, createTestContext());
            long end = System.nanoTime();
            totalTime += (end - start);
        }
        
        double avgTime = totalTime / iterations / 1_000_000.0; // 平均时间(毫秒)
        
        assertTrue(avgTime < 10, "平均执行时间应小于10ms,实际: " + avgTime + "ms");
        System.out.println("批量执行平均时间: " + avgTime + "ms");
    }
    
    @Test
    public void testExpressionPrecompilation() {
        // 测试表达式预编译效果
        
        // 第一次执行(包含编译时间)
        long firstExecution = measureExecutionTime(() -> 
            engine.execute(testRule, createTestContext())
        );
        
        // 第二次执行(使用预编译结果)
        long secondExecution = measureExecutionTime(() -> 
            engine.execute(testRule, createTestContext())
        );
        
        System.out.println("首次执行: " + firstExecution + "μs");
        System.out.println("二次执行: " + secondExecution + "μs");
        
        // 预编译应该让后续执行更快
        assertTrue(secondExecution <= firstExecution, 
            "预编译后执行应该更快或相同");
    }
    
    @Test
    public void testConcurrentExecution() throws InterruptedException {
        // 测试并发执行
        int threadCount = 10;
        int iterationsPerThread = 100;
        
        java.util.concurrent.CountDownLatch latch = 
            new java.util.concurrent.CountDownLatch(threadCount);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < iterationsPerThread; j++) {
                    engine.execute(testRule, createTestContext());
                }
                latch.countDown();
            }).start();
        }
        
        latch.await();
        long endTime = System.currentTimeMillis();
        
        long totalExecutions = threadCount * iterationsPerThread;
        double throughput = totalExecutions / ((endTime - startTime) / 1000.0);
        
        System.out.println("并发吞吐量: " + throughput + " ops/sec");
        assertTrue(throughput > 1000, "吞吐量应大于1000 ops/sec");
    }
    
    private RuleExecutionContext createTestContext() {
        RuleExecutionContext context = new RuleExecutionContext();
        context.setAttribute("amount", 2000.0);
        context.setAttribute("userId", "user123");
        return context;
    }
    
    private long measureExecutionTime(Runnable task) {
        long start = System.nanoTime();
        task.run();
        return (System.nanoTime() - start) / 1000; // 返回微秒
    }
}
