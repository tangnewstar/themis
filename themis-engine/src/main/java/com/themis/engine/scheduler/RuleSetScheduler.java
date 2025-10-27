package com.themis.engine.scheduler;

import com.themis.core.engine.*;
import com.themis.core.model.RuleDefinition;

import java.util.*;
import java.util.concurrent.*;

/**
 * 规则集调度器
 * 支持顺序、并行、分支执行模式
 */
public class RuleSetScheduler {
    
    private final ExecutorService executorService;
    private final RuleEngine ruleEngine;
    
    public RuleSetScheduler(RuleEngine ruleEngine) {
        this(ruleEngine, Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }
    
    public RuleSetScheduler(RuleEngine ruleEngine, ExecutorService executorService) {
        this.ruleEngine = ruleEngine;
        this.executorService = executorService;
    }
    
    /**
     * 顺序执行模式
     */
    public ScheduleResult executeSequential(List<RuleDefinition> rules, RuleContext context,
                                           SequentialStrategy strategy) {
        long startTime = System.currentTimeMillis();
        List<RuleExecutionResult> results = new ArrayList<>();
        
        for (RuleDefinition rule : rules) {
            try {
                RuleExecutionResult result = ruleEngine.execute(context, rule);
                results.add(result);
                
                // 应用策略
                if (strategy == SequentialStrategy.STOP_ON_ERROR && !result.isSuccess()) {
                    break;
                }
                if (strategy == SequentialStrategy.STOP_ON_MATCH && result.isSuccess()) {
                    break;
                }
            } catch (RuleExecutionException e) {
                if (strategy == SequentialStrategy.STOP_ON_ERROR) {
                    break;
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        return new ScheduleResult(ExecutionMode.SEQUENTIAL, results, endTime - startTime);
    }
    
    /**
     * 并行执行模式
     */
    public ScheduleResult executeParallel(List<RuleDefinition> rules, RuleContext context,
                                         ParallelStrategy strategy) {
        long startTime = System.currentTimeMillis();
        List<Future<RuleExecutionResult>> futures = new ArrayList<>();
        
        for (RuleDefinition rule : rules) {
            Future<RuleExecutionResult> future = executorService.submit(() -> {
                try {
                    return ruleEngine.execute(context.copy(), rule);
                } catch (RuleExecutionException e) {
                    return RuleExecutionResult.failure(rule.getRuleId(), e.getMessage());
                }
            });
            futures.add(future);
        }
        
        List<RuleExecutionResult> results = collectResults(futures, strategy);
        
        long endTime = System.currentTimeMillis();
        return new ScheduleResult(ExecutionMode.PARALLEL, results, endTime - startTime);
    }
    
    /**
     * 分支执行模式
     */
    public ScheduleResult executeBranch(Map<String, List<RuleDefinition>> branches, 
                                       RuleContext context, String branchCondition) {
        long startTime = System.currentTimeMillis();
        
        // 选择分支
        String selectedBranch = evaluateBranchCondition(branchCondition, context);
        List<RuleDefinition> selectedRules = branches.get(selectedBranch);
        
        if (selectedRules == null) {
            selectedRules = branches.get("default");
        }
        
        if (selectedRules == null) {
            return new ScheduleResult(ExecutionMode.BRANCH, Collections.emptyList(), 0);
        }
        
        // 执行选中的分支
        List<RuleExecutionResult> results = new ArrayList<>();
        for (RuleDefinition rule : selectedRules) {
            try {
                results.add(ruleEngine.execute(context, rule));
            } catch (RuleExecutionException e) {
                // 继续执行
            }
        }
        
        long endTime = System.currentTimeMillis();
        return new ScheduleResult(ExecutionMode.BRANCH, results, endTime - startTime);
    }
    
    /**
     * 收集并行执行结果
     */
    private List<RuleExecutionResult> collectResults(List<Future<RuleExecutionResult>> futures,
                                                     ParallelStrategy strategy) {
        List<RuleExecutionResult> results = new ArrayList<>();
        
        if (strategy == ParallelStrategy.WAIT_ALL) {
            for (Future<RuleExecutionResult> future : futures) {
                try {
                    results.add(future.get());
                } catch (Exception e) {
                    // 忽略异常
                }
            }
        } else if (strategy == ParallelStrategy.WAIT_ANY) {
            for (Future<RuleExecutionResult> future : futures) {
                try {
                    if (future.isDone()) {
                        results.add(future.get());
                        break;
                    }
                } catch (Exception e) {
                    // 忽略
                }
            }
        } else if (strategy == ParallelStrategy.TIMEOUT) {
            for (Future<RuleExecutionResult> future : futures) {
                try {
                    results.add(future.get(1000, TimeUnit.MILLISECONDS));
                } catch (TimeoutException e) {
                    future.cancel(true);
                } catch (Exception e) {
                    // 忽略
                }
            }
        }
        
        return results;
    }
    
    /**
     * 评估分支条件
     */
    private String evaluateBranchCondition(String condition, RuleContext context) {
        // 简单实现：从上下文获取值
        Object value = context.getAttribute(condition);
        return value != null ? value.toString() : "default";
    }
    
    /**
     * 关闭调度器
     */
    public void shutdown() {
        executorService.shutdown();
    }
    
    /**
     * 执行模式
     */
    public enum ExecutionMode {
        SEQUENTIAL, PARALLEL, BRANCH
    }
    
    /**
     * 顺序执行策略
     */
    public enum SequentialStrategy {
        EXECUTE_ALL,        // 执行所有规则
        STOP_ON_ERROR,      // 遇错停止
        STOP_ON_MATCH       // 匹配即停止
    }
    
    /**
     * 并行执行策略
     */
    public enum ParallelStrategy {
        WAIT_ALL,           // 等待所有完成
        WAIT_ANY,           // 任意一个完成
        TIMEOUT             // 超时返回
    }
}
