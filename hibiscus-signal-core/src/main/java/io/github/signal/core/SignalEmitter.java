package io.github.signal.core;

import io.github.signal.core.config.SignalConfig;
import io.github.signal.core.model.Envelope;
import io.github.signal.core.model.Sig;
import io.github.signal.core.model.SignalContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * 信号发射器
 * 负责信号的发射逻辑，包括同步和异步发射
 */
public class SignalEmitter<S, T> {

    /**
     * 日志记录器
     */
    private static final Logger log = LoggerFactory.getLogger(SignalEmitter.class);

    /**
     * 线程池
     */
    private final ExecutorService executorService;

    /**
     * 信号处理器
     */
    private final SignalProcessor<S, T>  signalProcessor;

    /**
     * 信号度量
     */
    private final SignalMetrics metrics;

    public SignalEmitter(ExecutorService executorService, SignalProcessor<S, T>  signalProcessor, SignalMetrics metrics) {
        this.executorService = executorService;
        this.signalProcessor = signalProcessor;
        this.metrics = metrics;
    }

    /**
     * 同步发射信号
     */
    public void emitSync(String event, Envelope<S, T> envelope, List<Sig<S, T>> sigs,
                         SignalConfig config, Consumer<Throwable> errorHandler,
                         SignalCallback<S, T> callback,  SignalProtectionManager protectionManager) {
        for (Sig<S, T> sig : sigs) {
            long startTime = System.currentTimeMillis();
            SignalContext context = new SignalContext();
            try {
                signalProcessor.executeWithTracingAndProtection(event, sig, envelope, config, context, protectionManager, metrics);
                if (config.isRecordMetrics()) {
                    // 记录处理时间
                    long processingTime = System.currentTimeMillis() - startTime;
                    metrics.recordProcessingTime(event, processingTime);
                    log.debug("Signal [{}] processed in {}ms", event, processingTime);
                }
                if (callback != null) {
                    callback.onSuccess(event, envelope);
                }
            } catch (Exception e) {
                handleError(event, config, errorHandler, e);
                if (callback != null) {
                    callback.onError(event, envelope, e);
                }
            } finally {
                if (callback != null) {
                    callback.onComplete(event, envelope);
                }
            }
        }
    }

    /**
     * 异步发射信号
     */
    public void emitAsync(String event, Envelope<S, T> envelope, List<Sig<S, T>> sigs,
                          SignalConfig config, Consumer<Throwable> errorHandler,
                          SignalCallback<S, T> callback, SignalProtectionManager protectionManager) {
        for (Sig<S, T> sig : sigs) {
            CompletableFuture.runAsync(() -> {
                long startTime = System.currentTimeMillis();
                SignalContext context = new SignalContext();
                try {
                    signalProcessor.executeWithTracingAndProtection(event, sig, envelope, config, context,
                            protectionManager, metrics);
                    signalProcessor.executeWithTimeout(sig, envelope, 1000L);
                    if (config.isRecordMetrics()) {
                        long processingTime = System.currentTimeMillis() - startTime;
                        log.debug("Signal [{}] processed asynchronously in {}ms", event, processingTime);
                    }
                    if (callback != null) {
                        callback.onSuccess(event, envelope);
                    }
                } catch (Exception e) {
                    handleError(event, config, errorHandler, e);
                    if (callback != null) {
                        callback.onError(event, envelope, e);
                    }
                } finally {
                    if (callback != null) {
                        callback.onComplete(event, envelope);
                    }
                }
            }, executorService);
        }
    }

    /**
     * 处理错误
     */
    private void handleError(String event, SignalConfig config,
                             Consumer<Throwable> errorHandler, Exception e) {
        if (config.isRecordMetrics()) {
            log.error("Signal [{}] processing error recorded", event);
        }
        log.error("Signal [{}] handler error: {}", event, e.getMessage(), e);
        if (errorHandler != null) {
            errorHandler.accept(e);
        }
    }
}
