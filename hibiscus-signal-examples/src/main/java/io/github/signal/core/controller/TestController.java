package io.github.signal.core.controller;

import io.github.signal.spring.anno.SignalEmitter;
import io.github.signal.utils.SignalContextCollector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    @SignalEmitter("user.created")
    public String index() {
        SignalContextCollector.collect("user.created", "66666");
        return "Hello World!";
    }



//    @Autowired
//    private Signals<String, Object> signals;
//
//    @GetMapping("/")
//    public String index() {
//        signals.emit("demoEvent", Envelope.Builder.<String, Object>builder()
//                .context(new SignalContext())   // 添加上下文
//                .sender("你好")            // 发送者
//                .payload("我是木然")           // 载荷
//                .build(), throwable -> {
//            // 错误处理
//        });
//        return "Hello World!";
//    }
}
