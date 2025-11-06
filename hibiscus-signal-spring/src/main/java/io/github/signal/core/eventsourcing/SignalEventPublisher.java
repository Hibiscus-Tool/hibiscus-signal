package io.github.signal.core.eventsourcing;

import io.github.signal.core.Signals;
import io.github.signal.core.model.Envelope;
import io.github.signal.core.model.SignalContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 基于Signal框架的事件发布者实现
 * 将事件溯源事件发布到Signal框架中
 */
@Component
public class SignalEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SignalEventPublisher.class);

    @Autowired
    private Signals<Object, Object> signals;

    @Override
    public void publish(Event event) {
        try {
            // 创建Signal上下文
            SignalContext context = new SignalContext();
            context.initTrace("EventSourcing." + event.getEventName());
            
            // 添加事件元数据到上下文
            if (event.getMetadata() != null) {
                context.setAttribute("userId", event.getMetadata().getUserId());
                context.setAttribute("sessionId", event.getMetadata().getSessionId());
                context.setAttribute("ipAddress", event.getMetadata().getIpAddress());
                context.setAttribute("requestId", event.getMetadata().getRequestId());
            }
            
            // 创建Signal信封
            Envelope<Object, Object> envelope = Envelope.Builder.builder()
                    .eventType("EventSourcing." + event.getEventName())
                    .sender(this)
                    .payload(event)
                    .context(context)
                    .build();

            // 发布到Signal框架
            signals.emit("EventSourcing." + event.getEventName(), envelope, 
                error -> log.error("事件发布失败: {}", event.getEventName(), error));

            log.debug("事件已发布到Signal框架: {} - {}", event.getEventName(), event.getEventId());

        } catch (Exception e) {
            log.error("发布事件失败: {}", event.getEventName(), e);
        }
    }

    @Override
    public void publishAll(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        for (Event event : events) {
            publish(event);
        }

        log.info("批量发布完成，共发布 {} 个事件", events.size());
    }

    @Override
    public void publishAsync(Event event) {
        CompletableFuture.runAsync(() -> publish(event));
    }

    @Override
    public void publishAllAsync(List<Event> events) {
        CompletableFuture.runAsync(() -> publishAll(events));
    }
}
