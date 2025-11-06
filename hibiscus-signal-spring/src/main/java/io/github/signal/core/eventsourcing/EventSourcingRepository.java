package io.github.signal.core.eventsourcing;

import java.util.concurrent.CompletableFuture;

/**
 * 事件溯源仓库接口
 * 提供聚合根的存储和重建功能
 */
public interface EventSourcingRepository<T extends AggregateRoot> {

    /**
     * 保存聚合根
     */
    CompletableFuture<Void> save(T aggregate);

    /**
     * 根据ID获取聚合根
     */
    CompletableFuture<T> findById(String id);

    /**
     * 根据ID和版本获取聚合根
     */
    CompletableFuture<T> findByIdAndVersion(String id, long version);

    /**
     * 检查聚合根是否存在
     */
    CompletableFuture<Boolean> exists(String id);

    /**
     * 删除聚合根
     */
    CompletableFuture<Void> delete(String id);

    /**
     * 获取聚合根的当前版本
     */
    CompletableFuture<Long> getCurrentVersion(String id);

    /**
     * 获取聚合根的所有事件
     */
    CompletableFuture<java.util.List<Event>> getEvents(String id);

    /**
     * 获取聚合根从指定版本开始的事件
     */
    CompletableFuture<java.util.List<Event>> getEvents(String id, long fromVersion);
}
