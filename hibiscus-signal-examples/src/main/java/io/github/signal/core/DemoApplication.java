package io.github.signal.core;


import io.github.signal.core.config.SignalConfig;
import io.github.signal.core.interceptor.MySignalFilter;
import io.github.signal.core.interceptor.MySignalInterceptor;
import io.github.signal.core.interceptor.MySignalTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Autowired
    private Signals<Object, Object> signals;

    @PostConstruct
    public void init() {
        // 注册事件处理器
        signals.addSignalInterceptor("demoEvent", new MySignalInterceptor());
        signals.addFilter("demoEvent", new MySignalFilter());
        signals.addSignalTransformer("demoEvent", new MySignalTransformer());
        signals.connect("demoEvent", envelope -> {
            // 输出事件内容
            System.out.println("\ndemoEvent: " + envelope.getSender() + ", " + envelope.getPayload() + "\n");
        }, SignalConfig.builder()
                .async(true)
                .maxHandlers(20)
                .maxRetries(3)
                .recordMetrics(false)
                .retryDelayMs(1000)
                .build());
    }
}

