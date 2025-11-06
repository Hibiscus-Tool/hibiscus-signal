package io.github.signal.core.interceptor;

import io.github.signal.core.SignalFilter;
import io.github.signal.core.model.Envelope;

public class MySignalFilter implements SignalFilter<Object, Object> {

    @Override
    public boolean filter(String event, Envelope<Object, Object> envelope) {
        // Implement your filtering logic here
        System.out.println("Filtering event: " + event);
        // Allow the event to continue
        return true;
    }

    @Override
    public int getPriority() {
        return 0; // You can set the priority if needed
    }
}
