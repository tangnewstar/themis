package com.themis.engine.expression;

import ognl.Ognl;
import ognl.OgnlException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OGNL表达式编译器实现
 * 基于OGNL库实现表达式编译和执行
 */
public class OgnlExpressionCompiler implements ExpressionCompiler {
    
    private final Map<String, CompiledExpression> cache = new ConcurrentHashMap<>();
    
    @Override
    public CompiledExpression compile(String expression) throws ExpressionCompileException {
        if (expression == null || expression.trim().isEmpty()) {
            throw new ExpressionCompileException(expression, "Expression cannot be null or empty");
        }
        
        try {
            Object compiledExpr = Ognl.parseExpression(expression);
            return new OgnlCompiledExpression(expression, compiledExpr);
        } catch (OgnlException e) {
            throw new ExpressionCompileException(expression, e.getMessage(), e);
        }
    }
    
    @Override
    public CompiledExpression compileAndCache(String expressionId, String expression) 
            throws ExpressionCompileException {
        CompiledExpression compiled = cache.get(expressionId);
        if (compiled == null) {
            compiled = compile(expression);
            cache.put(expressionId, compiled);
        }
        return compiled;
    }
    
    @Override
    public CompiledExpression getFromCache(String expressionId) {
        return cache.get(expressionId);
    }
    
    @Override
    public void clearCache(String expressionId) {
        cache.remove(expressionId);
    }
    
    @Override
    public void clearAllCache() {
        cache.clear();
    }
    
    @Override
    public ExpressionLanguage getLanguage() {
        return ExpressionLanguage.OGNL;
    }
    
    /**
     * OGNL编译后的表达式
     */
    private static class OgnlCompiledExpression implements CompiledExpression {
        
        private final String expression;
        private final Object compiledExpr;
        
        public OgnlCompiledExpression(String expression, Object compiledExpr) {
            this.expression = expression;
            this.compiledExpr = compiledExpr;
        }
        
        @Override
        public Object execute(Map<String, Object> context) {
            try {
                return Ognl.getValue(compiledExpr, context);
            } catch (OgnlException e) {
                throw new RuntimeException("Failed to execute OGNL expression: " + expression, e);
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public <T> T execute(Map<String, Object> context, Class<T> returnType) {
            Object result = execute(context);
            if (result == null) {
                return null;
            }
            if (returnType.isInstance(result)) {
                return (T) result;
            }
            // 类型转换
            return convertType(result, returnType);
        }
        
        @SuppressWarnings("unchecked")
        private <T> T convertType(Object value, Class<T> targetType) {
            if (targetType == String.class) {
                return (T) String.valueOf(value);
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return (T) Boolean.valueOf(String.valueOf(value));
            } else if (targetType == Integer.class || targetType == int.class) {
                return (T) Integer.valueOf(String.valueOf(value));
            } else if (targetType == Long.class || targetType == long.class) {
                return (T) Long.valueOf(String.valueOf(value));
            } else if (targetType == Double.class || targetType == double.class) {
                return (T) Double.valueOf(String.valueOf(value));
            }
            return targetType.cast(value);
        }
        
        @Override
        public String getExpression() {
            return expression;
        }
        
        @Override
        public ExpressionLanguage getLanguage() {
            return ExpressionLanguage.OGNL;
        }
    }
}
