package com.themis.core.plugin.spi;

import java.util.ServiceLoader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI服务加载器
 * 基于Java SPI机制实现插件动态加载
 */
public class ThemisSPILoader {
    
    /**
     * 服务缓存
     */
    private static final Map<Class<?>, List<?>> SERVICE_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 加载指定类型的所有服务实现
     * 
     * @param serviceClass 服务接口类
     * @param <T> 服务类型
     * @return 服务实现列表
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> loadServices(Class<T> serviceClass) {
        return (List<T>) SERVICE_CACHE.computeIfAbsent(serviceClass, key -> {
            List<T> services = new ArrayList<>();
            ServiceLoader<T> loader = ServiceLoader.load(serviceClass);
            
            for (T service : loader) {
                services.add(service);
            }
            
            return services;
        });
    }
    
    /**
     * 加载指定类型的单个服务实现（返回第一个）
     * 
     * @param serviceClass 服务接口类
     * @param <T> 服务类型
     * @return 服务实现，如果没有则返回null
     */
    public static <T> T loadService(Class<T> serviceClass) {
        List<T> services = loadServices(serviceClass);
        return services.isEmpty() ? null : services.get(0);
    }
    
    /**
     * 加载指定类型和名称的服务实现
     * 
     * @param serviceClass 服务接口类
     * @param serviceName 服务名称
     * @param <T> 服务类型
     * @return 服务实现，如果没有则返回null
     */
    public static <T> T loadService(Class<T> serviceClass, String serviceName) {
        List<T> services = loadServices(serviceClass);
        
        for (T service : services) {
            if (service instanceof NamedService) {
                NamedService namedService = (NamedService) service;
                if (serviceName.equals(namedService.getName())) {
                    return service;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 重新加载服务（清除缓存）
     * 
     * @param serviceClass 服务接口类
     */
    public static void reload(Class<?> serviceClass) {
        SERVICE_CACHE.remove(serviceClass);
    }
    
    /**
     * 清除所有缓存
     */
    public static void clearCache() {
        SERVICE_CACHE.clear();
    }
    
    /**
     * 命名服务接口
     */
    public interface NamedService {
        /**
         * 获取服务名称
         */
        String getName();
    }
}
