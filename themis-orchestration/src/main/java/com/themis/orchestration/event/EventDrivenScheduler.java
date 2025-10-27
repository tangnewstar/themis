package com.themis.orchestration.event;

import com.themis.core.context.RuleContext;
import com.themis.core.engine.RuleEngine;
import com.themis.core.model.RuleDefinition;
import com.themis.core.model.RuleExecutionResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件驱动调度器
 * 基于事件触发规则执行
 */
public class EventDrivenScheduler {
    
    /**
     * 事件总线
     */
    private final EventBus eventBus;
    
    /**
     * 规则引擎
     */
    private final RuleEngine ruleEngine;
    
    /**
     * 事件路由映射（事件类型 -> 规则列表）
     */
    private final Map<String, List<RuleDefinition>> eventRoutes = new ConcurrentHashMap<>();
    
    /**
     * 事件过滤器
     */
    private final Map<String, EventFilter> eventFilters = new ConcurrentHashMap<>();
    
    public EventDrivenScheduler(EventBus eventBus, RuleEngine ruleEngine) {
        this.eventBus = eventBus;
        this.ruleEngine = ruleEngine;
    }
    
    /**
     * 注册事件路由
     * 
     * @param eventType 事件类型
     * @param rule 规则
     */
    public void registerRoute(String eventType, RuleDefinition rule) {
        eventRoutes.computeIfAbsent(eventType, k -> new ArrayList<>()).add(rule);
        
        // 订阅事件
        eventBus.subscribe(eventType, event -> handleEvent(event));
    }
    
    /**
     * 注册事件过滤器
     * 
     * @param eventType 事件类型
     * @param filter 过滤器
     */
    public void registerFilter(String eventType, EventFilter filter) {
        eventFilters.put(eventType, filter);
    }
    
    /**
     * 处理事件
     */
    private void handleEvent(Event event) {
        String eventType = event.getEventType();
        
        // 检查过滤器
        EventFilter filter = eventFilters.get(eventType);
        if (filter != null && !filter.accept(event)) {
            return;
        }
        
        // 获取匹配的规则
        List<RuleDefinition> rules = eventRoutes.get(eventType);
        if (rules == null || rules.isEmpty()) {
            return;
        }
        
        // 创建规则上下文
        RuleContext context = createContextFromEvent(event);
        
        // 执行规则
        for (RuleDefinition rule : rules) {
            try {
                RuleExecutionResult result = ruleEngine.execute(rule, context);
                
                // 发布执行结果事件
                publishResultEvent(event, rule, result);
                
            } catch (Exception e) {
                handleExecutionError(event, rule, e);
            }
        }
    }
    
    /**
     * 从事件创建规则上下文
     */
    private RuleContext createContextFromEvent(Event event) {
        RuleContext context = new RuleContext();
        
        // 添加事件数据到上下文
        if (event.getData() != null) {
            event.getData().forEach(context::setAttribute);
        }
        
        // 添加事件元数据
        context.setAttribute("__event_id", event.getEventId());
        context.setAttribute("__event_type", event.getEventType());
        context.setAttribute("__event_source", event.getSource());
        context.setAttribute("__event_timestamp", event.getTimestamp());
        
        return context;
    }
    
    /**
     * 发布执行结果事件
     */
    private void publishResultEvent(Event originalEvent, RuleDefinition rule, RuleExecutionResult result) {
        Event resultEvent = Event.builder()
            .eventType("rule.execution.completed")
            .source("EventDrivenScheduler")
            .addData("originalEventId", originalEvent.getEventId())
            .addData("ruleId", rule.getRuleId())
            .addData("success", result.isSuccess())
            .addData("result", result.getResult())
            .build();
        
        eventBus.publish(resultEvent);
    }
    
    /**
     * 处理执行错误
     */
    private void handleExecutionError(Event event, RuleDefinition rule, Exception e) {
        Event errorEvent = Event.builder()
            .eventType("rule.execution.error")
            .source("EventDrivenScheduler")
            .addData("originalEventId", event.getEventId())
            .addData("ruleId", rule.getRuleId())
            .addData("error", e.getMessage())
            .build();
        
        eventBus.publish(errorEvent);
    }
    
    /**
     * 取消路由
     */
    public void unregisterRoute(String eventType, RuleDefinition rule) {
        List<RuleDefinition> rules = eventRoutes.get(eventType);
        if (rules != null) {
            rules.remove(rule);
        }
    }
    
    /**
     * 清除所有路由
     */
    public void clearRoutes() {
        eventRoutes.clear();
    }
    
    /**
     * 获取路由统计
     */
    public Map<String, Integer> getRouteStats() {
        Map<String, Integer> stats = new HashMap<>();
        for (Map.Entry<String, List<RuleDefinition>> entry : eventRoutes.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().size());
        }
        return stats;
    }
    
    /**
     * 事件过滤器接口
     */
    @FunctionalInterface
    public interface EventFilter {
        boolean accept(Event event);
    }
}
