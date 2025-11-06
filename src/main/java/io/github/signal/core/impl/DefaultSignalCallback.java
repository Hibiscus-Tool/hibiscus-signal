package io.github.signal.core.impl;

import io.github.signal.core.SignalCallback;
import io.github.signal.core.model.Envelope;
import io.github.signal.core.model.SignalContext;
import io.github.signal.utils.SignalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the SignalCallback interface.
 * Purpose:
 * - Logs the outcomes of signal processing (success, error, and completion).
 * - Provides a basic tracing mechanism when the signal processing is complete.
 */
public class DefaultSignalCallback implements SignalCallback<Object, Object> {

    // Logger for recording the callback actions
    private static final Logger log = LoggerFactory.getLogger(DefaultSignalCallback.class);

    /**
     * Called when signal processing is successful.
     *
     * @param event  the name of the signal event
     */
    @Override
    public void onSuccess(String event, Envelope<Object, Object> envelope) {
        log.info("Signal [{}] processed successfully by [{}]. Parameters: {}",
                event, envelope.getSender(), envelope.getPayload());
    }

    /**
     * Called when an error occurs during signal processing.
     *
     * @param event  the name of the signal event
     * @param error  the error that occurred
     */
    @Override
    public void onError(String event, Envelope<Object, Object> envelope, Throwable error) {
        log.error("Signal [{}] failed to process by [{}]. Error: {}. Parameters: {}",
                event, envelope.getSender(), error.getMessage(), envelope.getPayload(), error);
    }

    /**
     * Called after the signal processing (regardless of success or error).
     * If the signal's parameters include a SignalContext, prints the tracing tree.
     *
     * @param event  the name of the signal event
     */
    @Override
    public void onComplete(String event, Envelope<Object, Object> envelope) {
        // Check if the parameters include a SignalContext for tracing
        SignalContext context = envelope.getContext();
        // If context is present, print the trace tree
        if (context != null) {
            SignalTracer.printTraceTree(context);
        }
        log.info("Signal [{}] processing completed by [{}]. Parameters: {}",
                event, envelope.getSender(), envelope.getPayload());
    }
}
