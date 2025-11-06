package io.github.signal.core.interceptor;

import io.github.signal.core.SignalTransformer;
import io.github.signal.core.model.Envelope;

public class MySignalTransformer implements SignalTransformer<Object, Object> {

    @Override
    public Envelope<Object, Object> transform(String event, Envelope<Object, Object> envelope) {
        // Implement transformation logic here
        System.out.println("Transforming event: " + event);
        return envelope;  // Return the transformed envelope
    }
}
