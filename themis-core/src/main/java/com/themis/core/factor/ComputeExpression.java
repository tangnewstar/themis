package com.themis.core.factor;

import java.util.Map;

/**
 * 计算表达式
 * 定义因子的计算逻辑
 */
public class ComputeExpression {
    
    /**
     * 表达式类型
     */
    private ExpressionType expressionType;
    
    /**
     * 表达式内容
     */
    private String expression;
    
    /**
     * 表达式参数
     */
    private Map<String, Object> parameters;
    
    /**
     * 脚本语言（仅用于脚本类型）
     */
    private String scriptLanguage;
    
    /**
     * 函数名称（仅用于函数调用类型）
     */
    private String functionName;
    
    public ComputeExpression() {
    }
    
    public ComputeExpression(ExpressionType expressionType, String expression) {
        this.expressionType = expressionType;
        this.expression = expression;
    }
    
    /**
     * 创建SQL表达式
     */
    public static ComputeExpression sql(String sqlExpression) {
        return new ComputeExpression(ExpressionType.SQL, sqlExpression);
    }
    
    /**
     * 创建OGNL表达式
     */
    public static ComputeExpression ognl(String ognlExpression) {
        return new ComputeExpression(ExpressionType.OGNL, ognlExpression);
    }
    
    /**
     * 创建SpEL表达式
     */
    public static ComputeExpression spel(String spelExpression) {
        return new ComputeExpression(ExpressionType.SPEL, spelExpression);
    }
    
    /**
     * 创建Groovy脚本
     */
    public static ComputeExpression groovy(String groovyScript) {
        ComputeExpression expr = new ComputeExpression(ExpressionType.SCRIPT, groovyScript);
        expr.setScriptLanguage("groovy");
        return expr;
    }
    
    /**
     * 创建函数调用
     */
    public static ComputeExpression function(String functionName) {
        ComputeExpression expr = new ComputeExpression(ExpressionType.FUNCTION, null);
        expr.setFunctionName(functionName);
        return expr;
    }
    
    // Getters and Setters
    
    public ExpressionType getExpressionType() {
        return expressionType;
    }
    
    public void setExpressionType(ExpressionType expressionType) {
        this.expressionType = expressionType;
    }
    
    public String getExpression() {
        return expression;
    }
    
    public void setExpression(String expression) {
        this.expression = expression;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public String getScriptLanguage() {
        return scriptLanguage;
    }
    
    public void setScriptLanguage(String scriptLanguage) {
        this.scriptLanguage = scriptLanguage;
    }
    
    public String getFunctionName() {
        return functionName;
    }
    
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
    
    /**
     * 表达式类型
     */
    public enum ExpressionType {
        /**
         * SQL表达式
         */
        SQL("sql", "SQL表达式"),
        
        /**
         * OGNL表达式
         */
        OGNL("ognl", "OGNL表达式"),
        
        /**
         * SpEL表达式
         */
        SPEL("spel", "SpEL表达式"),
        
        /**
         * Aviator表达式
         */
        AVIATOR("aviator", "Aviator表达式"),
        
        /**
         * MVEL表达式
         */
        MVEL("mvel", "MVEL表达式"),
        
        /**
         * 脚本（Groovy/JavaScript/Python）
         */
        SCRIPT("script", "脚本"),
        
        /**
         * 函数调用
         */
        FUNCTION("function", "函数调用"),
        
        /**
         * Java代码
         */
        JAVA("java", "Java代码");
        
        private final String code;
        private final String description;
        
        ExpressionType(String code, String description) {
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
    
    @Override
    public String toString() {
        return String.format("ComputeExpression{type=%s, expr='%s'}", 
                           expressionType, 
                           expression != null && expression.length() > 50 ? 
                               expression.substring(0, 50) + "..." : expression);
    }
}
