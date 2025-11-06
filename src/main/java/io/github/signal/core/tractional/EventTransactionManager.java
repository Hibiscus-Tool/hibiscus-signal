package io.github.signal.core.tractional;


import io.github.signal.core.SignalHandler;
import io.github.signal.core.config.SignalConfig;
import io.github.signal.core.model.Envelope;
import io.github.signal.core.model.SignalContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 事件事务管理器
 * 解决事件驱动架构中的事务隔离和事件重发问题
 */
public class EventTransactionManager<S, T> {

    private static final Logger log = LoggerFactory.getLogger(EventTransactionManager.class);

    private final PlatformTransactionManager transactionManager;
    private final ConcurrentHashMap<String, EventTransactionInfo> eventTransactions = new ConcurrentHashMap<>();
    private final AtomicLong transactionCounter = new AtomicLong(0);
    private DeadLetterQueueManager<S, T> deadLetterQueueManager;

    public EventTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * 在独立事务中执行事件处理
     *
     * @param eventName 事件名称
     * @param handler   事件处理器
     * @param config    信号配置
     * @param context   信号上下文
     * @return 执行结果
     */
    public Object executeInTransaction(String eventName, SignalHandler<S, T> handler,
                                       SignalConfig config, SignalContext context, Envelope<S, T> envelope) {

        String transactionId = generateTransactionId(eventName);
        EventTransactionInfo transactionInfo = new EventTransactionInfo(transactionId, eventName, context);
        eventTransactions.put(transactionId, transactionInfo);

        TransactionStatus status = null;
        Object result = null;

        try {
            // 创建新事务
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
            def.setTimeout((int) (config.getTimeoutMs() / 1000)); // 转换为秒

            status = transactionManager.getTransaction(def);
            transactionInfo.setTransactionStatus(status);

            log.info("开始执行事件事务: {} - {}", eventName, transactionId);

            // 执行事件处理
            handler.handle(Envelope.Builder.<S, T>builder()
                    .payload(envelope.getPayload())
                    .context(context)
                    .sender(envelope.getSender())
                    .build());
            result = "SUCCESS"; // 返回成功标识

            // 提交事务
            transactionManager.commit(status);
            transactionInfo.setStatus(EventTransactionStatus.COMMITTED);

            log.info("事件事务执行成功: {} - {}", eventName, transactionId);

        } catch (Exception e) {
            // 回滚事务
            if (status != null && !status.isCompleted()) {
                transactionManager.rollback(status);
            }
            transactionInfo.setStatus(EventTransactionStatus.ROLLBACK);
            transactionInfo.setError(e);

            log.error("事件事务执行失败: {} - {}, 错误: {}", eventName, transactionId, e.getMessage(), e);

            // 根据配置决定是否重试
            if (config.getMaxRetries() > 0) {
                handleRetry(eventName, handler, config, context, envelope, e);
            }

            throw new RuntimeException("事件处理失败: " + eventName, e);

        } finally {
            // 清理事务信息
            eventTransactions.remove(transactionId);
        }

        return result;
    }

    /**
     * 处理事件重试
     */
    private void handleRetry(String eventName, SignalHandler<S, T> handler,
                             SignalConfig config, SignalContext context, Envelope<S, T> envelope, Exception error) {
        try {
            log.info("开始重试事件: {} - 错误: {}", eventName, error.getMessage());

            // 创建重试任务
            RetryTask retryTask = new RetryTask(eventName, handler, config, context, envelope, error);

            // 添加到重试队列
            if (retryQueue != null) {
                retryQueue.addRetryTask(retryTask);
                log.info("重试任务已添加到队列: {}", eventName);
            } else {
                log.warn("重试队列未初始化，无法重试事件: {}", eventName);
                // 直接处理死信事件
                handleDeadLetter(eventName, context, envelope, error);
            }

        } catch (Exception e) {
            log.error("处理重试失败: {} - 错误: {}", eventName, e.getMessage(), e);
            // 重试处理失败，直接处理死信事件
            handleDeadLetter(eventName, context, envelope, error);
        }
    }

    /**
     * 重试任务
     */
    private static class RetryTask<S, T> {
        private final String eventName;
        private final SignalHandler<S, T> handler;
        private final SignalConfig config;
        private final SignalContext context;
        private final T params;
        private final Exception originalError;
        private final long createTime;
        private int retryCount = 0;

        public RetryTask(String eventName, SignalHandler<S, T> handler, SignalConfig config,
                         SignalContext context, T params, Exception originalError) {
            this.eventName = eventName;
            this.handler = handler;
            this.config = config;
            this.context = context;
            this.params = params;
            this.originalError = originalError;
            this.createTime = System.currentTimeMillis();
        }

        public boolean canRetry() {
            return retryCount < config.getMaxRetries();
        }

        public void incrementRetryCount() {
            retryCount++;
        }

        public long getAge() {
            return System.currentTimeMillis() - createTime;
        }

        // Getters
        public String getEventName() {
            return eventName;
        }

        public SignalHandler<S, T> getHandler() {
            return handler;
        }

        public SignalConfig getConfig() {
            return config;
        }

        public SignalContext getContext() {
            return context;
        }

        public T getParams() {
            return params;
        }

        public Exception getOriginalError() {
            return originalError;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public long getCreateTime() {
            return createTime;
        }
    }

    /**
     * 重试队列管理器
     */
    private static class RetryQueue {
        private final java.util.concurrent.BlockingQueue<RetryTask> queue = new java.util.concurrent.LinkedBlockingQueue<>();
        private final java.util.concurrent.ExecutorService executor;
        private volatile boolean running = true;

