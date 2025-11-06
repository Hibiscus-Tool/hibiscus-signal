package io.github.signal.spring.configuration;

import io.github.signal.core.tractional.DeadLetterQueueManager;
import io.github.signal.core.tractional.EventTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.sql.DataSource;
import java.lang.reflect.Method;

@Configuration
@ConditionalOnClass(PlatformTransactionManager.class)
@ConditionalOnProperty(name = "hibiscus.transaction-enabled", havingValue = "true", matchIfMissing = true)
public class SignalTransactionConfiguration<S, T> {

    private static final Logger log = LoggerFactory.getLogger(SignalTransactionConfiguration.class);

    private final SignalProperties signalProperties;

    public SignalTransactionConfiguration(SignalProperties signalProperties) {
        this.signalProperties = signalProperties;
        log.info("事务配置已启用: 重试={}, 最大重试次数={}, 重试延迟={}ms",
                signalProperties.getTransaction().getEnableRetry(),
                signalProperties.getTransaction().getMaxRetries(),
                signalProperties.getTransaction().getRetryDelayMs());
    }

    @Bean
    @ConditionalOnProperty(name = "hibiscus.transaction-enabled", havingValue = "true")
    public EventTransactionManager<S, T> eventTransactionManager(PlatformTransactionManager transactionManager) {
        return new EventTransactionManager<>(transactionManager);
    }

    @Bean
    @ConditionalOnProperty(name = "hibiscus.transaction-enabled", havingValue = "true")
    public DeadLetterQueueManager<S, T> deadLetterQueueManager() {
        SignalProperties.DeadLetterProperties deadLetterProps = signalProperties.getDeadLetter();
        return new DeadLetterQueueManager<>(
                deadLetterProps.getMaxEvents(),
                deadLetterProps.getRetentionDays().intValue(),
                deadLetterProps.getEnableAutoCleanup()
        );
    }

    /**
     * 创建事务管理器
     * 使用反射方式创建，避免直接依赖
     */
    @Bean
    @ConditionalOnProperty(name = "hibiscus.transaction-enabled", havingValue = "true")
    @ConditionalOnMissingBean(PlatformTransactionManager.class)
    public PlatformTransactionManager platformTransactionManager(
            @Autowired(required = false) DataSource dataSource) {

        if (dataSource != null) {
            try {
                // 使用反射创建 DataSourceTransactionManager，避免直接依赖
                Class<?> dataSourceTransactionManagerClass = Class.forName(
                        "org.springframework.jdbc.datasource.DataSourceTransactionManager");
                Object transactionManager = dataSourceTransactionManagerClass.getDeclaredConstructor().newInstance();

                // 设置 DataSource
                Method setDataSourceMethod = dataSourceTransactionManagerClass.getMethod("setDataSource", DataSource.class);
                setDataSourceMethod.invoke(transactionManager, dataSource);

                log.info("使用 DataSource 创建事务管理器");
                return (PlatformTransactionManager) transactionManager;

            } catch (Exception e) {
                log.warn("无法创建 DataSourceTransactionManager，使用内存事务管理器: {}", e.getMessage());
                return createMemoryTransactionManager();
            }
        } else {
            log.warn("未找到 DataSource，创建内存事务管理器");
            return createMemoryTransactionManager();
        }
    }

    /**
     * 创建内存事务管理器
     */
    private PlatformTransactionManager createMemoryTransactionManager() {
        return new AbstractPlatformTransactionManager() {
            @Override
            protected Object doGetTransaction() {
                return new Object();
            }

            @Override
            protected void doBegin(Object transaction, TransactionDefinition definition) {
                log.debug("开始内存事务");
            }

            @Override
            protected void doCommit(DefaultTransactionStatus status) {
                log.debug("提交内存事务");
            }

            @Override
            protected void doRollback(DefaultTransactionStatus status) {
                log.debug("回滚内存事务");
            }

            @Override
            protected void doSetRollbackOnly(DefaultTransactionStatus status) {
                status.setRollbackOnly();
            }

            @Override
            protected boolean isExistingTransaction(Object transaction) {
                return false;
            }

            @Override
            protected void doCleanupAfterCompletion(Object transaction) {
                // 清理资源
            }
        };
    }
}
