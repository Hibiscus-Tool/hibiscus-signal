package io.github.signal.core.eventsourcing.example;

import io.github.signal.core.eventsourcing.AggregateFactory;

/**
 * 用户聚合根工厂
 */
public class UserAggregateFactory implements AggregateFactory<UserAggregate> {

    @Override
    public UserAggregate createAggregate(String id) {
        return new UserAggregate(id);
    }

    @Override
    public String getAggregateType() {
        return "UserAggregate";
    }
}
