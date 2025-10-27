package com.themis.core.function.optimization;

import com.themis.core.function.FunctionExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 函数编排管道
 * 支持链式调用和组合多个函数
 */
public class FunctionPipeline<T, R> {
    
    /**
     * 函数链
     */
    private final List<Function<?, ?>> functions = new ArrayList<>();
    
    /**
     * 添加函数到管道
     * 
     * @param function 函数
     * @param <U> 输出类型
     * @return 新的管道
     */
    @SuppressWarnings("unchecked")
    public <U> FunctionPipeline<T, U> then(Function<? super R, U> function) {
        FunctionPipeline<T, U> newPipeline = new FunctionPipeline<>();
        newPipeline.functions.addAll(this.functions);
        newPipeline.functions.add(function);
        return newPipeline;
    }
    
    /**
     * 执行管道
     * 
     * @param input 输入值
     * @return 输出值
     */
    @SuppressWarnings("unchecked")
    public R execute(T input) {
        Object result = input;
        
        for (Function<?, ?> function : functions) {
            Function<Object, Object> f = (Function<Object, Object>) function;
            result = f.apply(result);
        }
        
        return (R) result;
    }
    
    /**
     * 创建新的管道
     * 
     * @param function 初始函数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 管道
     */
    public static <T, R> FunctionPipeline<T, R> create(Function<T, R> function) {
        FunctionPipeline<T, R> pipeline = new FunctionPipeline<>();
        pipeline.functions.add(function);
        return pipeline;
    }
    
    /**
     * 组合多个函数
     * 
     * @param functions 函数列表
     * @param <T> 输入类型
     * @return 组合后的函数
     */
    @SafeVarargs
    public static <T> Function<T, T> compose(Function<T, T>... functions) {
        return input -> {
            T result = input;
            for (Function<T, T> function : functions) {
                result = function.apply(result);
            }
            return result;
        };
    }
    
    /**
     * 条件函数
     * 
     * @param condition 条件
     * @param trueFunction 条件为真时的函数
     * @param falseFunction 条件为假时的函数
     * @param <T> 类型
     * @return 条件函数
     */
    public static <T> Function<T, T> conditional(
            java.util.function.Predicate<T> condition,
            Function<T, T> trueFunction,
            Function<T, T> falseFunction) {
        
        return input -> condition.test(input) ? 
            trueFunction.apply(input) : 
            falseFunction.apply(input);
    }
    
    /**
     * 重试函数
     * 
     * @param function 原函数
     * @param maxRetries 最大重试次数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 带重试的函数
     */
    public static <T, R> Function<T, R> retry(Function<T, R> function, int maxRetries) {
        return input -> {
            Exception lastException = null;
            
            for (int i = 0; i <= maxRetries; i++) {
                try {
                    return function.apply(input);
                } catch (Exception e) {
                    lastException = e;
                    if (i < maxRetries) {
                        // 可以添加延迟策略
                        try {
                            Thread.sleep(100 * (i + 1));
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            
            throw new RuntimeException("Function failed after " + maxRetries + " retries", lastException);
        };
    }
    
    /**
     * 缓存函数
     * 
     * @param function 原函数
     * @param cache 缓存
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 带缓存的函数
     */
    public static <T, R> Function<T, R> cached(Function<T, R> function, FunctionResultCache cache) {
        return input -> {
            String cacheKey = FunctionResultCache.generateCacheKey("function", input);
            
            @SuppressWarnings("unchecked")
            R cachedResult = (R) cache.get(cacheKey);
            if (cachedResult != null) {
                return cachedResult;
            }
            
            R result = function.apply(input);
            cache.put(cacheKey, result);
            return result;
        };
    }
}
