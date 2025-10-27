package com.themis.core.plugin;

/**
 * 插件异常
 */
public class PluginException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final String pluginId;
    
    public PluginException(String pluginId, String message) {
        super(String.format("Plugin '%s' error: %s", pluginId, message));
        this.pluginId = pluginId;
    }
    
    public PluginException(String pluginId, String message, Throwable cause) {
        super(String.format("Plugin '%s' error: %s", pluginId, message), cause);
        this.pluginId = pluginId;
    }
    
    public PluginException(String pluginId, Throwable cause) {
        super(String.format("Plugin '%s' error", pluginId), cause);
        this.pluginId = pluginId;
    }
    
    public String getPluginId() {
        return pluginId;
    }
}
