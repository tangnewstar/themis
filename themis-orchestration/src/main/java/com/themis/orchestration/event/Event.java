package com.themis.orchestration.event;

import java.util.HashMap;
import java.util.Map;

/**
 * 事件
 * 规则引擎中的事件对象
 */
public class Event {
    
    /**
     * 事件ID
     */
    private final String eventId;
    
    /**
     * 事件类型
     */
    private final String eventType;
    
    /**
     * 事件源
     */
    private final String source;
    
    /**
     * 事件时间戳
     */
    private final long timestamp;
    
    /**
     * 事件数据
     */
    private final Map<String, Object> data;
    
    /**
     * 事件元数据
     */
    private final Map<String, Object> metadata;
    
    private Event(Builder builder) {
        this.eventId = builder.eventId;
        this.eventType = builder.eventType;
        this.source = builder.source;
        this.timestamp = builder.timestamp;
        this.data = builder.data;
        this.metadata = builder.metadata;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public String getSource() {
        return source;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public Object getData(String key) {
        return data != null ? data.get(key) : null;
    }
    
    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String eventId;
        private String eventType;
        private String source;
        private long timestamp = System.currentTimeMillis();
        private Map<String, Object> data = new HashMap<>();
        private Map<String, Object> metadata = new HashMap<>();
        
        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }
        
        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }
        
        public Builder source(String source) {
            this.source = source;
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder data(Map<String, Object> data) {
            this.data = data;
            return this;
        }
        
        public Builder addData(String key, Object value) {
            this.data.put(key, value);
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder addMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public Event build() {
            if (eventId == null) {
                eventId = java.util.UUID.randomUUID().toString();
            }
            return new Event(this);
        }
    }
    
    @Override
    public String toString() {
        return "Event{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", source='" + source + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
