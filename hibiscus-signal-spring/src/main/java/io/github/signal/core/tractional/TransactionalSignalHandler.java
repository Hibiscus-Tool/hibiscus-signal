package io.github.signal.core.tractional;

import io.github.signal.core.SignalHandler;
import io.github.signal.core.config.SignalConfig;
import io.github.signal.core.config.TransactionConfig;
import io.github.signal.core.model.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 事务感知的信号处理器包装器
 * 为每个信号处理器提供事务支持
 */
public class TransactionalSignalHandler<S, T> implements SignalHandler<S, T> {

    private static final Logger log = LoggerFactory.getLogger(TransactionalSignalHandler.class);

    private final SignalHandler<S, T> delegate;
    private final SignalConfig config;
    private final EventTransactionManager<S, T> transactionManager;
    private final DeadLetterQueueManager<S, T> deadLetterQueueManager;

    public TransactionalSignalHandler(SignalHandler<S, T> delegate, SignalConfig config,
                                      Object transactionManager,
                                      Object deadLetterQueueManager) {
        this.delegate = delegate;
        this.config = config;
        this.transactionManager = (EventTransactionManager<S, T>) transactionManager;
        this.deadLetterQueueManager = (DeadLetterQueueManager<S, T>) deadLetterQueueManager;
    }

    @Override
    public void handle(Envelope<S, T> envelope) {
        Object txConfigObj = config.getTransactionConfig();
        if (txConfigObj == null || !(txConfigObj instanceof TransactionConfig)) {
            delegate.handle(envelope);
            return;
        }
        
        TransactionConfig txConfig = (TransactionConfig) txConfigObj;
        if (!txConfig.isEnableTransaction()) {
            // 如果未启用事务，直接执行
            delegate.handle(envelope);
            return;
        }

        try {
            // 在事务中执行
            transactionManager.executeInTransaction(
                    envelope.getEventType(),
                    this::executeHandler,
                    config,
                    envelope.getContext(),
                    envelope
            );

        } catch (Exception e) {
            log.error("事务执行失败: {} - {}", envelope.getEventType(), e.getMessage(), e);

            // 如果启用了死信队列，发送到死信队列
            if (txConfig.isEnableDeadLetter() && deadLetterQueueManager != null) {
                handleDeadLetter(envelope, e, txConfig);
            }
            log.error("事务执行失败: {}", envelope.getEventType(), e);
        }
    }

    private Object executeHandler(Object... params) {
        if (params.length > 0 && params[0] instanceof Envelope) {
            @SuppressWarnings("unchecked")
            Envelope<S, T> envelope = (Envelope<S, T>) params[0];
            delegate.handle(envelope);
        }
        return "SUCCESS";
    }

    private void handleDeadLetter(Envelope<S, T> envelope, Exception error, TransactionConfig txConfig) {
        try {
            DeadLetterEvent<S, T> deadLetterEvent = new DeadLetterEvent<>(
                    envelope.getEventType(),
                    getHandlerName(),
                    envelope.getContext(),
                    envelope.getPayload(),
                    error,
                    txConfig.getMaxRetries()
            );

            deadLetterEvent.setId(generateEventId());
            deadLetterQueueManager.addDeadLetterEvent(deadLetterEvent);

            log.info("死信事件已创建: {}", deadLetterEvent.getEventSummary());

        } catch (Exception e) {
            log.error("创建死信事件失败: {}", e.getMessage(), e);
        }
    }

    private String getHandlerName() {
        return delegate.getClass().getSimpleName();
    }

    private String generateEventId() {
        return "DL_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }
}
