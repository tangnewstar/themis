package com.themis.core.factor;

import java.util.concurrent.TimeUnit;

/**
 * 时间窗配置
 * 定义时间窗的类型、大小和滑动策略
 */
public class TimeWindowConfig {
    
    /**
     * 窗口类型
     */
    private WindowType windowType;
    
    /**
     * 窗口大小
     */
    private long windowSize;
    
    /**
     * 时间单位
     */
    private TimeUnit timeUnit;
    
    /**
     * 滑动步长（仅用于滑动窗口）
     */
    private long slideStep;
    
    /**
     * 会话超时时间（仅用于会话窗口）
     */
    private long sessionTimeout;
    
    public TimeWindowConfig() {
    }
    
    public TimeWindowConfig(WindowType windowType, long windowSize, TimeUnit timeUnit) {
        this.windowType = windowType;
        this.windowSize = windowSize;
        this.timeUnit = timeUnit;
    }
    
    /**
     * 创建滑动窗口配置
     */
    public static TimeWindowConfig sliding(long size, TimeUnit unit) {
        return new TimeWindowConfig(WindowType.SLIDING, size, unit);
    }
    
    /**
     * 创建滚动窗口配置
     */
    public static TimeWindowConfig tumbling(long size, TimeUnit unit) {
        return new TimeWindowConfig(WindowType.TUMBLING, size, unit);
    }
    
    /**
     * 创建会话窗口配置
     */
    public static TimeWindowConfig session(long timeout, TimeUnit unit) {
        TimeWindowConfig config = new TimeWindowConfig();
        config.setWindowType(WindowType.SESSION);
        config.setSessionTimeout(timeout);
        config.setTimeUnit(unit);
        return config;
    }
    
    // Getters and Setters
    
    public WindowType getWindowType() {
        return windowType;
    }
    
    public void setWindowType(WindowType windowType) {
        this.windowType = windowType;
    }
    
    public long getWindowSize() {
        return windowSize;
    }
    
    public void setWindowSize(long windowSize) {
        this.windowSize = windowSize;
    }
    
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
    
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
    
    public long getSlideStep() {
        return slideStep;
    }
    
    public void setSlideStep(long slideStep) {
        this.slideStep = slideStep;
    }
    
    public long getSessionTimeout() {
        return sessionTimeout;
    }
    
    public void setSessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
    
    /**
     * 获取窗口大小（毫秒）
     */
    public long getWindowSizeInMillis() {
        return timeUnit.toMillis(windowSize);
    }
    
    /**
     * 获取会话超时时间（毫秒）
     */
    public long getSessionTimeoutInMillis() {
        return timeUnit.toMillis(sessionTimeout);
    }
    
    /**
     * 窗口类型枚举
     */
    public enum WindowType {
        /**
         * 滑动窗口 - 从当前时间向前推的固定时长
         * 示例：最近5分钟、最近1小时
         */
        SLIDING("sliding", "滑动窗口"),
        
        /**
         * 滚动窗口 - 固定时间边界的窗口
         * 示例：今日、本周、本月
         */
        TUMBLING("tumbling", "滚动窗口"),
        
        /**
         * 会话窗口 - 基于事件间隔的动态窗口
         * 示例：用户活跃会话期
         */
        SESSION("session", "会话窗口");
        
        private final String code;
        private final String description;
        
        WindowType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Override
    public String toString() {
        return String.format("TimeWindowConfig{type=%s, size=%d %s}", 
                           windowType, windowSize, timeUnit);
    }
}
