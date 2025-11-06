package io.github.signal.core;

import io.github.signal.core.model.Envelope;

@FunctionalInterface
public interface SignalFilter<S, T> {

    /**
     * 过滤信号。
     * 如果信号应继续传播，则@return为 true，如果应阻止，则为 false
     */
    boolean filter(String event, Envelope<S, T> envelope);

    /**
     * 指定筛选器的优先级。
     * 优先级值较低的过滤器将首先执行。
     *
     * @return 优先级值（默认为 0）
     */
    default int getPriority() {
        return 0;
    }
}
