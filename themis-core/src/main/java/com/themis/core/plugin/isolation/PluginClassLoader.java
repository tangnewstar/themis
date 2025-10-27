package com.themis.core.plugin.isolation;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件类加载器
 * 实现插件隔离，每个插件使用独立的ClassLoader
 */
public class PluginClassLoader extends URLClassLoader {
    
    /**
     * 插件ID
     */
    private final String pluginId;
    
    /**
     * 父类加载器委托策略
     */
    private final DelegationStrategy delegationStrategy;
    
    /**
     * 类加载缓存
     */
    private final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();
    
    /**
     * 共享类（不隔离的类）
     */
    private static final String[] SHARED_PACKAGES = {
        "java.",
        "javax.",
        "com.themis.core.plugin.",  // 插件核心接口
        "com.themis.core.context.", // 上下文
        "com.themis.core.model."    // 模型
    };
    
    public PluginClassLoader(String pluginId, URL[] urls, ClassLoader parent, 
                           DelegationStrategy delegationStrategy) {
        super(urls, parent);
        this.pluginId = pluginId;
        this.delegationStrategy = delegationStrategy;
    }
    
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // 检查缓存
        Class<?> clazz = classCache.get(name);
        if (clazz != null) {
            return clazz;
        }
        
        // 检查是否为共享类
        if (isSharedClass(name)) {
            return super.loadClass(name, resolve);
        }
        
        // 根据委托策略加载类
        if (delegationStrategy == DelegationStrategy.PARENT_FIRST) {
            clazz = loadClassParentFirst(name, resolve);
        } else {
            clazz = loadClassChildFirst(name, resolve);
        }
        
        // 缓存类
        if (clazz != null) {
            classCache.put(name, clazz);
        }
        
        return clazz;
    }
    
    /**
     * 父类优先加载
     */
    private Class<?> loadClassParentFirst(String name, boolean resolve) throws ClassNotFoundException {
        try {
            // 先从父类加载器加载
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException e) {
            // 父类加载失败，从当前加载器加载
            return findClass(name);
        }
    }
    
    /**
     * 子类优先加载（打破双亲委派）
     */
    private Class<?> loadClassChildFirst(String name, boolean resolve) throws ClassNotFoundException {
        try {
            // 先从当前加载器加载
            Class<?> clazz = findClass(name);
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        } catch (ClassNotFoundException e) {
            // 当前加载器加载失败，从父类加载器加载
            return super.loadClass(name, resolve);
        }
    }
    
    /**
     * 检查是否为共享类
     */
    private boolean isSharedClass(String className) {
        for (String sharedPackage : SHARED_PACKAGES) {
            if (className.startsWith(sharedPackage)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 卸载插件（清理缓存）
     */
    public void unload() {
        classCache.clear();
        try {
            close();
        } catch (Exception e) {
            System.err.println("Failed to close plugin class loader: " + pluginId);
        }
    }
    
    public String getPluginId() {
        return pluginId;
    }
    
    /**
     * 委托策略
     */
    public enum DelegationStrategy {
        /**
         * 父类优先（标准双亲委派模型）
         */
        PARENT_FIRST,
        
        /**
         * 子类优先（打破双亲委派，实现更强隔离）
         */
        CHILD_FIRST
    }
}
