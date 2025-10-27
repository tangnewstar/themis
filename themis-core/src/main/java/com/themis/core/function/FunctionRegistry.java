package com.themis.core.function;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 函数注册中心
 * 管理自定义函数的注册、查找和调用
 */
public class FunctionRegistry {
    
    private static final FunctionRegistry INSTANCE = new FunctionRegistry();
    
    private final Map<String, CustomFunction> functions = new ConcurrentHashMap<>();
    private final Map<String, Map<String, CustomFunction>> namespaces = new ConcurrentHashMap<>();
    
    private FunctionRegistry() {
    }
    
    public static FunctionRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册函数
     * 
     * @param function 自定义函数
     */
    public void register(CustomFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("Function cannot be null");
        }
        
        String fullName = getFullName(function);
        functions.put(fullName, function);
        
        // 按命名空间组织
        String namespace = function.getNamespace();
        if (namespace != null && !namespace.isEmpty()) {
            namespaces.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>())
                     .put(function.getName(), function);
        }
    }
    
    /**
     * 批量注册函数
     * 
     * @param functions 函数列表
     */
    public void registerAll(List<CustomFunction> functions) {
        if (functions != null) {
            functions.forEach(this::register);
        }
    }
    
    /**
     * 注销函数
     * 
     * @param name 函数名
     * @param namespace 命名空间
     */
    public void unregister(String name, String namespace) {
        String fullName = buildFullName(name, namespace);
        functions.remove(fullName);
        
        if (namespace != null && !namespace.isEmpty()) {
            Map<String, CustomFunction> nsMap = namespaces.get(namespace);
            if (nsMap != null) {
                nsMap.remove(name);
            }
        }
    }
    
    /**
     * 查找函数
     * 
     * @param name 函数名
     * @param namespace 命名空间(可选)
     * @return 函数实例,未找到返回null
     */
    public CustomFunction findFunction(String name, String namespace) {
        String fullName = buildFullName(name, namespace);
        return functions.get(fullName);
    }
    
    /**
     * 查找函数(默认命名空间)
     * 
     * @param name 函数名
     * @return 函数实例,未找到返回null
     */
    public CustomFunction findFunction(String name) {
        return findFunction(name, null);
    }
    
    /**
     * 获取指定命名空间的所有函数
     * 
     * @param namespace 命名空间
     * @return 函数列表
     */
    public List<CustomFunction> getFunctionsByNamespace(String namespace) {
        Map<String, CustomFunction> nsMap = namespaces.get(namespace);
        return nsMap != null ? new ArrayList<>(nsMap.values()) : Collections.emptyList();
    }
    
    /**
     * 获取所有函数
     * 
     * @return 函数列表
     */
    public List<CustomFunction> getAllFunctions() {
        return new ArrayList<>(functions.values());
    }
    
    /**
     * 获取所有命名空间
     * 
     * @return 命名空间列表
     */
    public Set<String> getAllNamespaces() {
        return new HashSet<>(namespaces.keySet());
    }
    
    /**
     * 检查函数是否存在
     * 
     * @param name 函数名
     * @param namespace 命名空间
     * @return 存在返回true
     */
    public boolean exists(String name, String namespace) {
        return findFunction(name, namespace) != null;
    }
    
    /**
     * 清除所有函数
     */
    public void clear() {
        functions.clear();
        namespaces.clear();
    }
    
    /**
     * 清除指定命名空间的函数
     * 
     * @param namespace 命名空间
     */
    public void clearNamespace(String namespace) {
        Map<String, CustomFunction> nsMap = namespaces.remove(namespace);
        if (nsMap != null) {
            for (String name : nsMap.keySet()) {
                String fullName = buildFullName(name, namespace);
                functions.remove(fullName);
            }
        }
    }
    
    /**
     * 获取函数统计信息
     */
    public FunctionStats getStats() {
        return new FunctionStats(
            functions.size(),
            namespaces.size()
        );
    }
    
    private String getFullName(CustomFunction function) {
        return buildFullName(function.getName(), function.getNamespace());
    }
    
    private String buildFullName(String name, String namespace) {
        if (namespace == null || namespace.isEmpty()) {
            return name;
        }
        return namespace + ":" + name;
    }
    
    /**
     * 函数统计信息
     */
    public static class FunctionStats {
        private final int totalFunctions;
        private final int namespaceCount;
        
        public FunctionStats(int totalFunctions, int namespaceCount) {
            this.totalFunctions = totalFunctions;
            this.namespaceCount = namespaceCount;
        }
        
        public int getTotalFunctions() {
            return totalFunctions;
        }
        
        public int getNamespaceCount() {
            return namespaceCount;
        }
        
        @Override
        public String toString() {
            return String.format("FunctionStats{total=%d, namespaces=%d}",
                               totalFunctions, namespaceCount);
        }
    }
}
