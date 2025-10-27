package com.themis.core.plugin.orchestration;

import com.themis.core.plugin.Plugin;
import com.themis.core.plugin.PluginContext;
import com.themis.core.plugin.PluginResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Predicate;

/**
 * 插件编排器
 * 支持顺序、并行、条件、循环等编排模式
 */
public class PluginOrchestrator {
    
    private final ExecutorService executorService;
    
    public PluginOrchestrator() {
        this(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }
    
    public PluginOrchestrator(ExecutorService executorService) {
        this.executorService = executorService;
    }
    
    /**
     * 顺序执行插件
     * 
     * @param plugins 插件列表
     * @param context 上下文
     * @param strategy 执行策略
     * @return 执行结果列表
     */
    public List<PluginResult> executeSequential(List<Plugin> plugins, PluginContext context, 
                                               SequentialStrategy strategy) {
        List<PluginResult> results = new ArrayList<>();
        
        for (Plugin plugin : plugins) {
            try {
                PluginResult result = plugin.execute(context);
                results.add(result);
                
                // 根据策略决定是否继续
                if (strategy == SequentialStrategy.STOP_ON_ERROR && !result.isSuccess()) {
                    break;
                }
                
                if (strategy == SequentialStrategy.STOP_ON_CONDITION && shouldStopExecution(result)) {
                    break;
                }
                
            } catch (Exception e) {
                PluginResult errorResult = PluginResult.builder()
                    .pluginId(plugin.getPluginId())
                    .success(false)
                    .error(e.getMessage())
                    .build();
                results.add(errorResult);
                
                if (strategy == SequentialStrategy.STOP_ON_ERROR) {
                    break;
                }
            }
        }
        
        return results;
    }
    
    /**
     * 并行执行插件
     * 
     * @param plugins 插件列表
     * @param context 上下文
     * @param strategy 执行策略
     * @return 执行结果列表
     */
    public List<PluginResult> executeParallel(List<Plugin> plugins, PluginContext context,
                                             ParallelStrategy strategy) {
        List<Future<PluginResult>> futures = new ArrayList<>();
        
        // 提交所有插件执行任务
        for (Plugin plugin : plugins) {
            Future<PluginResult> future = executorService.submit(() -> plugin.execute(context));
            futures.add(future);
        }
        
        List<PluginResult> results = new ArrayList<>();
        long timeout = strategy.getTimeoutSeconds();
        
        // 收集结果
        for (Future<PluginResult> future : futures) {
            try {
                PluginResult result;
                if (timeout > 0) {
                    result = future.get(timeout, TimeUnit.SECONDS);
                } else {
                    result = future.get();
                }
                results.add(result);
                
                // WAIT_ANY策略：只要有一个成功就可以提前结束
                if (strategy == ParallelStrategy.WAIT_ANY && result.isSuccess()) {
                    cancelRemaining(futures);
                    break;
                }
                
            } catch (TimeoutException e) {
                future.cancel(true);
                results.add(createTimeoutResult(e));
            } catch (Exception e) {
                results.add(createErrorResult(e));
            }
        }
        
        return results;
    }
    
    /**
     * 条件执行插件
     * 
     * @param plugin 插件
     * @param context 上下文
     * @param condition 条件
     * @return 执行结果
     */
    public PluginResult executeConditional(Plugin plugin, PluginContext context, 
                                          Predicate<PluginContext> condition) {
        if (condition.test(context)) {
            return plugin.execute(context);
        } else {
            return PluginResult.builder()
                .pluginId(plugin.getPluginId())
                .success(true)
                .skipped(true)
                .message("Condition not met, plugin skipped")
                .build();
        }
    }
    
    /**
     * 循环执行插件
     * 
     * @param plugin 插件
     * @param context 上下文
     * @param maxIterations 最大迭代次数
     * @param condition 继续条件
     * @return 执行结果列表
     */
    public List<PluginResult> executeLoop(Plugin plugin, PluginContext context,
                                         int maxIterations, Predicate<PluginResult> condition) {
        List<PluginResult> results = new ArrayList<>();
        
        for (int i = 0; i < maxIterations; i++) {
            PluginResult result = plugin.execute(context);
            results.add(result);
            
            // 检查是否继续
            if (!condition.test(result)) {
                break;
            }
        }
        
        return results;
    }
    
    /**
     * 分支执行插件
     * 
     * @param branches 分支映射（条件 -> 插件）
     * @param context 上下文
     * @param defaultPlugin 默认插件（当没有分支匹配时执行）
     * @return 执行结果
     */
    public PluginResult executeBranch(List<ConditionalBranch> branches, PluginContext context,
                                     Plugin defaultPlugin) {
        for (ConditionalBranch branch : branches) {
            if (branch.getCondition().test(context)) {
                return branch.getPlugin().execute(context);
            }
        }
        
        // 执行默认分支
        if (defaultPlugin != null) {
            return defaultPlugin.execute(context);
        }
        
        return PluginResult.builder()
            .success(true)
            .message("No branch matched, no default plugin")
            .build();
    }
    
    /**
     * 取消剩余任务
     */
    private void cancelRemaining(List<Future<PluginResult>> futures) {
        for (Future<PluginResult> future : futures) {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }
    }
    
    /**
     * 判断是否应该停止执行
     */
    private boolean shouldStopExecution(PluginResult result) {
        // 可以基于结果的特定属性判断
        return result.getData() != null && 
               Boolean.TRUE.equals(result.getData().get("stopExecution"));
    }
    
    /**
     * 创建超时结果
     */
    private PluginResult createTimeoutResult(TimeoutException e) {
        return PluginResult.builder()
            .success(false)
            .error("Plugin execution timeout: " + e.getMessage())
            .build();
    }
    
    /**
     * 创建错误结果
     */
    private PluginResult createErrorResult(Exception e) {
        return PluginResult.builder()
            .success(false)
            .error("Plugin execution error: " + e.getMessage())
            .build();
    }
    
    /**
     * 关闭编排器
     */
    public void shutdown() {
        executorService.shutdown();
    }
    
    /**
     * 顺序执行策略
     */
    public enum SequentialStrategy {
        /**
         * 执行所有插件（不管成功失败）
         */
        EXECUTE_ALL,
        
        /**
         * 遇到错误停止
         */
        STOP_ON_ERROR,
        
        /**
         * 满足条件时停止
         */
        STOP_ON_CONDITION
    }
    
    /**
     * 并行执行策略
     */
    public enum ParallelStrategy {
        /**
         * 等待所有插件完成
         */
        WAIT_ALL(0),
        
        /**
         * 等待任意一个成功
         */
        WAIT_ANY(0),
        
        /**
         * 带超时的等待所有
         */
        WAIT_ALL_WITH_TIMEOUT(30);
        
        private final long timeoutSeconds;
        
        ParallelStrategy(long timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }
        
        public long getTimeoutSeconds() {
            return timeoutSeconds;
        }
    }
    
    /**
     * 条件分支
     */
    public static class ConditionalBranch {
        private final Predicate<PluginContext> condition;
        private final Plugin plugin;
        
        public ConditionalBranch(Predicate<PluginContext> condition, Plugin plugin) {
            this.condition = condition;
            this.plugin = plugin;
        }
        
        public Predicate<PluginContext> getCondition() {
            return condition;
        }
        
        public Plugin getPlugin() {
            return plugin;
        }
    }
}
