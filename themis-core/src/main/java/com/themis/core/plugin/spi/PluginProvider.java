package com.themis.core.plugin.spi;

import com.themis.core.plugin.Plugin;

/**
 * 插件SPI接口
 * 用于通过SPI机制动态加载插件
 */
public interface PluginProvider extends ThemisSPILoader.NamedService {
    
    /**
     * 创建插件实例
     * 
     * @return 插件实例
     */
    Plugin createPlugin();
    
    /**
     * 获取插件类型
     */
    String getPluginType();
    
    /**
     * 获取插件版本
     */
    String getVersion();
    
    /**
     * 获取插件优先级
     * 数值越大优先级越高
     */
    default int getPriority() {
        return 0;
    }
}
