package com.themis.core.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 规则协议注册中心
 * 管理和查找不同格式的规则定义协议
 */
public class RuleProtocolRegistry {
    
    private static final RuleProtocolRegistry INSTANCE = new RuleProtocolRegistry();
    
    private final Map<RuleFormatType, RuleDefinitionProtocol> protocols = new ConcurrentHashMap<>();
    
    private RuleProtocolRegistry() {
        // 私有构造函数，单例模式
    }
    
    public static RuleProtocolRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册规则协议
     * 
     * @param protocol 规则定义协议实现
     */
    public void register(RuleDefinitionProtocol protocol) {
        if (protocol == null) {
            throw new IllegalArgumentException("Protocol cannot be null");
        }
        protocols.put(protocol.getFormat(), protocol);
    }
    
    /**
     * 根据格式类型获取协议
     * 
     * @param formatType 格式类型
     * @return 规则定义协议，如果未找到则返回null
     */
    public RuleDefinitionProtocol getProtocol(RuleFormatType formatType) {
        return protocols.get(formatType);
    }
    
    /**
     * 根据格式代码获取协议
     * 
     * @param formatCode 格式代码（json, yaml, dsl等）
     * @return 规则定义协议，如果未找到则返回null
     */
    public RuleDefinitionProtocol getProtocol(String formatCode) {
        RuleFormatType formatType = RuleFormatType.fromCode(formatCode);
        return getProtocol(formatType);
    }
    
    /**
     * 取消注册协议
     * 
     * @param formatType 格式类型
     */
    public void unregister(RuleFormatType formatType) {
        protocols.remove(formatType);
    }
    
    /**
     * 检查是否支持指定格式
     * 
     * @param formatType 格式类型
     * @return 如果支持返回true，否则返回false
     */
    public boolean supports(RuleFormatType formatType) {
        return protocols.containsKey(formatType);
    }
    
    /**
     * 清除所有注册的协议
     */
    public void clear() {
        protocols.clear();
    }
    
    /**
     * 获取已注册的协议数量
     * 
     * @return 协议数量
     */
    public int size() {
        return protocols.size();
    }
}
