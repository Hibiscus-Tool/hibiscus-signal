package io.github.signal.core.eventsourcing;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 事件基类
 * 所有领域事件都应该继承此类
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public abstract class Event {

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 聚合根ID
     */
    private String aggregateId;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 事件版本
     */
    private long version;

    /**
     * 事件发生时间
     */
    private LocalDateTime occurredAt;

    /**
     * 事件元数据
     */
    private EventMetadata metadata;

    public Event() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.eventType = this.getClass().getSimpleName();
    }

    public Event(String aggregateId, long version) {
        this();
        this.aggregateId = aggregateId;
        this.version = version;
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public EventMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(EventMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * 获取事件名称（用于事件路由）
     */
    public String getEventName() {
        return this.getClass().getSimpleName();
    }
}
