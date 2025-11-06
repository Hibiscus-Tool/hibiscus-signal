package io.github.signal.core.observer;

import io.github.signal.core.model.SignalContext;
import io.github.signal.spring.anno.SignalHandler;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    @SignalHandler(value = "user.created",target = UserEventListener.class, methodName = "handleUserCreated", async = false)
    public void handleUserCreated(SignalContext context) {
        System.out.println(context);
        System.out.println("UserEventListener.handleUserCreated");
    }
}
