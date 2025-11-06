package io.github.signal.core;


import io.github.signal.core.model.Envelope;
import io.github.signal.core.model.SignalContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 信号管道处理器
 * 负责拦截器、过滤器、转换器的链式处理
 */
public class SignalPipeline<S, T> {

    /**
     * 日志记录器
     */
    private static final Logger log = LoggerFactory.getLogger(SignalPipeline.class);

    /**
     * 拦截器
     */
    private final Map<String, List<SignalInterceptor<S, T>>> signalInterceptors = new ConcurrentHashMap<>();

    /**
     * 过滤器
     */
    private final Map<String, List<SignalFilter<S, T>>> signalFilters = new ConcurrentHashMap<>();

    /**
     * 转换器
     */
    private final Map<String, List<SignalTransformer<S, T>>> signalTransformers = new ConcurrentHashMap<>();

    /**
     * 执行信号管道处理
     */
    public Envelope<S, T> processPipeline(String event, Envelope<S, T> envelope, SignalContext context) {
        // 1. 执行拦截器
        if (!executeInterceptors(event, envelope, context)) {
            return null; // 被拦截器阻止
        }
        // 2. 执行过滤器
        if (!executeFilters(event, envelope, context)) {
            return null; // 被过滤器阻止
        }
        // 3. 执行转换器
        return executeTransformers(event, envelope, context);
    }


    /**
     * 执行所有拦截器的后置处理
     */
    public void executePostProcessing(String event, Envelope<S, T> envelope) {
        List<SignalInterceptor<S, T>> interceptors = signalInterceptors.get(event);
        if (interceptors == null || interceptors.isEmpty()) {
            return;
        }

        for (SignalInterceptor<S, T> interceptor : interceptors) {
            try {
                interceptor.afterHandle(event, envelope, null);  // 假设没有异常，传递 null
            } catch (Exception ex) {
                log.error("Error during afterHandle for signal [{}] by interceptor [{}]: {}", event, interceptor.getClass().getSimpleName(), ex.getMessage());
            }
        }
    }

    /**
     * 执行拦截器链
     */
    private boolean executeInterceptors(String event, Envelope<S, T> envelope, SignalContext context) {
        List<SignalInterceptor<S, T>> interceptors = signalInterceptors.get(event);
        if (interceptors == null || interceptors.isEmpty()) {
            return true;
        }

        for (SignalInterceptor<S, T> interceptor : interceptors) {
            String spanId = UUID.randomUUID().toString();
            String parentSpanId = context.getParentSpanId() != null ? context.getParentSpanId() : context.getEventId();

            SignalContext.Span span = new SignalContext.Span();
            span.setSpanId(spanId);
            span.setParentSpanId(parentSpanId);
            span.setOperation("Interceptor: " + interceptor.getClass().getSimpleName());
            span.setStartTime(System.currentTimeMillis());

            context.setParentSpanId(spanId);
            boolean allowed = interceptor.beforeHandle(event, envelope);
            span.setEndTime(System.currentTimeMillis());
            context.addSpan(span);

            if (!allowed) {
                log.debug("Signal [{}] blocked by interceptor: {}", event, interceptor.getClass().getSimpleName());
                return false;
            }
        }

        return true;
    }

    /**
     * 执行过滤器链
     */
    private boolean executeFilters(String event, Envelope<S, T> envelope, SignalContext context) {
        List<SignalFilter<S, T>> filters = getSortedFilters(event);
        if (filters.isEmpty()) {
            return true;
        }

        for (SignalFilter<S, T> filter : filters) {
            String spanId = UUID.randomUUID().toString();
            String parentSpanId = context.getParentSpanId() != null ? context.getParentSpanId() : context.getEventId();

            SignalContext.Span span = new SignalContext.Span();
            span.setSpanId(spanId);
            span.setParentSpanId(parentSpanId);
            span.setOperation("Filter: " + filter.getClass().getSimpleName());
            span.setStartTime(System.currentTimeMillis());

            context.setParentSpanId(spanId);
            boolean pass = filter.filter(event, envelope);
            span.setEndTime(System.currentTimeMillis());
            context.addSpan(span);

            if (!pass) {
                log.debug("Signal [{}] filtered out by: {}", event, filter.getClass().getSimpleName());
                return false;
            }
        }

        return true;
    }

    /**
     * 执行转换器链
     */
    private Envelope<S, T> executeTransformers(String event, Envelope<S, T> envelope, SignalContext context) {
        List<SignalTransformer<S, T>> transformers = signalTransformers.get(event);
        if (transformers == null || transformers.isEmpty()) {
            return envelope;
        }

        Envelope<S, T> transformedParams = null;
        for (SignalTransformer<S, T> transformer : transformers) {
            String spanId = UUID.randomUUID().toString();
            String parentSpanId = context.getParentSpanId() != null ? context.getParentSpanId() : context.getEventId();

            SignalContext.Span span = new SignalContext.Span();
            span.setSpanId(spanId);
            span.setParentSpanId(parentSpanId);
            span.setOperation("Transformer: " + transformer.getClass().getSimpleName());
            span.setStartTime(System.currentTimeMillis());

            context.setParentSpanId(spanId);
            transformedParams = transformer.transform(event, envelope);
            span.setEndTime(System.currentTimeMillis());
            context.addSpan(span);
        }

        return transformedParams;
    }

    /**
     * 获取排序后的过滤器列表
     */
    private List<SignalFilter<S, T>> getSortedFilters(String event) {
        List<SignalFilter<S, T>> filters = signalFilters.getOrDefault(event, Collections.emptyList());
        List<SignalFilter<S, T>> copy = new ArrayList<>(filters);
        copy.sort(Comparator.comparingInt(SignalFilter::getPriority));
        return copy;
    }

    /**
     * 添加拦截器
     */
    public void addInterceptor(String event, SignalInterceptor<S, T> interceptor) {
        signalInterceptors.computeIfAbsent(event, k -> new ArrayList<>()).add(interceptor);
        log.info("Interceptor [{}] added to event [{}]", interceptor.getClass().getSimpleName(), event);
    }

    /**
     * 添加过滤器
     */
    public void addFilter(String event, SignalFilter<S, T> filter) {
        signalFilters.computeIfAbsent(event, k -> new ArrayList<>()).add(filter);
    }

    /**
     * 添加转换器
     */
    public void addTransformer(String event, SignalTransformer<S, T> transformer) {
        signalTransformers.computeIfAbsent(event, k -> new ArrayList<>()).add(transformer);
    }

    /**
     * 获取拦截器
     */
    public Map<String, List<SignalInterceptor<S, T>>> getSignalInterceptors() {
        return new ConcurrentHashMap<>(signalInterceptors);
    }

    /**
     * 获取过滤器
     */
    public Map<String, List<SignalFilter<S, T>>> getSignalFilters() {
        return new ConcurrentHashMap<>(signalFilters);
    }

    /**
     * 获取转换器
     */
    public Map<String, List<SignalTransformer<S, T>>> getSignalTransformers() {
        return new ConcurrentHashMap<>(signalTransformers);
    }
}
