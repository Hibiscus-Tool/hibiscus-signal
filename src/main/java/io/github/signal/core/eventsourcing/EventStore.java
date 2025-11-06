package io.github.signal.core.eventsourcing;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 事件存储接口
 * 定义事件存储的基本操作
 */
public interface EventStore {

    /**
     * 保存事件
     */
    CompletableFuture<Void> saveEvents(String aggregateId, List<Event> events, long expectedVersion);

    /**
     * 获取聚合根的所有事件
     */
    CompletableFuture<List<Event>> getEvents(String aggregateId);

    /**
     * 获取聚合根从指定版本开始的事件
     */
    CompletableFuture<List<Event>> getEvents(String aggregateId, long fromVersion);

    /**
     * 获取所有事件
     */
    CompletableFuture<List<Event>> getAllEvents();

    /**
     * 获取指定类型的事件
     */
    CompletableFuture<List<Event>> getEventsByType(String eventType);

    /**
     * 获取指定时间范围内的事件
     */
    CompletableFuture<List<Event>> getEventsByTimeRange(java.time.LocalDateTime start, java.time.LocalDateTime end);

    /**
     * 检查聚合根是否存在
     */
    CompletableFuture<Boolean> aggregateExists(String aggregateId);

    /**
     * 获取聚合根的当前版本
     */
    CompletableFuture<Long> getCurrentVersion(String aggregateId);

    /**
     * 删除聚合根的所有事件
     */
    CompletableFuture<Void> deleteAggregate(String aggregateId);

    /**
     * 订阅事件
     */
    void subscribe(EventSubscriber subscriber);

    /**
     * 取消订阅
     */
    void unsubscribe(EventSubscriber subscriber);
}
