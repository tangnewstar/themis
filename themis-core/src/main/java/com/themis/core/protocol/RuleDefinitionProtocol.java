package com.themis.core.protocol;

import com.themis.core.model.RuleDefinition;
import com.themis.core.model.ValidationResult;

/**
 * Rule Definition Protocol (RDP) - 规则定义协议接口
 * 将规则定义抽象为统一的协议接口，支持多种描述语言，实现规则定义与存储格式解耦
 */
public interface RuleDefinitionProtocol {
    
    /**
     * 将外部格式解析为内部规则模型
     * 
     * @param source 外部格式的规则定义（JSON、YAML、DSL等）
     * @return 内部规则模型
     * @throws RuleParseException 解析失败时抛出
     */
    RuleDefinition parse(String source) throws RuleParseException;
    
    /**
     * 验证规则定义的合法性
     * 
     * @param source 外部格式的规则定义
     * @return 验证结果，包含错误和警告信息
     */
    ValidationResult validate(String source);
    
    /**
     * 将规则模型序列化为外部格式
     * 
     * @param rule 内部规则模型
     * @return 序列化后的字符串
     * @throws RuleSerializationException 序列化失败时抛出
     */
    String serialize(RuleDefinition rule) throws RuleSerializationException;
    
    /**
     * 获取支持的格式类型
     * 
     * @return 格式类型枚举
     */
    RuleFormatType getFormat();
    
    /**
     * 获取协议名称
     * 
     * @return 协议名称
     */
    String getProtocolName();
    
    /**
     * 获取协议版本
     * 
     * @return 版本号
     */
    String getProtocolVersion();
}
