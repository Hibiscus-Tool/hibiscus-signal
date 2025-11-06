package io.github.signal.core.model;

/**
 * Signal Envelope
 */
public class Envelope<S, T> {

    /**
     * Event Type
     */
    private String eventType;

    /**
     * Sender
     */
    private S sender;

    /**
     * Payload
     */
    private T payload;

    /**
     * Signal Context
     */
    private SignalContext context;

    /**
     * Builder for Envelope
     */
    public static class Builder<S, T> {

        private String eventType;
        private S sender;
        private T payload;
        private SignalContext context;

        private Builder() {}

        public static <S, T> Builder<S, T> builder() {
            return new Builder<>();
        }

        public Builder<S, T> eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder<S, T> sender(S sender) {
            this.sender = sender;
            return this;
        }

        public Builder<S, T> payload(T payload) {
            this.payload = payload;
            return this;
        }

        public Builder<S, T> context(SignalContext context) {
            this.context = context;
            return this;
        }

        public Envelope<S, T> build() {
            Envelope<S, T> envelope = new Envelope<>();
            envelope.eventType = this.eventType;
            envelope.sender = this.sender;
            envelope.payload = this.payload;
            envelope.context = this.context;
            return envelope;
        }
    }

    public S getSender() {
        return sender;
    }

    public void setSender(S sender) {
        this.sender = sender;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public SignalContext getContext() {
        return context;
    }

    public void setContext(SignalContext context) {
        this.context = context;
    }
}
