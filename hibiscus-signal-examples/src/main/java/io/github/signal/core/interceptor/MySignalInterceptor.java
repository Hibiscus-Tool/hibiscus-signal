package io.github.signal.core.interceptor;

import io.github.signal.core.SignalInterceptor;
import io.github.signal.core.model.Envelope;

public class MySignalInterceptor implements SignalInterceptor<Object, Object> {

    @Override
    public boolean beforeHandle(String event, Envelope<Object, Object> envelope) {
        // Implement logic before handling the signal
        System.out.println("Before handling event: " + event);
        return true;  // Return true to allow handling to proceed
    }

    @Override
    public void afterHandle(String event, Envelope<Object, Object> envelope, Throwable error) {
        // Implement logic after handling the signal
        if (error != null) {
            System.out.println("Error occurred while handling event: " + event);
        } else {
            System.out.println("Successfully handled event: " + event);
        }
    }

    @Override
    public int getOrder() {
        return 0;  // Set order for priority of execution
    }
}
