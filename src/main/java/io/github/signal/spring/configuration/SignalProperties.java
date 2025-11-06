package io.github.signal.spring.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.TransactionDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Signal properties.
 */
@Configuration
@ComponentScan
@ConfigurationProperties("hibiscus")
public class SignalProperties {

    /**
     * Whether to persist the signal to a file.
     */
    private Boolean persistent = false;

    /**
     * The persistence methods to use (e.g., "file", "redis").
     * Possible values: "file", "redis".
     */
    private List<String> persistenceMethods = new ArrayList<>();

    /**
     * The file to persist the signal to.
     */
    private String persistenceFile = "signal.json";

    /**
     * Redis Enabled.
     */
    private Boolean redisEnabled = false;

    /**
     * Redis Host
     */
    private String redisHost = "localhost";

    /**
     * Redis Port
     */
    private Integer redisPort = 6379;

    /**
     * Redis Password
     */
    private String redisPassword = "";

    /**
     * Redis Database
     */
    private Integer redisDatabase = 0;

    /**
     * Redis Expire Seconds
     */
    private Integer redisExpireSeconds = 86400;

    /**
     * Database Persistent
     */
    private Boolean databasePersistent = false;

    /**
     * Database Persistent Table Name
     */
    private String databaseTableName = "signal_events";

    /**
     * Database Retention Days
     */
    private Integer databaseRetentionDays = 7;

    /**
     * Enable Database Cleanup
     */
    private Boolean enableDatabaseCleanup = true;

    /**
     * MQ Enabled
     */
    private Boolean mqEnabled = false;

    /**
     * MQ Type
     */
    private String mqType = "rabbitmq"; // rabbitmq, kafka, rocketmq

    /**
     * MQ Host
     */
    private String mqHost = "localhost";

    /**
     * MQ Port
     */
    private Integer mqPort = 5672;

    /**
     * MQ Username
     */
    private String mqUsername = "guest";

    /**
     * MQ Password
     */
    private String mqPassword = "guest";

    /**
     * MQ Virtual Host
     */
    private String mqVirtualHost = "/";

    /**
     * Protection Enabled
     */
    private Boolean protectionEnabled = false;

    /**
     * Circuit Breaker Enabled
     */
    private Integer circuitBreakerFailureThreshold = 5;

    /**
     * Circuit Breaker Open Timeout (ms)
     */
    private Long circuitBreakerOpenTimeoutMs = 60000L; // 60秒

    /**
     * Circuit Breaker Half Open Trial Count
     */
    private Integer circuitBreakerHalfOpenTrialCount = 3;

    /**
     * Rate Limiter Enabled
     */
    private Integer rateLimiterMaxRequestsPerSecond = 1000;

    /**
     * Rate Limiter Error Rate Threshold
     */
    private Double circuitBreakerErrorRateThreshold = 0.5;

    /**
     * Transaction Enabled
     */
    private Boolean transactionEnabled = false;

    /**
     * Transaction Properties
     */
    private TransactionProperties transaction = new TransactionProperties();

    /**
     * Dead Letter Enabled
     */
    private DeadLetterProperties deadLetter = new DeadLetterProperties();



    public Boolean getPersistent() {
        return persistent;
    }

    public void setPersistent(Boolean persistent) {
        this.persistent = persistent;
    }

    public String getPersistenceFile() {
        return persistenceFile;
    }

    public void setPersistenceFile(String persistenceFile) {
        this.persistenceFile = persistenceFile;
    }

    public Boolean getRedisEnabled() {
        return redisEnabled;
    }

