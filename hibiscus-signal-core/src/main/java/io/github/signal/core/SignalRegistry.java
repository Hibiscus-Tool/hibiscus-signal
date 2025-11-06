package io.github.signal.core;

import io.github.signal.core.config.SignalConfig;
import io.github.signal.core.enums.SignalPriority;
import io.github.signal.core.model.Sig;
import io.github.signal.core.model.SignalContext;
import io.github.signal.utils.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import static io.github.signal.core.enums.EventType.ADD_HANDLER;
import static io.github.signal.core.enums.EventType.REMOVE_HANDLER;

/**
 * 信号注册管理器
 * 负责信号的注册、解绑和队列管理
 */
public class SignalRegistry<S, T> {

    private static final Logger log = LoggerFactory.getLogger(SignalRegistry.class);

    /**
     * 监听器集合
     */
    private final Map<String, List<Sig<S, T>>> sigHandlers = new ConcurrentHashMap<>();

    /**
     * 信号配置
     */
    private final Map<String, SignalConfig> signalConfigs = new ConcurrentHashMap<>();

    /**
     * 监控器
     */
    private final SignalMetrics metrics;

    /**
     * 事件队列
     */
    private final EnumMap<SignalPriority, BlockingQueue<Sig<S, T>>> priorityQueues = new EnumMap<>(SignalPriority.class);

    /**
     * 事务管理器（可选，仅在 spring 模块中可用）
     */
    private final Object transactionManager;

    /**
     * 死信队列管理器（可选，仅在 spring 模块中可用）
     */
    private final Object deadLetterQueueManager;

    /**
     * 是否在循环中
     */
    private volatile boolean inLoop = false;

    public SignalRegistry(SignalMetrics metrics, Object transactionManager, Object deadLetterQueueManager) {
        this.metrics = metrics;
        this.transactionManager = transactionManager;
        this.deadLetterQueueManager = deadLetterQueueManager;
        for (SignalPriority p : SignalPriority.values()) {
            priorityQueues.put(p, new LinkedBlockingQueue<>());
        }
    }

    // 基础构造函数（无事务支持）
    public SignalRegistry(SignalMetrics metrics) {
        this.metrics = metrics;
        this.transactionManager = null;
        this.deadLetterQueueManager = null;
        for (SignalPriority p : SignalPriority.values()) {
            priorityQueues.put(p, new LinkedBlockingQueue<>());
        }
    }

    /**
     * 注册信号处理器
     */
    public long registerHandler(String event, SignalHandler<S, T> handler, SignalConfig signalConfig) {
        signalConfigs.computeIfAbsent(event, k -> signalConfig);
        long id = SnowflakeIdGenerator.nextId();

        SignalHandler<S, T> finalHandler = handler;

        // 如果启用了事务支持，尝试通过反射包装为事务感知的处理器
        if (transactionManager != null && deadLetterQueueManager != null &&
                signalConfig.getTransactionConfig() != null &&
                signalConfig.isTransactionEnabled()) {
            try {
                // 使用反射动态加载 TransactionalSignalHandler（在 spring 模块中）
                Class<?> transactionalHandlerClass = Class.forName("io.github.signal.core.tractional.TransactionalSignalHandler");
                finalHandler = (SignalHandler<S, T>) transactionalHandlerClass
                        .getConstructor(SignalHandler.class, SignalConfig.class, Object.class, Object.class)
                        .newInstance(handler, signalConfig, transactionManager, deadLetterQueueManager);
                log.debug("为事件 {} 创建事务感知的处理器", event);
            } catch (Exception e) {
                log.warn("无法创建事务感知处理器，使用原始处理器: {}", e.getMessage());
            }
        }

        Sig<S, T> signalHandler = Sig.<S, T>builder()
                .id(id)
                .evType(ADD_HANDLER)
                .signalName(event)
                .handler(finalHandler)
                .signalContext(new SignalContext())
                .priority(signalConfig.getPriority())
                .build();
        priorityQueues.get(signalConfig.getPriority()).offer(signalHandler);
        if (signalConfig.isRecordMetrics()){
            metrics.recordHandlerAdded(event);
        }
        processEvents();
        return id;
    }

    /**
     * 注册信号处理器（带上下文）
     */
    public long registerHandler(String event, SignalHandler<S, T> handler, SignalConfig signalConfig, SignalContext context) {
        signalConfigs.computeIfAbsent(event, k -> signalConfig);
        long id = SnowflakeIdGenerator.nextId();

        SignalHandler<S, T> finalHandler = handler;

        // 如果启用了事务支持，尝试通过反射包装为事务感知的处理器
        if (transactionManager != null && deadLetterQueueManager != null &&
                signalConfig.getTransactionConfig() != null &&
                signalConfig.isTransactionEnabled()) {
            try {
                // 使用反射动态加载 TransactionalSignalHandler（在 spring 模块中）
                Class<?> transactionalHandlerClass = Class.forName("io.github.signal.core.tractional.TransactionalSignalHandler");
                finalHandler = (SignalHandler<S, T>) transactionalHandlerClass
                        .getConstructor(SignalHandler.class, SignalConfig.class, Object.class, Object.class)
                        .newInstance(handler, signalConfig, transactionManager, deadLetterQueueManager);
                log.debug("为事件 {} 创建事务感知的处理器", event);
            } catch (Exception e) {
                log.warn("无法创建事务感知处理器，使用原始处理器: {}", e.getMessage());
            }
        }


        Sig<S, T> signalHandler = Sig.<S, T>builder()
                .id(id)
                .signalName(event)
                .evType(ADD_HANDLER)
                .handler(finalHandler)
                .signalContext(context)
                .priority(signalConfig.getPriority())
                .build();
        priorityQueues.get(signalConfig.getPriority()).offer(signalHandler);
        if (signalConfig.isRecordMetrics()){
            metrics.recordHandlerAdded(event);
        }
        processEvents();
        return id;
    }

