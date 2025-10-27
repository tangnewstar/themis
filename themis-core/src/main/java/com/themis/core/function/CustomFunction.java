package com.themis.core.function;

import java.util.List;

/**
 * 自定义函数接口
 */
public interface CustomFunction {
    
    /**
     * 获取函数名称
     * 
     * @return 函数名
     */
    String getName();
    
    /**
     * 获取命名空间
     * 
     * @return 命名空间,默认为null
     */
    String getNamespace();
    
    /**
     * 获取参数类型列表
     * 
     * @return 参数类型列表
     */
    List<Class<?>> getParameterTypes();
    
    /**
     * 获取返回值类型
     * 
     * @return 返回值类型
     */
    Class<?> getReturnType();
    
    /**
     * 执行函数逻辑
     * 
     * @param parameters 参数列表
     * @return 执行结果
     * @throws FunctionExecutionException 执行失败时抛出
     */
    Object execute(Object... parameters) throws FunctionExecutionException;
    
    /**
     * 验证参数
     * 
     * @param parameters 参数列表
     * @return 验证结果,true表示验证通过
     */
    boolean validate(Object... parameters);
    
    /**
     * 获取函数描述
     * 
     * @return 函数描述
     */
    String getDescription();
    
    /**
     * 获取函数示例
     * 
     * @return 使用示例
     */
    String getExample();
    
    /**
     * 获取函数版本
     * 
     * @return 版本号
     */
    default String getVersion() {
        return "1.0.0";
    }
    
    /**
     * 是否为纯函数(无副作用)
     * 
     * @return 是纯函数返回true
     */
    default boolean isPure() {
        return true;
    }
}
