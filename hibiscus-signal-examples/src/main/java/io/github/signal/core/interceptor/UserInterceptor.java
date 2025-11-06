package io.github.signal.core.interceptor;

import io.github.signal.core.SignalInterceptor;
import io.github.signal.core.model.Envelope;
import io.github.signal.spring.anno.SignalInterceptorBind;
import org.springframework.stereotype.Component;

@Component
@SignalInterceptorBind("user.created")
public class UserInterceptor implements SignalInterceptor<Object, Object> {

    @Override
    public void afterHandle(String event, Envelope<Object, Object> envelope, Throwable error) {
        System.out.println("afterHandle");
        SignalInterceptor.super.afterHandle(event, envelope, error);
    }

    @Override
    public int getOrder() {
        return SignalInterceptor.super.getOrder();
    }

    @Override
    public boolean beforeHandle(String event, Envelope<Object, Object> envelope) {
        System.out.println("beforeHandle");
        return SignalInterceptor.super.beforeHandle(event, envelope);
    }
}
