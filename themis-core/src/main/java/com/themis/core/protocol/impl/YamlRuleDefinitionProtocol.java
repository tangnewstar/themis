package com.themis.core.protocol.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.themis.core.model.RuleDefinition;
import com.themis.core.model.ValidationResult;
import com.themis.core.protocol.*;

/**
 * YAML格式的规则定义协议实现
 */
public class YamlRuleDefinitionProtocol implements RuleDefinitionProtocol {
    
    private final ObjectMapper yamlMapper;
    
    public YamlRuleDefinitionProtocol() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.registerModule(new JavaTimeModule());
    }
    
    public YamlRuleDefinitionProtocol(ObjectMapper yamlMapper) {
        this.yamlMapper = yamlMapper;
    }
    
    @Override
    public RuleDefinition parse(String source) throws RuleParseException {
        if (source == null || source.trim().isEmpty()) {
            throw new RuleParseException("Source cannot be null or empty");
        }
        
        try {
            return yamlMapper.readValue(source, RuleDefinition.class);
        } catch (JsonProcessingException e) {
            throw new RuleParseException("Failed to parse YAML rule definition", e);
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
            result.addError("YAML parse error: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public String serialize(RuleDefinition rule) throws RuleSerializationException {
        if (rule == null) {
            throw new RuleSerializationException("Rule cannot be null");
        }
        
        try {
            return yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rule);
        } catch (JsonProcessingException e) {
            throw new RuleSerializationException("Failed to serialize rule to YAML", e);
        }
    }
    
    @Override
    public RuleFormatType getFormat() {
        return RuleFormatType.YAML;
    }
    
    @Override
    public String getProtocolName() {
        return "YAML Rule Definition Protocol";
    }
    
    @Override
    public String getProtocolVersion() {
        return "1.0.0";
    }
}
