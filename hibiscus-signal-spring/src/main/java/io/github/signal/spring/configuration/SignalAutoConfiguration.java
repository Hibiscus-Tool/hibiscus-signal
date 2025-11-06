package io.github.signal.spring.configuration;


import io.github.signal.core.Signals;
import io.github.signal.core.tractional.DeadLetterQueueManager;
import io.github.signal.core.tractional.EventTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ExecutorService;

/**
 * Spring Boot auto-configuration class for the Signal framework.
 * if no user-defined beans are provided.
 */
@Configuration
@Import({SignalRedisConfiguration.class, SignalTransactionConfiguration.class})
public class SignalAutoConfiguration<S, T> {

    private static final Logger log = LoggerFactory.getLogger(SignalAutoConfiguration.class);

    /*
     * Provides a default {@link Signals} bean if one is not already defined.
     *
     * @return a singleton instance of the signal manager
     */
    @Bean(name = "signals")
    @Primary
    @ConditionalOnMissingBean(Signals.class)
    public Signals<S, T> signalManager(@Qualifier("signalExecutor") ExecutorService executorService,
                                       @Autowired(required = false) EventTransactionManager<S, T> transactionManager,
                                       @Autowired(required = false) DeadLetterQueueManager<S, T> deadLetterQueueManager) {

        if (transactionManager != null && deadLetterQueueManager != null) {
            log.info("创建支持事务的信号管理器");
            return new Signals<>(executorService, transactionManager, deadLetterQueueManager);
        } else {
            log.info("创建基础信号管理器（无事务支持）");
            return new Signals<>(executorService);
        }
    }
}
