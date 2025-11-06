package io.github.signal.core.eventsourcing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件溯源管理器
 * 负责协调事件存储、聚合根管理和事件发布
 */
@Component
public class EventSourcingManager {

    private static final Logger log = LoggerFactory.getLogger(EventSourcingManager.class);

    @Autowired
    private EventStore eventStore;

    @Autowired
    private EventPublisher eventPublisher;

    /**
     * 聚合根工厂映射
     */
    private final Map<String, AggregateFactory<?>> aggregateFactories = new ConcurrentHashMap<>();

    /**
     * 注册聚合根工厂
     */
    public void registerAggregateFactory(String aggregateType, AggregateFactory<?> factory) {
        aggregateFactories.put(aggregateType, factory);
        log.info("注册聚合根工厂: {} -> {}", aggregateType, factory.getClass().getSimpleName());
    }

    /**
     * 保存聚合根
     */
    public <T extends AggregateRoot> CompletableFuture<Void> save(T aggregate) {
        if (!aggregate.hasUncommittedEvents()) {
            return CompletableFuture.completedFuture(null);
        }

        List<Event> events = aggregate.getUncommittedEvents();
        long expectedVersion = aggregate.getVersion() - events.size();

        return eventStore.saveEvents(aggregate.getId(), events, expectedVersion)
                .thenRun(() -> {
                    // 发布事件
                    events.forEach(event -> eventPublisher.publish(event));
                    
                    // 标记事件为已提交
                    aggregate.markEventsAsCommitted();
                    
                    log.debug("聚合根 {} 保存成功，版本: {}", aggregate.getId(), aggregate.getVersion());
                })
                .exceptionally(throwable -> {
                    log.error("保存聚合根失败: {}", aggregate.getId(), throwable);
                    throw new RuntimeException("保存聚合根失败", throwable);
                });
    }

    /**
     * 重建聚合根
     */
    public <T extends AggregateRoot> CompletableFuture<T> rebuild(String aggregateId, String aggregateType) {
        AggregateFactory<T> factory = (AggregateFactory<T>) aggregateFactories.get(aggregateType);
        if (factory == null) {
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("未找到聚合根工厂: " + aggregateType));
            return future;
        }

        return eventStore.getEvents(aggregateId)
                .thenApply(events -> {
                    if (events.isEmpty()) {
                        return null;
                    }

                    T aggregate = factory.createAggregate(aggregateId);
                    aggregate.loadFromHistory(events);
                    log.debug("聚合根 {} 重建成功，版本: {}", aggregateId, aggregate.getVersion());
                    return aggregate;
                })
                .exceptionally(throwable -> {
                    log.error("重建聚合根失败: {}", aggregateId, throwable);
                    throw new RuntimeException("重建聚合根失败", throwable);
                });
    }

    /**
     * 获取聚合根
     */
    public <T extends AggregateRoot> CompletableFuture<T> getAggregate(String aggregateId, String aggregateType) {
        return eventStore.aggregateExists(aggregateId)
                .thenCompose(exists -> {
                    if (!exists) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return rebuild(aggregateId, aggregateType);
                });
    }

    /**
     * 删除聚合根
     */
    public CompletableFuture<Void> deleteAggregate(String aggregateId) {
        return eventStore.deleteAggregate(aggregateId)
                .thenRun(() -> log.info("聚合根 {} 删除成功", aggregateId))
                .exceptionally(throwable -> {
                    log.error("删除聚合根失败: {}", aggregateId, throwable);
                    throw new RuntimeException("删除聚合根失败", throwable);
                });
    }

    /**
     * 获取聚合根版本
     */
    public CompletableFuture<Long> getAggregateVersion(String aggregateId) {
        return eventStore.getCurrentVersion(aggregateId);
    }

    /**
     * 获取聚合根事件
     */
    public CompletableFuture<List<Event>> getAggregateEvents(String aggregateId) {
        return eventStore.getEvents(aggregateId);
    }

    /**
     * 获取聚合根事件（从指定版本开始）
     */
    public CompletableFuture<List<Event>> getAggregateEvents(String aggregateId, long fromVersion) {
        return eventStore.getEvents(aggregateId, fromVersion);
    }

    /**
     * 获取所有事件
     */
    public CompletableFuture<List<Event>> getAllEvents() {
        return eventStore.getAllEvents();
    }

    /**
     * 获取指定类型的事件
     */
    public CompletableFuture<List<Event>> getEventsByType(String eventType) {
        return eventStore.getEventsByType(eventType);
    }

    /**
     * 获取指定时间范围内的事件
     */
    public CompletableFuture<List<Event>> getEventsByTimeRange(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return eventStore.getEventsByTimeRange(start, end);
    }

    /**
     * 订阅事件
     */
    public void subscribe(EventSubscriber subscriber) {
        eventStore.subscribe(subscriber);
        log.info("事件订阅者注册成功: {}", subscriber.getSubscriberName());
    }

    /**
     * 取消订阅
     */
    public void unsubscribe(EventSubscriber subscriber) {
        eventStore.unsubscribe(subscriber);
        log.info("事件订阅者取消注册: {}", subscriber.getSubscriberName());
    }
}
