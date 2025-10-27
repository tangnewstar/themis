package com.themis.core.protocol.impl;

import com.themis.core.model.RuleDefinition;
import com.themis.core.model.ValidationResult;
import com.themis.core.protocol.*;

/**
 * 自然语言格式的规则定义协议实现
 * 通过NLP技术将自然语言描述转换为规则定义
 * 需要与themis-ai模块的NaturalLanguageTranslator集成
 */
public class NlpRuleDefinitionProtocol implements RuleDefinitionProtocol {
    
    private Object nlpTranslator; // 实际类型应为NaturalLanguageTranslator，避免编译时依赖
    
    public NlpRuleDefinitionProtocol() {
        // 默认构造函数，延迟加载translator
    }
    
    /**
     * 设置NLP翻译器（通过反射或依赖注入）
     */
    public void setNlpTranslator(Object translator) {
        this.nlpTranslator = translator;
    }
    
    @Override
    public RuleDefinition parse(String source) throws RuleParseException {
        if (source == null || source.trim().isEmpty()) {
            throw new RuleParseException("Source cannot be null or empty");
        }
        
        if (nlpTranslator == null) {
            throw new RuleParseException(
                "NLP translator not configured. Please set translator using setNlpTranslator()");
        }
        
        try {
            // 通过反射调用NaturalLanguageTranslator.translate方法
            java.lang.reflect.Method translateMethod = 
                nlpTranslator.getClass().getMethod("translate", String.class);
            
            Object result = translateMethod.invoke(nlpTranslator, source);
            
            if (result instanceof RuleDefinition) {
                return (RuleDefinition) result;
            } else {
                throw new RuleParseException("NLP translator returned invalid type");
            }
            
        } catch (Exception e) {
            throw new RuleParseException("Failed to parse natural language rule: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ValidationResult validate(String source) {
        ValidationResult result = new ValidationResult();
        
        if (source == null || source.trim().isEmpty()) {
            result.addError("Source cannot be null or empty");
            return result;
        }
        
        if (nlpTranslator == null) {
            result.addError("NLP translator not configured");
            return result;
        }
        
        try {
            RuleDefinition rule = parse(source);
            return rule.validate();
        } catch (RuleParseException e) {
            result.addError("NLP parse error: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public String serialize(RuleDefinition rule) throws RuleSerializationException {
        // 规则到自然语言的转换比较复杂，暂不支持
        throw new RuleSerializationException(
            "Serialization to natural language is not supported. " +
            "Use JSON or DSL format instead.");
    }
    
    @Override
    public RuleFormatType getFormat() {
        return RuleFormatType.NLP;
    }
    
    @Override
    public String getProtocolName() {
        return "Natural Language Rule Definition Protocol";
    }
    
    @Override
    public String getProtocolVersion() {
        return "1.0.0";
    }
}
