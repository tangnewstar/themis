package com.themis.core.function.business;

import com.themis.core.function.CustomFunction;

import java.util.Map;

/**
 * 风控业务函数库
 * 提供风险评估、黑名单检查等风控相关函数
 */
public class RiskControlFunctions {
    
    /**
     * 计算风险评分
     * 
     * @param userId 用户ID
     * @param amount 金额
     * @param context 上下文信息
     * @return 风险评分 (0-100)
     */
    @CustomFunction(
        name = "calculateRiskScore",
        description = "计算用户风险评分",
        category = "risk_control"
    )
    public static double calculateRiskScore(String userId, double amount, Map<String, Object> context) {
        double baseScore = 50.0;
        
        // 金额因素
        if (amount > 10000) {
            baseScore += 20;
        } else if (amount > 5000) {
            baseScore += 10;
        }
        
        // 用户历史行为因素（从上下文获取）
        Integer historyViolationCount = (Integer) context.getOrDefault("historyViolationCount", 0);
        baseScore += historyViolationCount * 5;
        
        // 时间因素（深夜交易风险更高）
        Integer hour = (Integer) context.getOrDefault("transactionHour", 12);
        if (hour >= 0 && hour < 6) {
            baseScore += 15;
        }
        
        return Math.min(100.0, baseScore);
    }
    
    /**
     * 检查是否在黑名单
     * 
     * @param userId 用户ID
     * @param blacklistType 黑名单类型
     * @return 是否在黑名单
     */
    @CustomFunction(
        name = "isInBlacklist",
        description = "检查用户是否在黑名单",
        category = "risk_control"
    )
    public static boolean isInBlacklist(String userId, String blacklistType) {
        // TODO: 实际实现需要查询黑名单数据库
        // 这里是框架示例
        return false;
    }
    
    /**
     * 检查设备风险
     * 
     * @param deviceId 设备ID
     * @param deviceFingerprint 设备指纹
     * @return 设备风险等级 (LOW/MEDIUM/HIGH)
     */
    @CustomFunction(
        name = "checkDeviceRisk",
        description = "检查设备风险等级",
        category = "risk_control"
    )
    public static String checkDeviceRisk(String deviceId, String deviceFingerprint) {
        // 框架实现：实际需要对接设备指纹服务
        if (deviceFingerprint == null || deviceFingerprint.isEmpty()) {
            return "HIGH";
        }
        
        return "LOW";
    }
    
    /**
     * 计算交易速度异常分数
     * 
     * @param userId 用户ID
     * @param currentTimestamp 当前时间戳
     * @param lastTransactionTimestamp 上次交易时间戳
     * @return 异常分数 (0-100)
     */
    @CustomFunction(
        name = "calculateVelocityScore",
        description = "计算交易速度异常分数",
        category = "risk_control"
    )
    public static double calculateVelocityScore(String userId, long currentTimestamp, 
                                               long lastTransactionTimestamp) {
        if (lastTransactionTimestamp <= 0) {
            return 0.0;
        }
        
        long timeDiff = currentTimestamp - lastTransactionTimestamp;
        
        // 时间间隔太短（可能是机器行为）
        if (timeDiff < 1000) { // 1秒内
            return 90.0;
        } else if (timeDiff < 5000) { // 5秒内
            return 60.0;
        } else if (timeDiff < 10000) { // 10秒内
            return 30.0;
        }
        
        return 0.0;
    }
    
    /**
     * 检查IP地址风险
     * 
     * @param ipAddress IP地址
     * @return 风险等级
     */
    @CustomFunction(
        name = "checkIpRisk",
        description = "检查IP地址风险等级",
        category = "risk_control"
    )
    public static String checkIpRisk(String ipAddress) {
        // 框架实现：实际需要对接IP情报库
        if (ipAddress == null || ipAddress.isEmpty()) {
            return "UNKNOWN";
        }
        
        // 检查是否为内网IP
        if (ipAddress.startsWith("192.168.") || 
            ipAddress.startsWith("10.") ||
            ipAddress.startsWith("172.")) {
            return "LOW";
        }
        
        return "MEDIUM";
    }
    
    /**
     * 计算用户信用分
     * 
     * @param userId 用户ID
     * @param accountAge 账户年龄（天）
     * @param verificationLevel 认证级别 (0-3)
     * @param historicalBehavior 历史行为评分
     * @return 信用分 (0-1000)
     */
    @CustomFunction(
        name = "calculateCreditScore",
        description = "计算用户信用分",
        category = "risk_control"
    )
    public static int calculateCreditScore(String userId, int accountAge, 
                                          int verificationLevel, double historicalBehavior) {
        int score = 500; // 基础分
        
        // 账户年龄加分
        score += Math.min(accountAge / 30 * 10, 200); // 每月10分，最多200分
        
        // 认证级别加分
        score += verificationLevel * 50; // 每级50分
        
        // 历史行为加分
        score += (int) (historicalBehavior * 100);
        
        return Math.min(1000, Math.max(0, score));
    }
}
