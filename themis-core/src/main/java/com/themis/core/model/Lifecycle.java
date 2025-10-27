package com.themis.core.model;

/**
 * 规则生命周期状态
 */
public enum Lifecycle {
    
    /**
     * 草稿 - 规则正在编辑中
     */
    DRAFT("draft", "草稿"),
    
    /**
     * 待审核 - 规则已提交等待审核
     */
    PENDING_REVIEW("pending_review", "待审核"),
    
    /**
     * 测试中 - 规则在测试环境验证中
     */
    TESTING("testing", "测试中"),
    
    /**
     * 已发布 - 规则已发布到生产环境
     */
    PUBLISHED("published", "已发布"),
    
    /**
     * 已激活 - 规则正在生产环境运行
     */
    ACTIVE("active", "已激活"),
    
    /**
     * 已暂停 - 规则暂时停止执行
     */
    PAUSED("paused", "已暂停"),
    
    /**
     * 已归档 - 规则已下线并归档
     */
    ARCHIVED("archived", "已归档"),
    
    /**
     * 已废弃 - 规则已废弃不再使用
     */
    DEPRECATED("deprecated", "已废弃");
    
    private final String code;
    private final String description;
    
    Lifecycle(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取生命周期状态
     */
    public static Lifecycle fromCode(String code) {
        for (Lifecycle lifecycle : values()) {
            if (lifecycle.code.equalsIgnoreCase(code)) {
                return lifecycle;
            }
        }
        throw new IllegalArgumentException("Unknown lifecycle code: " + code);
    }
    
    /**
     * 从RuleStatus转换为Lifecycle
     */
    public static Lifecycle fromRuleStatus(RuleStatus status) {
        switch (status) {
            case DRAFT:
                return DRAFT;
            case ACTIVE:
                return ACTIVE;
            case INACTIVE:
                return PAUSED;
            case ARCHIVED:
                return ARCHIVED;
            default:
                return DRAFT;
        }
    }
    
    /**
     * 转换为RuleStatus
     */
    public RuleStatus toRuleStatus() {
        switch (this) {
            case DRAFT:
            case PENDING_REVIEW:
            case TESTING:
                return RuleStatus.DRAFT;
            case PUBLISHED:
            case ACTIVE:
                return RuleStatus.ACTIVE;
            case PAUSED:
            case DEPRECATED:
                return RuleStatus.INACTIVE;
            case ARCHIVED:
                return RuleStatus.ARCHIVED;
            default:
                return RuleStatus.DRAFT;
        }
    }
    
    /**
     * 检查是否可以执行
     */
    public boolean isExecutable() {
        return this == ACTIVE || this == PUBLISHED;
    }
    
    /**
     * 检查是否可以编辑
     */
    public boolean isEditable() {
        return this == DRAFT || this == PENDING_REVIEW || this == TESTING;
    }
}
