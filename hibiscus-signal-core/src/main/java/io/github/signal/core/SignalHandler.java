package io.github.signal.core;

import io.github.signal.core.model.Envelope;

@FunctionalInterface
public interface SignalHandler<S, T> {

    /**
     * Handle a signal.
     */
    void handle(Envelope<S, T> envelope);

    /**
     * Compose two signal handlers.
     */
    default SignalHandler<S, T> andThen(SignalHandler<S, T> after) {
        return (envelope) -> {
            this.handle(envelope);
            after.handle(envelope);
        };
    }

    /**
     * Filter a signal.
     */
    default SignalHandler<S, T> when(SignalPredicate<S, T> predicate) {
        return (envelope) -> {
            if (predicate.isEligible(envelope)) {
                this.handle(envelope);
            }
        };
    }

    @FunctionalInterface
    interface SignalPredicate<S, T> {
        /**
         * Tests whether the signal should be processed.
         */
        boolean isEligible(Envelope<S, T> envelope);
    }
}
