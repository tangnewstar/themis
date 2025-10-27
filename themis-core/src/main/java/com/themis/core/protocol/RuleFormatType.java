package com.themis.core.protocol;

/**
 * 规则格式类型枚举
 */
public enum RuleFormatType {
    
    /**
     * JSON格式 - 结构化JSON格式，适用于API调用、配置存储
     */
    JSON("json", "JSON格式", "application/json"),
    
    /**
     * YAML格式 - 可读性强的YAML，适用于配置文件、人工编辑
     */
    YAML("yaml", "YAML格式", "application/x-yaml"),
    
    /**
     * DSL格式 - 领域特定语言，适用于策略专家编写
     */
    DSL("dsl", "DSL格式", "text/plain"),
    
    /**
     * 可视化格式 - 可视化拖拽配置，适用于运营人员操作
     */
    VISUAL("visual", "可视化格式", "application/json"),
    
    /**
     * 自然语言格式 - NLP自然语言描述，适用于AI辅助生成
     */
    NLP("nlp", "自然语言格式", "text/plain"),
    
    /**
     * Excel格式 - 决策表格式，适用于业务人员配置
     */
    EXCEL("excel", "Excel格式", "application/vnd.ms-excel"),
    
    /**
     * Groovy脚本 - 动态脚本，适用于复杂逻辑实现
     */
    GROOVY("groovy", "Groovy脚本", "text/x-groovy");
    
    private final String code;
    private final String description;
    private final String mimeType;
    
    RuleFormatType(String code, String description, String mimeType) {
        this.code = code;
        this.description = description;
        this.mimeType = mimeType;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    /**
     * 根据代码获取格式类型
     */
    public static RuleFormatType fromCode(String code) {
        for (RuleFormatType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown format type: " + code);
    }
}
