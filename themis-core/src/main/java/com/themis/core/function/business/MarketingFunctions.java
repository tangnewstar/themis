package com.themis.core.function.business;

import com.themis.core.function.CustomFunction;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * 营销业务函数库
 * 提供用户分群、优惠计算、活动规则等营销相关函数
 */
public class MarketingFunctions {
    
    /**
     * 计算优惠金额
     * 
     * @param originalAmount 原始金额
     * @param discountRate 折扣率 (0.0-1.0)
     * @param maxDiscount 最大优惠金额
     * @return 优惠后金额
     */
    @CustomFunction(
        name = "calculateDiscount",
        description = "计算优惠后金额",
        category = "marketing"
    )
    public static double calculateDiscount(double originalAmount, double discountRate, double maxDiscount) {
        double discountAmount = originalAmount * (1 - discountRate);
        double actualDiscount = originalAmount - discountAmount;
        
        if (actualDiscount > maxDiscount) {
            return originalAmount - maxDiscount;
        }
        
        return discountAmount;
    }
    
    /**
     * 判断用户是否属于目标人群
     * 
     * @param userId 用户ID
     * @param userTags 用户标签
     * @param targetTags 目标标签
     * @return 是否匹配
     */
    @CustomFunction(
        name = "isTargetAudience",
        description = "判断用户是否属于目标人群",
        category = "marketing"
    )
    public static boolean isTargetAudience(String userId, List<String> userTags, List<String> targetTags) {
        if (userTags == null || targetTags == null) {
            return false;
        }
        
        // 检查是否有交集
        for (String targetTag : targetTags) {
            if (userTags.contains(targetTag)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 计算用户RFM评分
     * R (Recency): 最近一次购买时间
     * F (Frequency): 购买频次
     * M (Monetary): 购买金额
     * 
     * @param daysSinceLastPurchase 距离最后一次购买的天数
     * @param purchaseFrequency 购买频次
     * @param totalAmount 总购买金额
     * @return RFM评分 (3位数字，每位1-5)
     */
    @CustomFunction(
        name = "calculateRFMScore",
        description = "计算用户RFM评分",
        category = "marketing"
    )
    public static String calculateRFMScore(int daysSinceLastPurchase, int purchaseFrequency, double totalAmount) {
        // R评分：最近购买越近分数越高
        int rScore = 5;
        if (daysSinceLastPurchase > 180) rScore = 1;
        else if (daysSinceLastPurchase > 90) rScore = 2;
        else if (daysSinceLastPurchase > 30) rScore = 3;
        else if (daysSinceLastPurchase > 7) rScore = 4;
        
        // F评分：购买频次越高分数越高
        int fScore = 1;
        if (purchaseFrequency > 50) fScore = 5;
        else if (purchaseFrequency > 20) fScore = 4;
        else if (purchaseFrequency > 10) fScore = 3;
        else if (purchaseFrequency > 5) fScore = 2;
        
        // M评分：购买金额越高分数越高
        int mScore = 1;
        if (totalAmount > 100000) mScore = 5;
        else if (totalAmount > 50000) mScore = 4;
        else if (totalAmount > 10000) mScore = 3;
        else if (totalAmount > 1000) mScore = 2;
        
        return String.format("%d%d%d", rScore, fScore, mScore);
    }
    
    /**
     * 计算用户生命周期价值 (CLV)
     * 
     * @param avgOrderValue 平均订单价值
     * @param purchaseFrequency 年购买频次
     * @param customerLifespan 客户生命周期（年）
     * @param profitMargin 利润率
     * @return 客户生命周期价值
     */
    @CustomFunction(
        name = "calculateCLV",
        description = "计算用户生命周期价值",
        category = "marketing"
    )
    public static double calculateCLV(double avgOrderValue, double purchaseFrequency, 
                                     double customerLifespan, double profitMargin) {
        return avgOrderValue * purchaseFrequency * customerLifespan * profitMargin;
    }
    
    /**
     * 检查优惠券是否可用
     * 
     * @param couponCode 优惠券代码
     * @param orderAmount 订单金额
     * @param minAmount 最低消费金额
     * @param expiryDate 过期时间
     * @return 是否可用
     */
    @CustomFunction(
        name = "isCouponValid",
        description = "检查优惠券是否可用",
        category = "marketing"
    )
    public static boolean isCouponValid(String couponCode, double orderAmount, 
                                       double minAmount, LocalDateTime expiryDate) {
        // 检查金额门槛
        if (orderAmount < minAmount) {
            return false;
        }
        
        // 检查是否过期
        if (expiryDate != null && LocalDateTime.now().isAfter(expiryDate)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 计算阶梯折扣
     * 
     * @param amount 金额
     * @param tiers 折扣阶梯配置 (金额门槛 -> 折扣率)
     * @return 折扣率
     */
    @CustomFunction(
        name = "calculateTierDiscount",
        description = "计算阶梯折扣率",
        category = "marketing"
    )
    public static double calculateTierDiscount(double amount, Map<Double, Double> tiers) {
        double maxTier = 0.0;
        double discount = 0.0;
        
        for (Map.Entry<Double, Double> entry : tiers.entrySet()) {
            if (amount >= entry.getKey() && entry.getKey() > maxTier) {
                maxTier = entry.getKey();
                discount = entry.getValue();
            }
        }
        
        return discount;
    }
    
    /**
     * 计算积分奖励
     * 
     * @param orderAmount 订单金额
     * @param pointsRate 积分比例
     * @param bonusMultiplier 奖励倍数
     * @return 获得积分
     */
    @CustomFunction(
        name = "calculatePoints",
        description = "计算积分奖励",
        category = "marketing"
    )
    public static int calculatePoints(double orderAmount, double pointsRate, double bonusMultiplier) {
        return (int) (orderAmount * pointsRate * bonusMultiplier);
    }
    
    /**
     * 判断是否为新用户
     * 
     * @param registerDate 注册日期
     * @param newUserDays 新用户定义天数
     * @return 是否为新用户
     */
    @CustomFunction(
        name = "isNewUser",
        description = "判断是否为新用户",
        category = "marketing"
    )
    public static boolean isNewUser(LocalDateTime registerDate, int newUserDays) {
        if (registerDate == null) {
            return false;
        }
        
        long daysBetween = ChronoUnit.DAYS.between(registerDate, LocalDateTime.now());
        return daysBetween <= newUserDays;
    }
}
