package com.themis.engine.expression;

import java.util.Map;

/**
 * 表达式编译器接口
 * 支持表达式预编译以提升执行性能
 */
public interface ExpressionCompiler {
    
    /**
     * 编译表达式
     * 
     * @param expression 表达式字符串
     * @return 编译后的表达式对象
     * @throws ExpressionCompileException 编译失败时抛出
     */
    CompiledExpression compile(String expression) throws ExpressionCompileException;
    
    /**
     * 编译并缓存表达式
     * 
     * @param expressionId 表达式ID（用于缓存键）
     * @param expression 表达式字符串
     * @return 编译后的表达式对象
     * @throws ExpressionCompileException 编译失败时抛出
     */
    CompiledExpression compileAndCache(String expressionId, String expression) 
        throws ExpressionCompileException;
    
    /**
     * 从缓存获取已编译的表达式
     * 
     * @param expressionId 表达式ID
     * @return 编译后的表达式对象，如果未找到返回null
     */
    CompiledExpression getFromCache(String expressionId);
    
    /**
     * 清除表达式缓存
     * 
     * @param expressionId 表达式ID
     */
    void clearCache(String expressionId);
    
    /**
     * 清除所有表达式缓存
     */
    void clearAllCache();
    
    /**
     * 获取编译器支持的表达式语言类型
     * 
     * @return 表达式语言类型
     */
    ExpressionLanguage getLanguage();
    
    /**
     * 表达式语言类型
     */
    enum ExpressionLanguage {
        OGNL("ognl", "OGNL表达式"),
        SPEL("spel", "Spring EL"),
        AVIATOR("aviator", "Aviator表达式"),
        MVEL("mvel", "MVEL表达式"),
        GROOVY("groovy", "Groovy脚本"),
        JAVASCRIPT("javascript", "JavaScript");
        
        private final String code;
        private final String description;
        
        ExpressionLanguage(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 已编译的表达式
     */
    interface CompiledExpression {
        
        /**
         * 执行表达式
         * 
         * @param context 执行上下文（变量映射）
         * @return 执行结果
         */
        Object execute(Map<String, Object> context);
        
        /**
         * 执行表达式并返回指定类型的结果
         * 
         * @param context 执行上下文
         * @param returnType 返回值类型
         * @param <T> 类型参数
         * @return 指定类型的执行结果
         */
        <T> T execute(Map<String, Object> context, Class<T> returnType);
        
        /**
         * 获取原始表达式字符串
         * 
         * @return 表达式字符串
         */
        String getExpression();
        
        /**
         * 获取表达式语言类型
         * 
         * @return 语言类型
         */
        ExpressionLanguage getLanguage();
    }
}
