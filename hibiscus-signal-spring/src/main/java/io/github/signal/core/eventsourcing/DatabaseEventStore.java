package io.github.signal.core.eventsourcing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 基于数据库的事件存储实现
 */
@Component
public class DatabaseEventStore implements EventStore {

    private static final Logger log = LoggerFactory.getLogger(DatabaseEventStore.class);

    @Autowired
    private EventRecordRepository eventRecordRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 事件订阅者列表
     */
    private final List<EventSubscriber> subscribers = new CopyOnWriteArrayList<>();

    @Override
    @Transactional
    public CompletableFuture<Void> saveEvents(String aggregateId, List<Event> events, long expectedVersion) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 检查版本一致性
                Long currentVersion = eventRecordRepository.getCurrentVersion(aggregateId);
                if (currentVersion != null && currentVersion != expectedVersion) {
                    throw new RuntimeException("版本不一致，期望版本: " + expectedVersion + ", 实际版本: " + currentVersion);
                }

                // 保存每个事件
                for (Event event : events) {
                    EventRecord eventRecord = new EventRecord();
                    eventRecord.setEventId(event.getEventId());
                    eventRecord.setEventName(event.getEventName());
                    eventRecord.setAggregateId(aggregateId);
                    eventRecord.setEventType(event.getEventType());
                    eventRecord.setVersion(event.getVersion());
                    eventRecord.setOccurredAt(event.getOccurredAt());
                    
                    // 序列化事件数据
                    if (event.getMetadata() != null) {
                        eventRecord.setMetadata(objectMapper.writeValueAsString(event.getMetadata()));
                    }
                    
                    // 序列化事件内容
                    eventRecord.setEventData(objectMapper.writeValueAsString(event));
                    
                    eventRecordRepository.save(eventRecord);
                }

                log.info("事件保存成功，聚合根: {}, 事件数量: {}", aggregateId, events.size());
                return null;

            } catch (Exception e) {
                log.error("保存事件失败，聚合根: {}", aggregateId, e);
                throw new RuntimeException("保存事件失败", e);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<List<Event>> getEvents(String aggregateId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<EventRecord> records = eventRecordRepository.findByAggregateIdOrderByVersion(aggregateId);
                return deserializeEvents(records);
            } catch (Exception e) {
                log.error("获取事件失败，聚合根: {}", aggregateId, e);
                throw new RuntimeException("获取事件失败", e);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<List<Event>> getEvents(String aggregateId, long fromVersion) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<EventRecord> records = eventRecordRepository.findByAggregateIdAndVersionGreaterThanOrderByVersion(aggregateId, fromVersion);
                return deserializeEvents(records);
            } catch (Exception e) {
                log.error("获取事件失败，聚合根: {}, 版本: {}", aggregateId, fromVersion, e);
                throw new RuntimeException("获取事件失败", e);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<List<Event>> getAllEvents() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<EventRecord> records = eventRecordRepository.findAllByOrderByOccurredAt();
                return deserializeEvents(records);
            } catch (Exception e) {
                log.error("获取所有事件失败", e);
                throw new RuntimeException("获取所有事件失败", e);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<List<Event>> getEventsByType(String eventType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<EventRecord> records = eventRecordRepository.findByEventTypeOrderByOccurredAt(eventType);
                return deserializeEvents(records);
            } catch (Exception e) {
                log.error("获取事件失败，类型: {}", eventType, e);
                throw new RuntimeException("获取事件失败", e);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<List<Event>> getEventsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<EventRecord> records = eventRecordRepository.findByOccurredAtBetweenOrderByOccurredAt(start, end);
                return deserializeEvents(records);
            } catch (Exception e) {
                log.error("获取事件失败，时间范围: {} - {}", start, end, e);
                throw new RuntimeException("获取事件失败", e);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<Boolean> aggregateExists(String aggregateId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return eventRecordRepository.existsByAggregateId(aggregateId);
            } catch (Exception e) {
                log.error("检查聚合根存在性失败: {}", aggregateId, e);
                return false;
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<Long> getCurrentVersion(String aggregateId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return eventRecordRepository.getCurrentVersion(aggregateId);
            } catch (Exception e) {
                log.error("获取当前版本失败: {}", aggregateId, e);
                return 0L;
            }
        });
    }

    @Override
    @Transactional
    public CompletableFuture<Void> deleteAggregate(String aggregateId) {
        return CompletableFuture.runAsync(() -> {
            try {
                eventRecordRepository.deleteByAggregateId(aggregateId);
                log.info("聚合根删除成功: {}", aggregateId);
            } catch (Exception e) {
                log.error("删除聚合根失败: {}", aggregateId, e);
                throw new RuntimeException("删除聚合根失败", e);
            }
        });
    }

    @Override
    public void subscribe(EventSubscriber subscriber) {
        subscribers.add(subscriber);
        log.info("事件订阅者注册成功: {}", subscriber.getSubscriberName());
    }

    @Override
    public void unsubscribe(EventSubscriber subscriber) {
        subscribers.remove(subscriber);
        log.info("事件订阅者取消注册: {}", subscriber.getSubscriberName());
    }

    /**
     * 反序列化事件记录为事件对象
     */
    private List<Event> deserializeEvents(List<EventRecord> records) {
        List<Event> events = new ArrayList<>();
        
        for (EventRecord record : records) {
            try {
                Event event = objectMapper.readValue(record.getEventData(), Event.class);
                events.add(event);
            } catch (JsonProcessingException e) {
                log.error("反序列化事件失败: {}", record.getEventId(), e);
            }
        }
        
        return events;
    }

    /**
     * 通知所有订阅者
     */
    private void notifySubscribers(Event event) {
        for (EventSubscriber subscriber : subscribers) {
            if (subscriber.shouldHandle(event)) {
                try {
                    subscriber.handleEvent(event);
                } catch (Exception e) {
                    log.error("事件订阅者处理事件失败: {}", subscriber.getSubscriberName(), e);
                }
            }
        }
    }
}
