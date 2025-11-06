package io.github.signal.core;

import io.github.signal.core.config.SignalConfig;
import io.github.signal.core.model.Envelope;
import io.github.signal.core.model.Sig;
import io.github.signal.core.model.SignalContext;
import io.github.signal.exception.SignalProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 信号处理器
 * 负责信号的具体处理逻辑，包括重试、超时、追踪等功能
 */
public class SignalProcessor<S, T> {

    /**
     * 日志记录器
     */
    private static final Logger log = LoggerFactory.getLogger(SignalProcessor.class);

    /**
     * 线程池
     */
    private final ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * 线程池
     */
    private final ExecutorService executorService;

    /**
     * 信号处理指标
     */
    private final SignalMetrics metrics;

    public SignalProcessor(ExecutorService executorService, SignalMetrics metrics) {
        this.executorService = executorService;
        this.metrics = metrics;
    }

    /**
     * 执行信号处理程序
     */
    public void executeHandler(Sig<S, T> sig, Envelope<S, T> envelope) {
        sig.getHandler().handle(envelope);
        log.info("Event: {}, Handle Successful：{}",sig.getSignalName(), envelope);
    }


    /**
     * 执行带追踪和熔断器状态更新的信号处理
     */
    public void executeWithTracingAndProtection(String event, Sig<S, T> sig, Envelope<S, T> envelope,
                                                SignalConfig config, SignalContext context,
                                                SignalProtectionManager protectionManager,
                                                SignalMetrics metrics) throws Exception {
        String spanId = UUID.randomUUID().toString();
        String parentSpanId = context.getParentSpanId() != null ? context.getParentSpanId() : context.getEventId();

        SignalContext.Span span = new SignalContext.Span();
        span.setSpanId(spanId);
        span.setParentSpanId(parentSpanId);
        String op = sig.getSignalName() != null ? sig.getSignalName() : "Handler: Unknown";
        span.setOperation(op);
        span.setStartTime(System.currentTimeMillis());
        context.setParentSpanId(spanId);

        try {
            executeWithRetry(sig, envelope, config);
            // 处理成功，更新熔断器状态
            if (protectionManager != null && metrics != null) {
                protectionManager.update(event, metrics);
            }
        } catch (Exception e) {
            // 处理失败，记录错误指标（熔断器状态会在Signals.emit中通过metrics更新）
            log.error("Signal handler execution failed: {} - {}", event, e.getMessage(), e);
            throw e;
        } finally {
            span.setEndTime(System.currentTimeMillis());
            context.addSpan(span);
        }
    }

    /**
     * 执行超时处理
     */
    public void executeWithTimeout(Sig<S, T> sig, Envelope<S, T> envelope, long timeoutMs) throws Exception {
        // 用于把 future 引用传给 wrapper 的取消器
        final AtomicReference<Future<?>> futureRef = new AtomicReference<>();

        Runnable task = () -> {
            // 在任务真正开始时安排一个取消器（相当于从“开始执行”计时）
            ScheduledFuture<?> canceller = timeoutScheduler.schedule(() -> {
                Future<?> f = futureRef.get();
                if (f != null && !f.isDone()) {
                    f.cancel(true); // 尝试中断
                }
            }, timeoutMs, TimeUnit.MILLISECONDS);

            try {
                // 真正执行 handler（注意：此处若 handler 抛异常会在 submit 后通过 Future.get 抛出 ExecutionException）
                executeHandler(sig, envelope);
            } finally {
                // 任务完成 / 抛出时取消取消器，避免不必要的取消发生
                canceller.cancel(false);
            }
        };

        Future<?> future = executorService.submit(task);
        futureRef.set(future);

        try {
            // 等待任务完成（不设置超时时间，因为超时由 canceller 在任务开始后触发）
            future.get();
        } catch (CancellationException ce) {
            metrics.recordError(sig.getSignalName());
            // 任务被 canceller 取消（超时）
            throw new SignalProcessingException("Signal handler execution timed out", 1001);
        } catch (ExecutionException ee) {
            // handler 内部真实异常，抛出其 cause
            Throwable cause = ee.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            } else {
                throw new SignalProcessingException("Unexpected signal handler error", 1003);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new SignalProcessingException("Signal handler interrupted", 1002);
        }
    }

    /**
     * 执行信号处理，包含重试逻辑
     */
    public void executeWithRetry(Sig<S, T> sig, Envelope<S, T> envelope,
                                 SignalConfig config) throws Exception {
        int retries = 0;
        Exception lastException = null;
        while (retries <= config.getMaxRetries()) {
            try {
                if (config.getTimeoutMs() > 0) {
                    executeWithTimeout(sig, envelope, config.getTimeoutMs());
                } else {
                    executeHandler(sig, envelope);
                }
                if (config.isRecordMetrics()){
                    metrics.recordProcessed(sig.getSignalName());
                }
                return;
            } catch (Exception e) {
                lastException = e;
                retries++;
                if (retries <= config.getMaxRetries()) {
                    try {
                        Thread.sleep(config.getRetryDelayMs());
                    } catch (InterruptedException ie) {
                        // 恢复中断状态并退出重试循环
                        Thread.currentThread().interrupt();
                        throw new SignalProcessingException("Signal handler execution interrupted", 1002);
                    }
                }
            }
        }
        if (lastException != null) {
            throw lastException;
        }
    }

    /**
     * 执行带追踪的信号处理
     */
    public void executeWithTracing(Sig<S, T> sig,  Envelope<S, T> envelope,
                                   SignalConfig config, SignalContext context) throws Exception {
        String spanId = UUID.randomUUID().toString();
        String parentSpanId = context.getParentSpanId() != null ? context.getParentSpanId() : context.getEventId();

        SignalContext.Span span = new SignalContext.Span();
        span.setSpanId(spanId);
        span.setParentSpanId(parentSpanId);
        String op = sig.getSignalName() != null ? sig.getSignalName() : "Handler: Unknown";
        span.setOperation(op);
        span.setStartTime(System.currentTimeMillis());

        context.setParentSpanId(spanId);

        try {
            executeWithRetry(sig, envelope, config);
        } finally {
            span.setEndTime(System.currentTimeMillis());
            context.addSpan(span);
        }
    }
}
