package io.github.signal.core;


import io.github.signal.core.config.SignalConfig;
import io.github.signal.core.model.Envelope;
import io.github.signal.core.model.Sig;
import io.github.signal.core.model.SignalContext;
import io.github.signal.core.persistent.UnifiedSignalPersistence;
import io.github.signal.core.tractional.DeadLetterQueueManager;
import io.github.signal.core.tractional.EventTransactionManager;
import io.github.signal.spring.configuration.SignalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Signals
 */
@Service
public class Signals<S, T> implements DisposableBean {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Signals.class);

    /**
     * Signal registry
     */
    private final SignalRegistry<S, T> signalRegistry;

    /**
     * Signal pipeline
     */
    private final SignalPipeline<S, T> signalPipeline;

    /**
     * Signal processor
     */
    private final SignalProcessor<S, T> signalProcessor;

    /**
     * Signal emitter
     */
    private final SignalEmitter<S, T> signalEmitter;

    /**
     * Signal metrics
     */
    private final SignalMetrics metrics;

    /**
     * Executor service
     */
    private final ExecutorService executorService;

    /**
     * Signal protection manager
     */
    private final SignalProtectionManager protectionManager;

    /**
     * 事务管理器
     */
    private final EventTransactionManager<S, T> transactionManager;

    /**
     * 死信队列管理器
     */
    private final DeadLetterQueueManager<S, T> deadLetterQueueManager;

    /**
     * Persistence
     */
    @Autowired
    private UnifiedSignalPersistence<S, T> unifiedSignalPersistence;

    /**
     * Signal properties
     */
    @Autowired
    private SignalProperties signalProperties;

    @Autowired
    // 基础构造函数（无事务支持）
    public Signals(@Qualifier("signalExecutor") ExecutorService executorService) {
        this.metrics = new SignalMetrics();
        this.executorService = executorService;
        this.transactionManager = null;
        this.deadLetterQueueManager = null;

        this.signalRegistry = new SignalRegistry<>(metrics);
        this.signalProcessor = new SignalProcessor<>(executorService, metrics);
        this.signalPipeline = new SignalPipeline<>();
        this.protectionManager = new SignalProtectionManager();
        this.signalEmitter = new SignalEmitter<>(executorService, signalProcessor, metrics);

        log.info("信号管理器已初始化（无事务支持）");
    }

    // 支持事务的构造函数
    public Signals(@Qualifier("signalExecutor") ExecutorService executorService,
                   EventTransactionManager<S, T> transactionManager,
                   DeadLetterQueueManager<S, T> deadLetterQueueManager) {
        this.metrics = new SignalMetrics();
        this.executorService = executorService;
        this.transactionManager = transactionManager;
        this.deadLetterQueueManager = deadLetterQueueManager;

        this.signalRegistry = new SignalRegistry<>(metrics, transactionManager, deadLetterQueueManager);
        this.signalProcessor = new SignalProcessor<>(executorService, metrics);
        this.signalPipeline = new SignalPipeline<>();
        this.protectionManager = new SignalProtectionManager();
        this.signalEmitter = new SignalEmitter<>(executorService, signalProcessor, metrics);

        log.info("信号管理器已初始化（支持事务）");
    }

    /**
     * 绑定事件处理器
     */
    public long connect(String event, SignalHandler<S, T> handler) {
        autoConfigureProtection(event);
        return connect(event, handler, new SignalConfig.Builder().build());
    }

    /**
     * 绑定事件处理器（带配置）
     */
    public long connect(String event, SignalHandler<S, T> handler, SignalConfig signalConfig) {
        autoConfigureProtection(event);
        return signalRegistry.registerHandler(event, handler, signalConfig);
    }

    /**
     * 绑定事件处理器（带上下文）
     */
    public long connect(String event, SignalHandler<S, T> handler, SignalContext context) {
        autoConfigureProtection(event);
        return connect(event, handler, new SignalConfig.Builder().build(), context);
    }

    /**
     * 绑定事件处理器（带配置和上下文）
     */
    public long connect(String event, SignalHandler<S, T> handler, SignalConfig signalConfig, SignalContext context) {
        autoConfigureProtection(event);
        return signalRegistry.registerHandler(event, handler, signalConfig, context);
    }

    /**
     * 解绑事件处理器
     */
    public void disconnect(String event, long id) {
        signalRegistry.unregisterHandler(event, id);
    }

    /**
     * 解绑事件处理器（带上下文）
     */
    public void disconnect(String event, long id, SignalContext context) {
        signalRegistry.unregisterHandler(event, id, context);
    }

    /**
     * 处理事件队列
     */
    public void processEvents() {
        signalRegistry.processEvents();
    }

    /**
     * 发射信号
     */
    public void emit(String event, Envelope<S, T> envelope, Consumer<Throwable> errorHandler) {
        // 1. 检查保护机制
        if (protectionManager.isBlocked(event)) {
            log.debug("Signal [{}] blocked by protection manager", event);
            return;
        }

        // 2. 准备上下文
        SignalContext context = envelope.getContext();
        if (context == null) {
            log.warn("Failed to prepare context for signal [{}]", event);
            return;
        }

        // 3. 执行管道处理
        Envelope<S, T> processedParams = signalPipeline.processPipeline(event, envelope, context);
        if (processedParams == null) {
            log.debug("Signal [{}] blocked by pipeline", event);
            return;
        }

        // 4. 记录指标
        SignalConfig config = signalRegistry.getConfig(event);
        if (config.isRecordMetrics()) {
            metrics.recordEmit(event);
        }

        // 5. 获取处理器并发射
        List<Sig<S, T>> sigs = signalRegistry.getHandlers(event);
        if (!signalRegistry.hasHandlers(event)) {
            log.debug("No handlers found for signal [{}]", event);
            return;
        }

        // 6. 根据配置选择同步或异步发射
        if (config.isAsync()) {
            signalEmitter.emitAsync(event, processedParams, sigs, config, errorHandler, null, protectionManager);
        } else {
            signalEmitter.emitSync(event, processedParams, sigs, config, errorHandler, null, protectionManager);
        }

        // 7. 执行后处理
        signalPipeline.executePostProcessing(event, processedParams);

        // 8. 判断是否进行持久化
        if (signalProperties.getPersistent()) {
            for (Sig<S, T> sig : sigs) {
                // 进行持久化操作
                unifiedSignalPersistence.saveEventAsync(sig, config, context, metrics.getMetrics(event));
                log.info("Event Info Is Saved By Persistence");
            }
        }
    }

    /**
     * 发射信号（带回调）
     */
    public void emit(String event, Envelope<S, T> envelope, SignalCallback<S, T> callback, Consumer<Throwable> errorHandler) {
        // 1. 检查保护机制
        if (protectionManager.isBlocked(event)) {
            log.debug("Signal [{}] blocked by protection manager", event);
            return;
        }

        // 2. 准备上下文
        SignalContext context = envelope.getContext();
        if (context == null) {
            if (callback != null) {
                callback.onError(event, envelope, new RuntimeException("Failed to prepare context"));
                callback.onComplete(event, envelope);
            }
            return;
        }

        // 3. 执行管道处理
        Envelope<S, T> processedParams = signalPipeline.processPipeline(event, envelope, context);
        if (processedParams == null) {
            if (callback != null) {
                callback.onError(event, envelope, new RuntimeException("Signal blocked by pipeline"));
                callback.onComplete(event, envelope);
            }
            return;
        }

        // 4. 记录指标
        SignalConfig config = signalRegistry.getConfig(event);
        if (config.isRecordMetrics()) {
            metrics.recordEmit(event);
        }

        // 5. 获取处理器并发射
        List<Sig<S, T>> sigs = signalRegistry.getHandlers(event);
        if (!signalRegistry.hasHandlers(event)) {
            if (callback != null) {
                callback.onError(event, processedParams, new RuntimeException("No handlers for event: " + event));
                callback.onComplete(event, processedParams);
            }
            return;
        }

        // 6. 根据配置选择同步或异步发射
        if (config.isAsync()) {
            signalEmitter.emitAsync(event, processedParams, sigs, config, errorHandler, callback, protectionManager);
        } else {
            signalEmitter.emitSync(event, processedParams, sigs, config, errorHandler, callback, protectionManager);
        }

        // 7. 执行后处理
        signalPipeline.executePostProcessing(event, processedParams);

        // 8. 判断是否进行持久化
        if (signalProperties.getPersistent()) {
            for (Sig<S, T> sig : sigs) {
                // 进行持久化操作
                unifiedSignalPersistence.saveEventAsync(sig, config, context, metrics.getMetrics(event));
                log.info("Event Info Is Saved By Persistence");
            }
        }
    }

    @Override
    public void destroy() {
        log.info("Shutting down Signal framework, cleaning up resources...");

        // 1. 清理所有事件处理器
        signalRegistry.clearAll();

        // 2. 关闭死信队列管理器
        if (deadLetterQueueManager != null) {
            deadLetterQueueManager.shutdown();
        }

        // 3. 关闭线程池
        shutdown();

        log.info("Signal framework resource cleanup completed");
    }

    // 提供事务管理器的访问方法
    public EventTransactionManager<S, T> getTransactionManager() {
        return transactionManager;
    }

    public DeadLetterQueueManager<S, T> getDeadLetterQueueManager() {
        return deadLetterQueueManager;
    }

    /**
     * 关闭执行器服务
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            log.info("正在关闭信号处理线程池...");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.warn("线程池未在60秒内关闭，强制关闭...");
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                        log.error("线程池强制关闭失败");
                    }
                } else {
                    log.info("线程池已优雅关闭");
                }
            } catch (InterruptedException ie) {
                log.warn("线程池关闭被中断，强制关闭...");
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 清除指定事件
     */
    public void clear(String... events) {
        signalRegistry.clear(events);
    }

    /**
     * 获取信号统计信息
     */
    public SignalMetrics getMetrics() {
        return metrics;
    }

    /**
     * 绑定信号过滤器
     */
    public void addFilter(String event, SignalFilter<S, T> filter) {
        signalPipeline.addFilter(event, filter);
    }

    /**
     * 绑定信号转换器
     */
    public void addSignalTransformer(String event, SignalTransformer<S, T> transformer) {
        signalPipeline.addTransformer(event, transformer);
    }

    /**
     * 绑定信号拦截器
     */
    public void addSignalInterceptor(String event, SignalInterceptor<S, T> interceptor) {
        signalPipeline.addInterceptor(event, interceptor);
    }

    /**
     * 获取已注册的事件列表
     */
    public Set<String> getRegisteredEvents() {
        return signalRegistry.getRegisteredEvents();
    }

    /**
     * 根据配置自动配置保护机制
     */
    public void autoConfigureProtection(String event) {
        if (signalProperties != null && signalProperties.getProtectionEnabled()) {
            // 自动创建熔断器
            CircuitBreaker breaker = new CircuitBreaker(
                    signalProperties.getCircuitBreakerFailureThreshold(),
                    signalProperties.getCircuitBreakerOpenTimeoutMs(),
                    signalProperties.getCircuitBreakerHalfOpenTrialCount()
            );

            // 自动创建限流器
            RateLimiter limiter = new RateLimiter(
                    signalProperties.getRateLimiterMaxRequestsPerSecond()
            );

            // 注册保护机制
            protectionManager.registerCircuitBreaker(event, breaker);
            protectionManager.registerRateLimiter(event, limiter);

            log.info("自动配置保护机制完成: {} - 熔断器阈值:{}, 限流器QPS:{}",
                    event,
                    signalProperties.getCircuitBreakerFailureThreshold(),
                    signalProperties.getRateLimiterMaxRequestsPerSecond());
        }
    }
}
