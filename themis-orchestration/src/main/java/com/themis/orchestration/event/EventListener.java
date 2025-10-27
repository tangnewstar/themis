package com.themis.orchestration.event;

/**
 * 事件监听器接口
 */
@FunctionalInterface
public interface EventListener {
    
    /**
     * 处理事件
     * 
     * @param event 事件
     */
    void onEvent(Event event);
}
