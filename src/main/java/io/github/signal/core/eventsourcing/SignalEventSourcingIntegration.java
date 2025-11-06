package io.github.signal.core.eventsourcing;

import io.github.signal.core.Signals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Signal框架与事件溯源的集成
 * 自动注册聚合根工厂和事件订阅者
 */
@Component
public class SignalEventSourcingIntegration {

    private static final Logger log = LoggerFactory.getLogger(SignalEventSourcingIntegration.class);

    @Autowired
    private EventSourcingManager eventSourcingManager;

    @Autowired
    private Signals<Object, Object> signals;

    @PostConstruct
    public void init() {
        log.info("初始化Signal框架与事件溯源的集成...");
        
        // 注册聚合根工厂
        registerAggregateFactories();
        
        // 注册事件订阅者
        registerEventSubscribers();
        
        log.info("Signal框架与事件溯源的集成初始化完成");
    }

    /**
     * 注册聚合根工厂
     */
    private void registerAggregateFactories() {
        // 这里可以自动扫描并注册所有聚合根工厂
        // 或者手动注册特定的工厂
        
        log.info("聚合根工厂注册完成");
    }

    /**
     * 注册事件订阅者
     */
    private void registerEventSubscribers() {
        // 注册事件溯源事件订阅者
        EventSubscriber eventSourcingSubscriber = new EventSourcingSubscriber();
        eventSourcingManager.subscribe(eventSourcingSubscriber);
        
        log.info("事件订阅者注册完成");
    }

    /**
     * 事件溯源事件订阅者
     * 处理所有事件溯源事件
     */
    private static class EventSourcingSubscriber implements EventSubscriber {

        @Override
        public void handleEvent(Event event) {
            log.info("处理事件溯源事件: {} - {}", event.getEventType(), event.getEventId());
            
            // 这里可以添加事件处理逻辑
            // 例如：发送通知、更新缓存、记录审计日志等
        }

        @Override
        public String getSubscriberName() {
            return "EventSourcingSubscriber";
        }

        @Override
        public String getEventType() {
            return null; // 订阅所有事件
        }
    }

    /**
     * 应用启动完成后执行
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("应用启动完成，事件溯源系统已就绪");
        
        // 可以在这里执行一些初始化操作
        // 例如：重建聚合根、检查事件一致性等
    }
}
