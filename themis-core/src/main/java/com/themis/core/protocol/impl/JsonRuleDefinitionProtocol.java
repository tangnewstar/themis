package com.themis.core.protocol.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.themis.core.model.RuleDefinition;
import com.themis.core.model.ValidationResult;
import com.themis.core.protocol.*;

/**
 * JSON格式的规则定义协议实现
 */
public class JsonRuleDefinitionProtocol implements RuleDefinitionProtocol {
    
    private final ObjectMapper objectMapper;
    
    public JsonRuleDefinitionProtocol() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    public JsonRuleDefinitionProtocol(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public RuleDefinition parse(String source) throws RuleParseException {
        if (source == null || source.trim().isEmpty()) {
            throw new RuleParseException("Source cannot be null or empty");
        }
        
        try {
            return objectMapper.readValue(source, RuleDefinition.class);
        } catch (JsonProcessingException e) {
            throw new RuleParseException("Failed to parse JSON rule definition", e);
        }
    }
    
    @Override
    public ValidationResult validate(String source) {
        ValidationResult result = new ValidationResult();
        
        if (source == null || source.trim().isEmpty()) {
            result.addError("Source cannot be null or empty");
            return result;
        }
        
        try {
            RuleDefinition rule = parse(source);
            return rule.validate();
        } catch (RuleParseException e) {
            result.addError("JSON parse error: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public String serialize(RuleDefinition rule) throws RuleSerializationException {
        if (rule == null) {
            throw new RuleSerializationException("Rule cannot be null");
        }
        
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rule);
        } catch (JsonProcessingException e) {
            throw new RuleSerializationException("Failed to serialize rule to JSON", e);
        }
    }
    
    @Override
    public RuleFormatType getFormat() {
        return RuleFormatType.JSON;
    }
    
    @Override
    public String getProtocolName() {
        return "JSON Rule Definition Protocol";
    }
    
    @Override
    public String getProtocolVersion() {
        return "1.0.0";
    }
}
