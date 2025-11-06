package io.github.signal.core;

import io.github.signal.core.model.Envelope;

/**
 * Callback interface for observing signal processing outcomes.
 * Purpose:
 * - Provides hooks for reacting to success, errors, and overall completion.
 * - Can be customized to implement logging, tracing, or additional business logic.
 */
public interface SignalCallback<S, T> {

    /**
     * Called when the signal has been processed successfully.
     *
     * @param event  the name of the signal event
     */
    default void onSuccess(String event, Envelope<S, T> envelope) {}

    /**
     * Called when an error occurs during signal processing.
     *
     * @param event  the name of the signal event
     * @param error  the error that occurred
     */
    default void onError(String event, Envelope<S, T> envelope, Throwable error) {}

    /**
     * Called after signal processing is complete, regardless of success or error.
     *
     * @param event  the name of the signal event
     */
    default void onComplete(String event, Envelope<S, T> envelope) {}
}