        public RetryQueue() {
            this.executor = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "EventRetryWorker");
                t.setDaemon(true);
                return t;
            });
            startRetryWorker();
        }

        public void addRetryTask(RetryTask task) {
            try {
                queue.offer(task, 5, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("添加重试任务被中断: {}", task.getEventName());
            }
        }

        private void startRetryWorker() {
            executor.submit(() -> {
                while (running && !Thread.currentThread().isInterrupted()) {
                    try {
                        RetryTask task = queue.poll(1, java.util.concurrent.TimeUnit.SECONDS);
                        if (task != null) {
                            processRetryTask(task);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        log.error("重试任务处理异常: {}", e.getMessage(), e);
                    }
                }
            });
        }

        private void processRetryTask(RetryTask task) {
            try {
                if (!task.canRetry()) {
                    log.warn("重试次数已达上限，发送到死信队列: {} (重试{}次)",
                            task.getEventName(), task.getRetryCount());
                    // 这里应该调用死信队列处理
                    return;
                }

                // 计算重试延迟
                long delay = calculateRetryDelay(task);
                if (delay > 0) {
                    Thread.sleep(delay);
                }

                // 执行重试
                log.info("执行重试: {} (第{}次重试)", task.getEventName(), task.getRetryCount() + 1);

                // 这里应该调用实际的事件处理逻辑
                // 暂时只是记录日志
                task.incrementRetryCount();

            } catch (Exception e) {
                log.error("重试任务处理失败: {} - 错误: {}", task.getEventName(), e.getMessage(), e);
            }
        }

        private long calculateRetryDelay(RetryTask task) {
            // 指数退避算法
            long baseDelay = task.getConfig().getRetryDelayMs();
            long delay = baseDelay * (1L << (task.getRetryCount() - 1));

            // 添加随机抖动，避免雪崩
            long jitter = (long) (Math.random() * baseDelay * 0.1);
            delay += jitter;

            // 限制最大延迟
            long maxDelay = Math.min(delay, 60000); // 最大1分钟

            return maxDelay;
        }

        public void shutdown() {
            running = false;
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // 重试队列实例
    private final RetryQueue retryQueue = new RetryQueue();

    /**
     * 处理死信事件
     */
    private void handleDeadLetter(String eventName, SignalContext context, Envelope<S, T> envelope, Exception error) {
        try {
            // 创建死信事件 - 使用默认重试次数，因为这里没有config参数
            DeadLetterEvent<S, T> deadLetterEvent = new DeadLetterEvent<S, T>(
                    eventName,
                    getHandlerName(context),
                    context,
                    envelope.getPayload(),
                    error,
                    3 // 默认重试3次
            );

            // 生成唯一ID
            deadLetterEvent.setId(generateTransactionId(eventName));

            // 添加到死信队列管理器
            if (deadLetterQueueManager != null) {
                deadLetterQueueManager.addDeadLetterEvent(deadLetterEvent);
                log.info("死信事件已添加到队列: {}", deadLetterEvent.getEventSummary());
            } else {
                log.warn("死信队列管理器未初始化，无法处理死信事件: {}", eventName);
            }

        } catch (Exception e) {
            log.error("处理死信事件时发生异常: {} - 原始错误: {}", e.getMessage(), error.getMessage(), e);
        }
    }

    /**
     * 获取处理器名称
     */
    private String getHandlerName(SignalContext context) {
        if (context != null) {
            // 从上下文中获取处理器信息，如果没有则使用默认值
            Object handlerInfo = context.getAttribute("handler");
            if (handlerInfo != null) {
                return handlerInfo.getClass().getSimpleName();
            }
        }
        return "UnknownHandler";
    }

    /**
     * 生成事务ID
     */
    private String generateTransactionId(String eventName) {
        return eventName + "_" + System.currentTimeMillis() + "_" + transactionCounter.incrementAndGet();
    }

    /**
     * 获取事务信息
     */
    public EventTransactionInfo getTransactionInfo(String transactionId) {
        return eventTransactions.get(transactionId);
    }

    /**
     * 获取所有活跃事务
     */
    public ConcurrentHashMap<String, EventTransactionInfo> getActiveTransactions() {
        return new ConcurrentHashMap<>(eventTransactions);
    }

    /**
     * 事件事务信息
     */
    public static class EventTransactionInfo {
        private final String transactionId;
        private final String eventName;
        private final SignalContext context;
        private TransactionStatus transactionStatus;
        private EventTransactionStatus status;
        private Exception error;
        private final long startTime;

        public EventTransactionInfo(String transactionId, String eventName, SignalContext context) {
            this.transactionId = transactionId;
            this.eventName = eventName;
            this.context = context;
            this.startTime = System.currentTimeMillis();
            this.status = EventTransactionStatus.RUNNING;
        }

        // Getters and Setters
        public String getTransactionId() {
            return transactionId;
        }

        public String getEventName() {
            return eventName;
        }

        public SignalContext getContext() {
            return context;
        }

        public TransactionStatus getTransactionStatus() {
            return transactionStatus;
        }

        public void setTransactionStatus(TransactionStatus transactionStatus) {
            this.transactionStatus = transactionStatus;
        }

        public EventTransactionStatus getStatus() {
            return status;
        }

        public void setStatus(EventTransactionStatus status) {
            this.status = status;
        }

        public Exception getError() {
            return error;
        }

        public void setError(Exception error) {
            this.error = error;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getDuration() {
            return System.currentTimeMillis() - startTime;
        }
    }

    /**
     * 事件事务状态
     */
    public enum EventTransactionStatus {
        RUNNING,    // 运行中
        COMMITTED,  // 已提交
        ROLLBACK    // 已回滚
    }
}