    public void setRedisEnabled(Boolean redisEnabled) {
        this.redisEnabled = redisEnabled;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public Integer getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(Integer redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public Integer getRedisDatabase() {
        return redisDatabase;
    }

    public void setRedisDatabase(Integer redisDatabase) {
        this.redisDatabase = redisDatabase;
    }

    public Integer getRedisExpireSeconds() {
        return redisExpireSeconds;
    }

    public void setRedisExpireSeconds(Integer redisExpireSeconds) {
        this.redisExpireSeconds = redisExpireSeconds;
    }

    public List<String> getPersistenceMethods() {
        return persistenceMethods;
    }

    public void setPersistenceMethods(List<String> persistenceMethods) {
        this.persistenceMethods = persistenceMethods;
    }

    public Boolean getDatabasePersistent() {
        return databasePersistent;
    }

    public void setDatabasePersistent(Boolean databasePersistent) {
        this.databasePersistent = databasePersistent;
    }

    public String getDatabaseTableName() {
        return databaseTableName;
    }

    public void setDatabaseTableName(String databaseTableName) {
        this.databaseTableName = databaseTableName;
    }

    public Integer getDatabaseRetentionDays() {
        return databaseRetentionDays;
    }

    public void setDatabaseRetentionDays(Integer databaseRetentionDays) {
        this.databaseRetentionDays = databaseRetentionDays;
    }

    public Boolean getEnableDatabaseCleanup() {
        return enableDatabaseCleanup;
    }

    public void setEnableDatabaseCleanup(Boolean enableDatabaseCleanup) {
        this.enableDatabaseCleanup = enableDatabaseCleanup;
    }

    public Boolean getMqEnabled() {
        return mqEnabled;
    }

    public void setMqEnabled(Boolean mqEnabled) {
        this.mqEnabled = mqEnabled;
    }

    public String getMqType() {
        return mqType;
    }

    public void setMqType(String mqType) {
        this.mqType = mqType;
    }

    public String getMqHost() {
        return mqHost;
    }

    public void setMqHost(String mqHost) {
        this.mqHost = mqHost;
    }

    public Integer getMqPort() {
        return mqPort;
    }

    public void setMqPort(Integer mqPort) {
        this.mqPort = mqPort;
    }

    public String getMqUsername() {
        return mqUsername;
    }

    public void setMqUsername(String mqUsername) {
        this.mqUsername = mqUsername;
    }

    public String getMqPassword() {
        return mqPassword;
    }

    public void setMqPassword(String mqPassword) {
        this.mqPassword = mqPassword;
    }

    public String getMqVirtualHost() {
        return mqVirtualHost;
    }

    public void setMqVirtualHost(String mqVirtualHost) {
        this.mqVirtualHost = mqVirtualHost;
    }

    public Boolean getProtectionEnabled() {
        return protectionEnabled;
    }

    public void setProtectionEnabled(Boolean protectionEnabled) {
        this.protectionEnabled = protectionEnabled;
    }

    public Integer getCircuitBreakerFailureThreshold() {
        return circuitBreakerFailureThreshold;
    }

    public void setCircuitBreakerFailureThreshold(Integer circuitBreakerFailureThreshold) {
        this.circuitBreakerFailureThreshold = circuitBreakerFailureThreshold;
    }

    public Long getCircuitBreakerOpenTimeoutMs() {
        return circuitBreakerOpenTimeoutMs;
    }

    public void setCircuitBreakerOpenTimeoutMs(Long circuitBreakerOpenTimeoutMs) {
        this.circuitBreakerOpenTimeoutMs = circuitBreakerOpenTimeoutMs;
    }

    public Integer getCircuitBreakerHalfOpenTrialCount() {
        return circuitBreakerHalfOpenTrialCount;
    }

    public void setCircuitBreakerHalfOpenTrialCount(Integer circuitBreakerHalfOpenTrialCount) {
        this.circuitBreakerHalfOpenTrialCount = circuitBreakerHalfOpenTrialCount;
    }

    public Integer getRateLimiterMaxRequestsPerSecond() {
        return rateLimiterMaxRequestsPerSecond;
    }

    public void setRateLimiterMaxRequestsPerSecond(Integer rateLimiterMaxRequestsPerSecond) {
        this.rateLimiterMaxRequestsPerSecond = rateLimiterMaxRequestsPerSecond;
    }

    public Double getCircuitBreakerErrorRateThreshold() {
        return circuitBreakerErrorRateThreshold;
    }

    public void setCircuitBreakerErrorRateThreshold(Double circuitBreakerErrorRateThreshold) {
        this.circuitBreakerErrorRateThreshold = circuitBreakerErrorRateThreshold;
    }


    public Boolean getTransactionEnabled() {
        return transactionEnabled;
    }

    public void setTransactionEnabled(Boolean transactionEnabled) {
        this.transactionEnabled = transactionEnabled;
    }

    public TransactionProperties getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionProperties transaction) {
        this.transaction = transaction;
    }

    public DeadLetterProperties getDeadLetter() {
        return deadLetter;
    }

    public void setDeadLetter(DeadLetterProperties deadLetter) {
        this.deadLetter = deadLetter;
    }

    /**
     * 事务配置属性
     */
    public static class TransactionProperties {
        private Boolean enableRetry = true;
        private Integer maxRetries = 3;
        private Long retryDelayMs = 1000L;
        private Long maxRetryDelayMs = 60000L;
        private Boolean enableExponentialBackoff = true;
        private Boolean enableJitter = true;
        private Integer isolationLevel = TransactionDefinition.ISOLATION_READ_COMMITTED;
        private Integer propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW;

        // Getters and Setters
        public Boolean getEnableRetry() { return enableRetry; }
        public void setEnableRetry(Boolean enableRetry) { this.enableRetry = enableRetry; }

        public Integer getMaxRetries() { return maxRetries; }
        public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }

        public Long getRetryDelayMs() { return retryDelayMs; }
        public void setRetryDelayMs(Long retryDelayMs) { this.retryDelayMs = retryDelayMs; }

        public Long getMaxRetryDelayMs() { return maxRetryDelayMs; }
        public void setMaxRetryDelayMs(Long maxRetryDelayMs) { this.maxRetryDelayMs = maxRetryDelayMs; }

        public Boolean getEnableExponentialBackoff() { return enableExponentialBackoff; }
        public void setEnableExponentialBackoff(Boolean enableExponentialBackoff) { this.enableExponentialBackoff = enableExponentialBackoff; }

        public Boolean getEnableJitter() { return enableJitter; }
        public void setEnableJitter(Boolean enableJitter) { this.enableJitter = enableJitter; }

        public Integer getIsolationLevel() { return isolationLevel; }
        public void setIsolationLevel(Integer isolationLevel) { this.isolationLevel = isolationLevel; }

        public Integer getPropagationBehavior() { return propagationBehavior; }
        public void setPropagationBehavior(Integer propagationBehavior) { this.propagationBehavior = propagationBehavior; }
    }

    /**
     * 死信队列配置属性
     */
    public static class DeadLetterProperties {
        private Integer maxEvents = 10000;
        private Long retentionDays = 30L;
        private Boolean enableAutoCleanup = true;

        // Getters and Setters
        public Integer getMaxEvents() { return maxEvents; }
        public void setMaxEvents(Integer maxEvents) { this.maxEvents = maxEvents; }

        public Long getRetentionDays() { return retentionDays; }
        public void setRetentionDays(Long retentionDays) { this.retentionDays = retentionDays; }

        public Boolean getEnableAutoCleanup() { return enableAutoCleanup; }
        public void setEnableAutoCleanup(Boolean enableAutoCleanup) { this.enableAutoCleanup = enableAutoCleanup; }
    }
}
