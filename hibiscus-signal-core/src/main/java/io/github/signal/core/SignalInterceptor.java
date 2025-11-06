package io.github.signal.core;

import io.github.signal.core.model.Envelope;

public interface SignalInterceptor<S, T> {

    /**
     * 信号处理前的拦截
     * @param event 信号名称
     * @param envelope 信号内容
     * @return true表示继续处理，false表示中断处理
     */
    default boolean beforeHandle(String event, Envelope<S, T> envelope) {
        return true;
    }

    /**
     * 信号处理后的拦截
     * @param event 信号名称
     * @param envelope 信号内容
     * @param error 处理过程中的异常，如果没有则为null
     */
    default void afterHandle(String event, Envelope<S, T> envelope, Throwable error) {
    }

    /**
     * 获取拦截器优先级
     * @return 优先级，数字越小优先级越高
     */
    default int getOrder() {
        return 0;
    }
}
