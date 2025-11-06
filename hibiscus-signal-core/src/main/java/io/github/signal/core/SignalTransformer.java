package io.github.signal.core;

import io.github.signal.core.model.Envelope;

@FunctionalInterface
public interface SignalTransformer<S, T> {

    /**
     * 在将信号参数传递给处理程序之前对其进行转换。
     * @return 转换后的参数
     */
    Envelope<S, T> transform(String event, Envelope<S, T> envelope);
}
