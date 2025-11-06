package io.github.signal.core.eventsourcing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 聚合根基类
 * 所有聚合根都应该继承此类
 */
public abstract class AggregateRoot {

    /**
     * 聚合根ID
     */
    private String id;

    /**
     * 聚合根版本
     */
    private long version;

    /**
     * 未提交的事件列表
     */
    private final List<Event> uncommittedEvents = new ArrayList<>();

    /**
     * 构造函数
     */
    protected AggregateRoot() {
        this.id = UUID.randomUUID().toString();
        this.version = 0;
    }

    /**
     * 构造函数
     */
    protected AggregateRoot(String id, long version) {
        this.id = id;
        this.version = version;
    }

    /**
     * 获取聚合根ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置聚合根ID
     */
    protected void setId(String id) {
        this.id = id;
    }

    /**
     * 获取聚合根版本
     */
    public long getVersion() {
        return version;
    }

    /**
     * 设置聚合根版本
     */
    protected void setVersion(long version) {
        this.version = version;
    }

    /**
     * 获取未提交的事件列表
     */
    public List<Event> getUncommittedEvents() {
        return Collections.unmodifiableList(uncommittedEvents);
    }

    /**
     * 清除未提交的事件
     */
    public void clearUncommittedEvents() {
        uncommittedEvents.clear();
    }

    /**
     * 应用事件到聚合根
     * 子类应该重写此方法来实现具体的事件处理逻辑
     */
    protected abstract void apply(Event event);

    /**
     * 应用事件并添加到未提交事件列表
     */
    protected void applyAndRecord(Event event) {
        // 设置事件的基本信息
        event.setAggregateId(this.id);
        event.setVersion(this.version + 1);

        // 应用事件到聚合根
        apply(event);

        // 增加版本号
        this.version++;

        // 添加到未提交事件列表
        uncommittedEvents.add(event);
    }

    /**
     * 从事件流重建聚合根
     */
    public void loadFromHistory(List<Event> events) {
        for (Event event : events) {
            apply(event);
            this.version = event.getVersion();
        }
    }

    /**
     * 检查是否有未提交的事件
     */
    public boolean hasUncommittedEvents() {
        return !uncommittedEvents.isEmpty();
    }

    /**
     * 获取未提交事件的数量
     */
    public int getUncommittedEventCount() {
        return uncommittedEvents.size();
    }

    /**
     * 标记事件为已提交
     */
    public void markEventsAsCommitted() {
        clearUncommittedEvents();
    }

    /**
     * 获取聚合根类型
     */
    public String getAggregateType() {
        return this.getClass().getSimpleName();
    }
}