    /**
     * 解绑信号处理器
     */
    public void unregisterHandler(String event, long id) {
        SignalConfig config = signalConfigs.getOrDefault(event, new SignalConfig.Builder().build());
        Sig<S, T> ev = Sig.<S, T>builder()
                .id(id)
                .signalName(event)
                .evType(REMOVE_HANDLER)
                .signalContext(new SignalContext())
                .priority(config.getPriority())
                .build();
        priorityQueues.get(config.getPriority()).offer(ev);
        metrics.recordHandlerRemoved(event);
        processEvents();
    }

    /**
     * 解绑信号处理器（带上下文）
     */
    public void unregisterHandler(String event, long id, SignalContext context) {
        SignalConfig config = signalConfigs.getOrDefault(event, new SignalConfig.Builder().build());
        Sig<S, T> ev = Sig.<S, T>builder()
                .id(id)
                .signalName(event)
                .evType(REMOVE_HANDLER)
                .signalContext(context)
                .priority(config.getPriority())
                .build();
        priorityQueues.get(config.getPriority()).offer(ev);
        metrics.recordHandlerRemoved(event);
        processEvents();
    }

    /**
     * 处理事件队列
     */
    public void processEvents() {
        if (inLoop) return;
        synchronized (this) {
            if (allQueuesEmpty()) return;
            inLoop = true;
        }
        try {
            // 按优先级顺序处理：HIGH -> MEDIUM -> LOW
            processPriorityQueue(SignalPriority.HIGH);
            processPriorityQueue(SignalPriority.MEDIUM);
            processPriorityQueue(SignalPriority.LOW);
        } finally {
            inLoop = false;
        }
    }

    /**
     * 处理优先级队列
     */
    private void processPriorityQueue(SignalPriority priority) {
        BlockingQueue<Sig<S, T>> queue = priorityQueues.get(priority);
        Sig<S, T> sigHandler;
        while ((sigHandler = queue.poll()) != null) {
            List<Sig<S, T>> sigs = sigHandlers.computeIfAbsent(sigHandler.getSignalName(), k -> new CopyOnWriteArrayList<>());
            SignalConfig config = signalConfigs.computeIfAbsent(sigHandler.getSignalName(), k -> new SignalConfig());
            switch (sigHandler.getEvType()) {
                case ADD_HANDLER:
                    if (sigs.size() < config.getMaxHandlers()) {
                        sigs.add(sigHandler);
                        log.debug("Handler registered for event: {}", sigHandler.getSignalName());
                    }
                    break;
                case REMOVE_HANDLER:
                    Sig<S, T> finalEvent = sigHandler;
                    sigs.removeIf(sh -> Objects.equals(sh.getId(), finalEvent.getId()));
                    log.debug("Handler unregistered for event: {}", sigHandler.getSignalName());
                    break;
                default:
                    log.warn("Unknown event type: {}", sigHandler.getEvType());
            }
        }
    }

    /**
     * 判断所有队列是否为空
     */
    private boolean allQueuesEmpty() {
        return priorityQueues.values().stream().allMatch(Queue::isEmpty);
    }


    /**
     * 获取信号配置
     */
    public SignalConfig getConfig(String event) {
        return signalConfigs.getOrDefault(event, new SignalConfig.Builder().build());
    }

    /**
     * 获取事件处理器列表
     */
    public List<Sig<S, T>> getHandlers(String event) {
        return sigHandlers.getOrDefault(event, Collections.emptyList());
    }

    /**
     * 检查事件是否有处理器
     */
    public boolean hasHandlers(String event) {
        List<Sig<S, T>> handlers = sigHandlers.get(event);
        return handlers != null && !handlers.isEmpty();
    }

    /**
     * 清除所有事件（用于应用关闭时清理资源）
     */
    public void clearAll() {
        int handlerCount = sigHandlers.size();
        int configCount = signalConfigs.size();

        sigHandlers.clear();
        signalConfigs.clear();

        // 清空所有优先级队列
        for (SignalPriority priority : SignalPriority.values()) {
            priorityQueues.get(priority).clear();
        }

        log.info("All signal handlers cleared: {} handlers, {} configs", handlerCount, configCount);
    }

    /**
     * 清除指定事件
     */
    public void clear(String... events) {
        for (String event : events) {
            sigHandlers.remove(event);
            signalConfigs.remove(event);
            log.debug("已清除事件: {}", event);
        }
    }

    /**
     * 获取已注册的事件列表
     */
    public Set<String> getRegisteredEvents() {
        return Collections.unmodifiableSet(sigHandlers.keySet());
    }
}
