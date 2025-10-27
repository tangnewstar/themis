package com.themis.orchestration.event;

import java.util.*;
import java.util.concurrent.*;

/**
 * 事件总线
 * 支持事件发布订阅和异步处理
 */
public class EventBus {
    
    /**
     * 事件监听器映射
     */
    private final Map<String, List<EventListener>> listeners = new ConcurrentHashMap<>();
    
    /**
     * 异步执行器
     */
    private final ExecutorService executorService;
    
    /**
     * 是否异步执行
     */
    private final boolean async;
    
    public EventBus() {
        this(true, Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }
    
    public EventBus(boolean async, ExecutorService executorService) {
        this.async = async;
        this.executorService = executorService;
    }
    
    /**
     * 订阅事件
     * 
     * @param eventType 事件类型
     * @param listener 监听器
     */
    public void subscribe(String eventType, EventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(listener);
    }
    
    /**
     * 取消订阅
     * 
     * @param eventType 事件类型
     * @param listener 监听器
     */
    public void unsubscribe(String eventType, EventListener listener) {
        List<EventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }
    
    /**
     * 发布事件
     * 
     * @param event 事件
     */
    public void publish(Event event) {
        List<EventListener> eventListeners = listeners.get(event.getEventType());
        
        if (eventListeners == null || eventListeners.isEmpty()) {
            return;
        }
        
        if (async) {
            publishAsync(event, eventListeners);
        } else {
            publishSync(event, eventListeners);
        }
    }
    
    /**
     * 同步发布事件
     */
    private void publishSync(Event event, List<EventListener> eventListeners) {
        for (EventListener listener : eventListeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                handleListenerError(listener, event, e);
            }
        }
    }
    
    /**
     * 异步发布事件
     */
    private void publishAsync(Event event, List<EventListener> eventListeners) {
        for (EventListener listener : eventListeners) {
            executorService.submit(() -> {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    handleListenerError(listener, event, e);
                }
            });
        }
    }
    
    /**
     * 处理监听器错误
     */
    private void handleListenerError(EventListener listener, Event event, Exception e) {
        System.err.println("Error in event listener: " + listener.getClass().getName() + 
                          ", event: " + event.getEventType() + ", error: " + e.getMessage());
        e.printStackTrace();
    }
    
    /**
     * 清除所有监听器
     */
    public void clear() {
        listeners.clear();
    }
    
    /**
     * 关闭事件总线
     */
    public void shutdown() {
        executorService.shutdown();
    }
    
    /**
     * 获取订阅统计
     */
    public Map<String, Integer> getSubscriptionStats() {
        Map<String, Integer> stats = new HashMap<>();
        for (Map.Entry<String, List<EventListener>> entry : listeners.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().size());
        }
        return stats;
    }
}
