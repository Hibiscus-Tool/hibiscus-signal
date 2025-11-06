package io.github.signal.core.persistent;

import io.github.signal.core.config.SignalConfig;
import io.github.signal.core.model.Sig;
import io.github.signal.core.model.SignalContext;
import io.github.signal.spring.configuration.SignalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 统一信号持久化管理器
 * 支持多种存储方式的组合使用
 *
 * @author heathcetide
 */
@Service
public class UnifiedSignalPersistence<S, T> {

    private static final Logger log = LoggerFactory.getLogger(UnifiedSignalPersistence.class);

    @Autowired
    private ExecutorService persistenceExecutor;

    @Autowired(required = false)
    private RedisSignalPersistence redisSignalPersistence;

    @Autowired(required = false)
    private DatabaseSignalPersistence databaseSignalPersistence;

    @Autowired(required = false)
    private MqSignalPersistence mqSignalPersistence;

    @Autowired
    private SignalProperties signalProperties;

    /**
     * 异步保存事件信息到所有启用的存储
     */
    public void saveEventAsync(Sig<S, T> sig, SignalConfig config,
                               SignalContext context, Map<String, Object> metrics) {
        CompletableFuture.runAsync(() -> {
            try {
                saveEvent(sig, config, context, metrics);
            } catch (Exception e) {
                log.error("Async Save Event Failed : {}", e.getMessage(), e);
            }
        }, persistenceExecutor);
    }

    /**
     * 同步保存事件信息到所有启用的存储
     */
    public void saveEvent(Sig<S, T> sig, SignalConfig config,
                          SignalContext context, Map<String, Object> metrics) {
        SignalPersistenceInfo<S, T> info = new SignalPersistenceInfo<>(sig, config, context, metrics);
        List<String> persistenceMethods = signalProperties.getPersistenceMethods();
        if (persistenceMethods.contains("file")) {
            String persistenceFile = signalProperties.getPersistenceFile();
            FileSignalPersistence.saveToFileIncrementally(info, persistenceFile);
            log.info("Event Info Is Be Saved : {}", persistenceFile);
        }

        // Redis存储
        if (Boolean.TRUE.equals(signalProperties.getRedisEnabled()) && redisSignalPersistence != null) {
            if (persistenceMethods.contains("redis")) {
                redisSignalPersistence.saveEvent(info);
                log.info("Event Info Is Ready Save To Redis");
            } else {
                log.info("Redis Persistence Is Impossible, Skip Redis Persistence");
            }
        }

        // DB存储
        if (Boolean.TRUE.equals(signalProperties.getDatabasePersistent()) && databaseSignalPersistence != null) {
            if (persistenceMethods.contains("db")) {
                databaseSignalPersistence.saveEventRecord(info.getsig(), info.getSignalConfig(), info.getSignalContext());
                log.info("Event Info Is Ready Save To DB");
            } else {
                log.info("DB Persistence Is Impossible, Skip DB Persistence");
            }
        }

        // MQ存储
        if (Boolean.TRUE.equals(signalProperties.getMqEnabled()) && mqSignalPersistence != null) {
            if (persistenceMethods.contains("mq")) {
                mqSignalPersistence.publishEvent(info);
                log.info("Event Info Is Ready Save To MQ");
            } else {
                log.info("MQ Persistence Is Impossible, Skip MQ Persistence");
            }
        }
    }
}
