package io.github.signal.spring.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.signal.core.persistent.DatabaseSignalPersistence;
import io.github.signal.core.persistent.EventRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DB配置类
 * 提供DB连接和序列化配置
 * 只有在明确启用DB且存在DB相关类时才加载
 *
 * @author heathcetide
 */
@Configuration
@ConditionalOnClass({javax.sql.DataSource.class, org.springframework.data.jpa.repository.JpaRepository.class})
@ConditionalOnProperty(name = "hibiscus.database-persistent", havingValue = "true", matchIfMissing = false)
public class SignalDBConfiguration {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRecordRepository eventRecordRepository;

    /**
     * 手动注册 DBSignalPersistence 作为 Spring Bean
     */
    @Bean
    @ConditionalOnProperty(name = "hibiscus.database-persistent", havingValue = "true", matchIfMissing = false)
    public DatabaseSignalPersistence databaseSignalPersistence() {
        return new DatabaseSignalPersistence(eventRecordRepository, objectMapper);
    }
}
