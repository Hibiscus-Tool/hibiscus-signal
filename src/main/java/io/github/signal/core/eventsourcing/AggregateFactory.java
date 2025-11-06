package io.github.signal.core.eventsourcing;

/**
 * 聚合根工厂接口
 * 用于创建聚合根实例
 */
@FunctionalInterface
public interface AggregateFactory<T extends AggregateRoot> {

    /**
     * 创建聚合根实例
     */
    T createAggregate(String id);

    /**
     * 获取聚合根类型
     */
    default String getAggregateType() {
        return this.getClass().getSimpleName();
    }
}
