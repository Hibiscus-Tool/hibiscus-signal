package io.github.signal.core.eventsourcing;

/**
 * 事件订阅者接口
 * 用于订阅和处理事件
 */
@FunctionalInterface
public interface EventSubscriber {

    /**
     * 处理事件
     */
    void handleEvent(Event event);

    /**
     * 获取订阅者名称
     */
    default String getSubscriberName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 获取订阅的事件类型
     * 返回null表示订阅所有事件
     */
    default String getEventType() {
        return null;
    }

    /**
     * 检查是否应该处理此事件
     */
    default boolean shouldHandle(Event event) {
        String eventType = getEventType();
        return eventType == null || eventType.equals(event.getEventType());
    }
}
