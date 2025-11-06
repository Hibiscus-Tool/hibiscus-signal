package io.github.signal.core.persistent;


import io.github.signal.core.config.SignalConfig;
import io.github.signal.core.model.Sig;
import io.github.signal.core.model.SignalContext;

import java.util.Map;

public class SignalPersistenceInfo<S, T> {

    private Sig<S, T> sig;

    private SignalConfig signalConfig;

    private SignalContext signalContext;

    private Map<String, Object> metrics;

    public SignalPersistenceInfo() {
    }

    public SignalPersistenceInfo(Sig<S, T> sig, SignalConfig signalConfig, SignalContext signalContext, Map<String, Object> metrics) {
        this.sig = sig;
        this.signalConfig = signalConfig;
        this.signalContext = signalContext;
        this.metrics = metrics;
    }

    public Sig<S, T> getsig() {
        return sig;
    }

    public void setSig(Sig<S, T>  sigHandler) {
        this.sig = sig;
    }

    public SignalConfig getSignalConfig() {
        return signalConfig;
    }

    public void setSignalConfig(SignalConfig signalConfig) {
        this.signalConfig = signalConfig;
    }

    public SignalContext getSignalContext() {
        return signalContext;
    }

    public void setSignalContext(SignalContext signalContext) {
        this.signalContext = signalContext;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }

    @Override
    public String toString() {
        return "SignalPersistenceInfo{" +
                "sig=" + sig +
                ", signalConfig=" + signalConfig +
                ", signalContext=" + signalContext +
                ", metrics=" + metrics +
                '}';
    }
}
