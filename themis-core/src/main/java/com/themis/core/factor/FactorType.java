package com.themis.core.factor;

/**
 * 因子类型枚举
 * 按照设计文档分为：时间窗因子、个体因子、全局因子、关系因子
 */
public enum FactorType {
    
    // ========== 时间窗因子 ==========
    
    /**
     * 时间窗计数因子 - 统计时间窗内事件次数
     * 示例：用户5分钟内登录次数
     */
    TIME_WINDOW_COUNT("time_window_count", "时间窗计数", FactorCategory.TIME_WINDOW),
    
    /**
     * 时间窗求和因子 - 时间窗内数值累加
     * 示例：用户今日消费金额
     */
    TIME_WINDOW_SUM("time_window_sum", "时间窗求和", FactorCategory.TIME_WINDOW),
    
    /**
     * 时间窗均值因子 - 时间窗内平均值
     * 示例：用户近7日平均订单金额
     */
    TIME_WINDOW_AVG("time_window_avg", "时间窗均值", FactorCategory.TIME_WINDOW),
    
    /**
     * 时间窗最大值因子 - 时间窗内最大值
     * 示例：用户近30日最大单笔交易
     */
    TIME_WINDOW_MAX("time_window_max", "时间窗最大值", FactorCategory.TIME_WINDOW),
    
    /**
     * 时间窗最小值因子 - 时间窗内最小值
     */
    TIME_WINDOW_MIN("time_window_min", "时间窗最小值", FactorCategory.TIME_WINDOW),
    
    /**
     * 时间窗去重因子 - 时间窗内去重计数
     * 示例：用户近1小时使用设备数
     */
    TIME_WINDOW_DISTINCT("time_window_distinct", "时间窗去重", FactorCategory.TIME_WINDOW),
    
    /**
     * 时间窗趋势因子 - 时间窗内变化趋势
     * 示例：用户交易金额增长率
     */
    TIME_WINDOW_TREND("time_window_trend", "时间窗趋势", FactorCategory.TIME_WINDOW),
    
    // ========== 个体因子 ==========
    
    /**
     * 用户画像因子 - 用户的静态属性特征
     * 示例：年龄、性别、职业、会员等级
     */
    USER_PROFILE("user_profile", "用户画像", FactorCategory.INDIVIDUAL),
    
    /**
     * 设备画像因子 - 设备相关特征
     * 示例：设备型号、操作系统、设备信誉
     */
    DEVICE_PROFILE("device_profile", "设备画像", FactorCategory.INDIVIDUAL),
    
    /**
     * 商户画像因子 - 商户的属性特征
     * 示例：商户类型、经营年限、信用评分
     */
    MERCHANT_PROFILE("merchant_profile", "商户画像", FactorCategory.INDIVIDUAL),
    
    /**
     * 历史行为因子 - 个体的历史行为统计
     * 示例：历史订单数、平均客单价
     */
    HISTORICAL_BEHAVIOR("historical_behavior", "历史行为", FactorCategory.INDIVIDUAL),
    
    // ========== 全局因子 ==========
    
    /**
     * 平台统计因子 - 全平台维度的统计指标
     * 示例：平台日均订单量、活跃用户数
     */
    PLATFORM_STATS("platform_stats", "平台统计", FactorCategory.GLOBAL),
    
    /**
     * 业务指标因子 - 特定业务线的指标
     * 示例：某品类的销售趋势
     */
    BUSINESS_METRICS("business_metrics", "业务指标", FactorCategory.GLOBAL),
    
    /**
     * 环境因子 - 外部环境相关因子
     * 示例：节假日标识、天气状况
     */
    ENVIRONMENT("environment", "环境因子", FactorCategory.GLOBAL),
    
    // ========== 关系因子 ==========
    
    /**
     * 社交关系因子 - 用户间的关联关系
     * 示例：共同好友数、亲密度
     */
    SOCIAL_RELATION("social_relation", "社交关系", FactorCategory.RELATION),
    
    /**
     * 设备关联因子 - 设备间的关联关系
     * 示例：同IP设备数、设备聚集度
     */
    DEVICE_RELATION("device_relation", "设备关联", FactorCategory.RELATION),
    
    /**
     * 交易关联因子 - 交易主体间的关联
     * 示例：资金流转路径、交易闭环
     */
    TRANSACTION_RELATION("transaction_relation", "交易关联", FactorCategory.RELATION),
    
    // ========== 自定义因子 ==========
    
    /**
     * 自定义因子 - 用户自定义的计算逻辑
     */
    CUSTOM("custom", "自定义因子", FactorCategory.CUSTOM);
    
    private final String code;
    private final String description;
    private final FactorCategory category;
    
    FactorType(String code, String description, FactorCategory category) {
        this.code = code;
        this.description = description;
        this.category = category;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public FactorCategory getCategory() {
        return category;
    }
    
    /**
     * 根据代码获取因子类型
     */
    public static FactorType fromCode(String code) {
        for (FactorType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown factor type: " + code);
    }
    
    /**
     * 因子分类
     */
    public enum FactorCategory {
        TIME_WINDOW("时间窗因子"),
        INDIVIDUAL("个体因子"),
        GLOBAL("全局因子"),
        RELATION("关系因子"),
        CUSTOM("自定义因子");
        
        private final String description;
        
        FactorCategory(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
