package io.github.signal.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.signal.core.SignalHandler;
import io.github.signal.core.enums.EventType;
import io.github.signal.core.enums.SignalPriority;

/**
 * Signal
 */
public class Sig<S, T> {

    /**
     * Signal Id
     */
    private Long id;

    /**
     * Signal Name
     */
    private String signalName;

    /**
     * Signal Event Type
     */
    private EventType evType;

    /**
     * Signal Handler
     */
    @JsonIgnore
    private SignalHandler<S, T> handler;

    /**
     * Signal Priority
     */
    private SignalPriority priority;

    /**
     * Signal Context
     */
    private SignalContext signalContext;

    private Sig(Builder<S, T> builder) {
        this.id = builder.id;
        this.signalName = builder.signalName;
        this.handler = builder.handler;
        this.priority = builder.priority;
        this.signalContext = builder.signalContext;
        this.evType = builder.evType;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSignalName() {
        return signalName;
    }

    public void setEvType(EventType evType) {
        this.evType = evType;
    }

    public EventType getEvType() {
        return evType;
    }

    public SignalHandler<S, T> getHandler() {
        return handler;
    }

    public void setSignalName(String signalName) {
        this.signalName = signalName;
    }

    public void setHandler(SignalHandler<S, T> handler) {
        this.handler = handler;
    }

    public SignalPriority getPriority() {
        return priority;
    }

    public void setPriority(SignalPriority priority) {
        this.priority = priority;
    }

    public SignalContext getSignalContext() {
        return signalContext;
    }

    public void setSignalContext(SignalContext signalContext) {
        this.signalContext = signalContext;
    }
    public static class Builder<S, T> {
        private Long id;
        private String signalName;
        private SignalHandler<S, T> handler;
        private SignalPriority priority = SignalPriority.MEDIUM; // 默认值
        private SignalContext signalContext;
        private EventType evType;

        public Builder<S, T> id(Long id) {
            this.id = id;
            return this;
        }

        public Builder<S, T> signalName(String signalName) {
            this.signalName = signalName;
            return this;
        }

        public Builder<S, T> handler(SignalHandler<S, T> handler) {
            this.handler = handler;
            return this;
        }

        public Builder<S, T> priority(SignalPriority priority) {
            this.priority = priority;
            return this;
        }

        public Builder<S, T> signalContext(SignalContext signalContext) {
            this.signalContext = signalContext;
            return this;
        }

        public Builder<S, T> evType(EventType evType) {
            this.evType = evType;
            return this;
        }

        public Sig<S, T> build() {
            return new Sig<>(this);
        }
    }

    public static <S, T> Builder<S, T> builder() {
        return new Builder<>();
    }
}
