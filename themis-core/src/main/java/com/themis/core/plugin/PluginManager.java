package com.themis.core.plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件管理器
 * 负责插件的注册、生命周期管理和调度
 */
public class PluginManager {
    
    private static final PluginManager INSTANCE = new PluginManager();
    
    private final Map<String, ThemisPlugin> plugins = new ConcurrentHashMap<>();
    private final Map<ThemisPlugin.PluginType, List<ThemisPlugin>> pluginsByType = new ConcurrentHashMap<>();
    
    private PluginManager() {
    }
    
    public static PluginManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册插件
     * 
     * @param plugin 插件实例
     * @throws PluginException 注册失败时抛出
     */
    public void register(ThemisPlugin plugin) throws PluginException {
        if (plugin == null) {
            throw new PluginException("null", "Plugin cannot be null");
        }
        
        String pluginId = plugin.getPluginId();
        if (plugins.containsKey(pluginId)) {
            throw new PluginException(pluginId, "Plugin already registered");
        }
        
        plugins.put(pluginId, plugin);
        
        // 按类型分组
        pluginsByType.computeIfAbsent(plugin.getPluginType(), k -> new ArrayList<>()).add(plugin);
    }
    
    /**
     * 取消注册插件
     * 
     * @param pluginId 插件ID
     */
    public void unregister(String pluginId) {
        ThemisPlugin plugin = plugins.remove(pluginId);
        if (plugin != null) {
            List<ThemisPlugin> typePlugins = pluginsByType.get(plugin.getPluginType());
            if (typePlugins != null) {
                typePlugins.remove(plugin);
            }
            
            // 销毁插件
            try {
                if (plugin.getStatus() == ThemisPlugin.PluginStatus.STARTED) {
                    plugin.stop();
                }
                plugin.destroy();
            } catch (Exception e) {
                // 忽略销毁时的异常
            }
        }
    }
    
    /**
     * 获取插件
     * 
     * @param pluginId 插件ID
     * @return 插件实例,如果不存在返回null
     */
    public ThemisPlugin getPlugin(String pluginId) {
        return plugins.get(pluginId);
    }
    
    /**
     * 获取指定类型的所有插件
     * 
     * @param type 插件类型
     * @return 插件列表
     */
    public List<ThemisPlugin> getPluginsByType(ThemisPlugin.PluginType type) {
        return new ArrayList<>(pluginsByType.getOrDefault(type, Collections.emptyList()));
    }
    
    /**
     * 获取所有插件
     * 
     * @return 插件列表
     */
    public List<ThemisPlugin> getAllPlugins() {
        return new ArrayList<>(plugins.values());
    }
    
    /**
     * 初始化所有插件
     */
    public void initAll() throws PluginException {
        for (ThemisPlugin plugin : plugins.values()) {
            if (plugin.getStatus() == ThemisPlugin.PluginStatus.CREATED) {
                plugin.init(plugin.getConfig());
            }
        }
    }
    
    /**
     * 启动所有插件
     */
    public void startAll() throws PluginException {
        for (ThemisPlugin plugin : plugins.values()) {
            if (plugin.getStatus() == ThemisPlugin.PluginStatus.INITIALIZED) {
                plugin.start();
            }
        }
    }
    
    /**
     * 停止所有插件
     */
    public void stopAll() {
        for (ThemisPlugin plugin : plugins.values()) {
            try {
                if (plugin.getStatus() == ThemisPlugin.PluginStatus.STARTED) {
                    plugin.stop();
                }
            } catch (Exception e) {
                // 继续停止其他插件
            }
        }
    }
    
    /**
     * 销毁所有插件
     */
    public void destroyAll() {
        for (ThemisPlugin plugin : plugins.values()) {
            try {
                plugin.destroy();
            } catch (Exception e) {
                // 继续销毁其他插件
            }
        }
        plugins.clear();
        pluginsByType.clear();
    }
    
    /**
     * 执行插件健康检查
     * 
     * @return 健康检查结果映射
     */
    public Map<String, ThemisPlugin.HealthStatus> healthCheckAll() {
        Map<String, ThemisPlugin.HealthStatus> results = new HashMap<>();
        for (Map.Entry<String, ThemisPlugin> entry : plugins.entrySet()) {
            try {
                results.put(entry.getKey(), entry.getValue().healthCheck());
            } catch (Exception e) {
                results.put(entry.getKey(), 
                    ThemisPlugin.HealthStatus.unhealthy("Health check failed: " + e.getMessage()));
            }
        }
        return results;
    }
    
    /**
     * 获取插件统计信息
     */
    public PluginStats getStats() {
        int total = plugins.size();
        int started = 0;
        int stopped = 0;
        int error = 0;
        
        for (ThemisPlugin plugin : plugins.values()) {
            switch (plugin.getStatus()) {
                case STARTED:
                    started++;
                    break;
                case STOPPED:
                    stopped++;
                    break;
                case ERROR:
                    error++;
                    break;
            }
        }
        
        return new PluginStats(total, started, stopped, error);
    }
    
    /**
     * 插件统计信息
     */
    public static class PluginStats {
        private final int total;
        private final int started;
        private final int stopped;
        private final int error;
        
        public PluginStats(int total, int started, int stopped, int error) {
            this.total = total;
            this.started = started;
            this.stopped = stopped;
            this.error = error;
        }
        
        public int getTotal() {
            return total;
        }
        
        public int getStarted() {
            return started;
        }
        
        public int getStopped() {
            return stopped;
        }
        
        public int getError() {
            return error;
        }
        
        @Override
        public String toString() {
            return String.format("PluginStats{total=%d, started=%d, stopped=%d, error=%d}",
                               total, started, stopped, error);
        }
    }
}
