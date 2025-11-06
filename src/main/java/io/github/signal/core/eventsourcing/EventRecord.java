package io.github.signal.core.eventsourcing;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 事件记录实体
 * 用于数据库持久化存储事件溯源事件
 */
@Entity
@Table(name = "event_sourcing_events", indexes = {
    @Index(name = "idx_aggregate_id", columnList = "aggregate_id"),
    @Index(name = "idx_event_type", columnList = "event_type"),
    @Index(name = "idx_occurred_at", columnList = "occurred_at"),
    @Index(name = "idx_event_id", columnList = "event_id", unique = true),
    @Index(name = "idx_aggregate_version", columnList = "aggregate_id,version")
})
public class EventRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(name = "event_name", nullable = false, length = 128)
    private String eventName;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "event_data", columnDefinition = "TEXT", nullable = false)
    private String eventData;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Version
    @Column(name = "version_lock")
    private Long versionLock = 0L;

    public EventRecord() {
        this.createdTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getEventData() {
        return eventData;
    }

    public void setEventData(String eventData) {
        this.eventData = eventData;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public Long getVersionLock() {
        return versionLock;
    }

    public void setVersionLock(Long versionLock) {
        this.versionLock = versionLock;
    }

    @Override
    public String toString() {
        return "EventRecord{" +
                "id=" + id +
                ", eventId='" + eventId + '\'' +
                ", eventName='" + eventName + '\'' +
                ", aggregateId='" + aggregateId + '\'' +
                ", version=" + version +
                ", occurredAt=" + occurredAt +
                '}';
    }
}
