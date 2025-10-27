package com.themis.core.plugin;

import java.util.Map;

/**
 * Themis插件基础接口
 * 所有插件必须实现此接口
 */
public interface ThemisPlugin {
    
    /**
     * 获取插件唯一标识
     * 
     * @return 插件ID
     */
    String getPluginId();
    
    /**
     * 获取插件名称
     * 
     * @return 插件名称
     */
    String getPluginName();
    
    /**
     * 获取插件版本
     * 
     * @return 版本号
     */
    String getVersion();
    
    /**
     * 获取插件类型
     * 
     * @return 插件类型
     */
    PluginType getPluginType();
    
    /**
     * 插件初始化
     * 
     * @param config 初始化配置
     * @throws PluginException 初始化失败时抛出
     */
    void init(Map<String, Object> config) throws PluginException;
    
    /**
     * 插件启动
     * 
     * @throws PluginException 启动失败时抛出
     */
    void start() throws PluginException;
    
    /**
     * 插件停止
     * 
     * @throws PluginException 停止失败时抛出
     */
    void stop() throws PluginException;
    
    /**
     * 插件销毁
     */
    void destroy();
    
    /**
     * 获取插件配置
     * 
     * @return 插件配置
     */
    Map<String, Object> getConfig();
    
    /**
     * 获取插件状态
     * 
     * @return 插件状态
     */
    PluginStatus getStatus();
    
    /**
     * 插件健康检查
     * 
     * @return 健康状态
     */
    HealthStatus healthCheck();
    
    /**
     * 插件类型枚举
     */
    enum PluginType {
        DATA_SOURCE("data_source", "数据源插件"),
        COMPUTE("compute", "计算插件"),
        ACTION("action", "动作插件"),
        FUNCTION("function", "函数插件"),
        NOTIFICATION("notification", "通知插件"),
        STORAGE("storage", "存储插件"),
        CUSTOM("custom", "自定义插件");
        
        private final String code;
        private final String description;
        
        PluginType(String code, String description) {
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
    
    /**
     * 插件状态枚举
     */
    enum PluginStatus {
        CREATED("created", "已创建"),
        INITIALIZED("initialized", "已初始化"),
        STARTED("started", "已启动"),
        STOPPED("stopped", "已停止"),
        ERROR("error", "错误"),
        DESTROYED("destroyed", "已销毁");
        
        private final String code;
        private final String description;
        
        PluginStatus(String code, String description) {
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
    
    /**
     * 健康状态
     */
    class HealthStatus {
        private final boolean healthy;
        private final String message;
        private final long checkTime;
        
        public HealthStatus(boolean healthy, String message) {
            this.healthy = healthy;
            this.message = message;
            this.checkTime = System.currentTimeMillis();
        }
        
        public static HealthStatus healthy() {
            return new HealthStatus(true, "OK");
        }
        
        public static HealthStatus unhealthy(String message) {
            return new HealthStatus(false, message);
        }
        
        public boolean isHealthy() {
            return healthy;
        }
        
        public String getMessage() {
            return message;
        }
        
        public long getCheckTime() {
            return checkTime;
        }
    }
}
