package io.github.signal.spring.configuration;

import io.github.signal.core.SignalFilter;
import io.github.signal.core.SignalInterceptor;
import io.github.signal.core.SignalTransformer;
import io.github.signal.core.Signals;
import io.github.signal.spring.anno.SignalFilterBind;
import io.github.signal.spring.anno.SignalInterceptorBind;
import io.github.signal.spring.anno.SignalTransformerBind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Automatically detects and registers Signal-related components such as
 * interceptors, filters, and transformers based on their annotations.
 */
@Component
public class SignalComponentRegistrar implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(SignalComponentRegistrar.class);

    private final Signals<Object, Object> signals;

    public SignalComponentRegistrar(Signals<Object, Object> signals) {
        this.signals = signals;
    }


    /**
     * Intercepts all Spring-initialized beans and automatically registers those that
     * if they are annotated with their respective binding annotations.
     *
     * @param bean     the fully initialized bean instance
     * @param beanName the name of the bean in the Spring context
     * @return the same bean instance, possibly modified
     * @throws BeansException if any post-processing fails
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Register signal interceptors
        if (bean instanceof SignalInterceptor<?, ?>) {
            SignalInterceptorBind annotation = bean.getClass().getAnnotation(SignalInterceptorBind.class);
            if (annotation != null) {
                registerInterceptor((SignalInterceptor<Object, Object>) bean, annotation);
            }
        }
        // Register signal filters
        if (bean instanceof SignalFilter<?, ?>) {
            SignalFilterBind annotation = bean.getClass().getAnnotation(SignalFilterBind.class);
            if (annotation != null) {
                registerFilter((SignalFilter<Object, Object>) bean, annotation);
            }
        }
        // Register signal transformers
        if (bean instanceof SignalTransformer<?, ?>) {
            SignalTransformerBind annotation = bean.getClass().getAnnotation(SignalTransformerBind.class);
            if (annotation != null) {
                registerTransformer((SignalTransformer<Object, Object>) bean, annotation);
            }
        }
        return bean;
    }

    /**
     * Registers a signal interceptor to one or more events based on the annotation.
     * Supports wildcard `"*"` to apply the interceptor to all currently registered events.
     *
     * @param interceptor the SignalInterceptor instance
     * @param annotation  the associated {@link SignalInterceptorBind} annotation
     */
    private void registerInterceptor(SignalInterceptor<Object, Object> interceptor, SignalInterceptorBind annotation) {
        for (String pattern : annotation.value()) {
            // 如果是 "*"，则注册到所有事件
            if ("*".equals(pattern)) {
                signals.getRegisteredEvents().forEach(event ->
                        signals.addSignalInterceptor(event, interceptor)
                );
                logger.info("Registered interceptor: {} for all events", interceptor.getClass().getSimpleName());
            } else {
                // 处理 "user.*" 或类似的事件模式
                boolean isPattern = pattern.contains("*");
                if (isPattern) {
                    // 通过正则表达式匹配事件
                    String regexPattern = convertToRegex(pattern);
                    signals.getRegisteredEvents().forEach(event -> {
                        if (event.matches(regexPattern)) {
                            signals.addSignalInterceptor(event, interceptor);
                            logger.info("Registered interceptor: {} for event: {}", interceptor.getClass().getSimpleName(), event);
                        }
                    });
                } else {
                    // 如果没有通配符，直接注册
                    signals.addSignalInterceptor(pattern, interceptor);
                    logger.info("Registered interceptor: {} for event: {}", interceptor.getClass().getSimpleName(), pattern);
                }
            }
        }
    }

    /**
     * Registers a signal filter to the specified events based on the annotation.
     *
     * @param filter      the SignalFilter instance
     * @param annotation  the associated {@link SignalFilterBind} annotation
     */
    private void registerFilter(SignalFilter<Object, Object> filter, SignalFilterBind annotation) {
        for (String pattern : annotation.value()) {
            // 如果是 "*"，则注册到所有事件
            if ("*".equals(pattern)) {
                signals.getRegisteredEvents().forEach(event ->
                        signals.addFilter(event, filter)
                );
                logger.info("Registered filter: {} for all events", filter.getClass().getSimpleName());
            } else {
                // 处理 "user.*" 或类似的事件模式
                boolean isPattern = pattern.contains("*");
                if (isPattern) {
                    // 通过正则表达式匹配事件
                    String regexPattern = convertToRegex(pattern);
                    signals.getRegisteredEvents().forEach(event -> {
                        if (event.matches(regexPattern)) {
                            signals.addFilter(event, filter);
                            logger.info("Registered filter: {} for event: {}", filter.getClass().getSimpleName(), event);
                        }
                    });
                } else {
                    // 如果没有通配符，直接注册
                    signals.addFilter(pattern, filter);
                    logger.info("Registered filter: {} for event: {}", filter.getClass().getSimpleName(), pattern);
                }
            }
        }
    }

    /**
     * Registers a signal transformer to the specified events based on the annotation.
     *
     * @param transformer the SignalTransformer instance
     * @param annotation  the associated {@link SignalTransformerBind} annotation
     */
    private void registerTransformer(SignalTransformer<Object, Object> transformer, SignalTransformerBind annotation) {
        for (String pattern : annotation.value()) {
            // 如果是 "*"，则注册到所有事件
            if ("*".equals(pattern)) {
                signals.getRegisteredEvents().forEach(event ->
                        signals.addSignalTransformer(event, transformer)
                );
                logger.info("Registered transformer: {} for all events", transformer.getClass().getSimpleName());
            } else {
                // 处理 "user.*" 或类似的事件模式
                boolean isPattern = pattern.contains("*");
                if (isPattern) {
                    // 通过正则表达式匹配事件
                    String regexPattern = convertToRegex(pattern);
                    signals.getRegisteredEvents().forEach(event -> {
                        if (event.matches(regexPattern)) {
                            signals.addSignalTransformer(event, transformer);
                            logger.info("Registered transformer: {} for event: {}", transformer.getClass().getSimpleName(), event);
                        }
                    });
                } else {
                    // 如果没有通配符，直接注册
                    signals.addSignalTransformer(pattern, transformer);
                    logger.info("Registered transformer: {} for event: {}", transformer.getClass().getSimpleName(), pattern);
                }
            }
        }
    }

    /**
     * 将通配符模式转换为正则表达式
     * 例如："user.*" 会转换成 "user\..*"，以匹配以"user."为前缀的所有事件。
     *
     * @param pattern 原始通配符模式
     * @return 转换后的正则表达式
     */
    private String convertToRegex(String pattern) {
        // 将通配符 "*" 转换成正则表达式 ".*"
        return pattern.replace(".", "\\.").replace("*", ".*");
    }
}
