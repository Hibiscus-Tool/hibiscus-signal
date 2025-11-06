package io.github.signal.core.eventsourcing;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 事件溯源配置类
 * 提供事件溯源相关的Bean配置
 */
@Configuration
@Import({EventSourcingManager.class, SignalEventPublisher.class})
@ConditionalOnProperty(name = "hibiscus.event-sourcing-enabled", havingValue = "true", matchIfMissing = false)
public class EventSourcingConfiguration {

    /**
     * 事件溯源管理器
     */
    @Bean
    @ConditionalOnProperty(name = "hibiscus.event-sourcing-enabled", havingValue = "true")
    public EventSourcingManager eventSourcingManager() {
        return new EventSourcingManager();
    }

    /**
     * 事件发布者
     */
    @Bean
    @ConditionalOnProperty(name = "hibiscus.event-sourcing-enabled", havingValue = "true")
    public EventPublisher eventPublisher() {
        return new SignalEventPublisher();
    }

    /**
     * 事件存储
     */
    @Bean
    @ConditionalOnProperty(name = "hibiscus.event-sourcing-enabled", havingValue = "true")
    @ConditionalOnClass(name = "javax.sql.DataSource")
    public EventStore eventStore() {
        return new DatabaseEventStore();
    }
}
