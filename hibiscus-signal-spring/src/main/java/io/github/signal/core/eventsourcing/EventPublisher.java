package io.github.signal.core.eventsourcing;

/**
 * 事件发布者接口
 * 负责将事件发布到事件总线或其他订阅者
 */
public interface EventPublisher {

    /**
     * 发布事件
     */
    void publish(Event event);

    /**
     * 批量发布事件
     */
    void publishAll(java.util.List<Event> events);

    /**
     * 异步发布事件
     */
    void publishAsync(Event event);

    /**
     * 异步批量发布事件
     */
    void publishAllAsync(java.util.List<Event> events);
}
