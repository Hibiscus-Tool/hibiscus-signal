package io.github.signal.core.config;

import org.springframework.transaction.TransactionDefinition;

/**
 * 事务配置类
 * 配置事件处理的事务策略
 */
public class TransactionConfig {

    /**
     * 是否启用事务
     */
    private boolean enableTransaction = true;

    /**
     * 是否启用重试
     */
    private boolean enableRetry = true;

    /**
     * 是否启用死信队列
     */
    private boolean enableDeadLetter = true;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;

    /**
     * 重试延迟时间
     */
    private long retryDelayMs = 1000L;

    /**
     * 最大重试延迟时间
     */
    private long maxRetryDelayMs = 60000L; // 最大重试延迟1分钟

    /**
     * 是否启用指数退避
     */
    private boolean enableExponentialBackoff = true;

    /**
     * 是否启用抖动
     */
    private boolean enableJitter = true;

    /**
     * 死信队列保留天数
     */
    private int deadLetterRetentionDays = 30;

    // 事务隔离级别
    private int isolationLevel = TransactionDefinition.ISOLATION_READ_COMMITTED;
    // 事务传播行为
    private int propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TransactionConfig config = new TransactionConfig();

        public Builder enableTransaction(boolean enable) {
            config.enableTransaction = enable;
            return this;
        }

        public Builder enableRetry(boolean enable) {
            config.enableRetry = enable;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            config.maxRetries = maxRetries;
            return this;
        }

        public Builder retryDelayMs(long delay) {
            config.retryDelayMs = delay;
            return this;
        }

        public Builder isolationLevel(int level) {
            config.isolationLevel = level;
            return this;
        }

        public Builder propagationBehavior(int behavior) {
            config.propagationBehavior = behavior;
            return this;
        }

        public TransactionConfig build() {
            return config;
        }
    }

    public boolean isEnableTransaction() {
        return enableTransaction;
    }

    public void setEnableTransaction(boolean enableTransaction) {
        this.enableTransaction = enableTransaction;
    }

    public boolean isEnableRetry() {
        return enableRetry;
    }

    public void setEnableRetry(boolean enableRetry) {
        this.enableRetry = enableRetry;
    }

    public boolean isEnableDeadLetter() {
        return enableDeadLetter;
    }

    public void setEnableDeadLetter(boolean enableDeadLetter) {
        this.enableDeadLetter = enableDeadLetter;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }

    public long getMaxRetryDelayMs() {
        return maxRetryDelayMs;
    }

    public void setMaxRetryDelayMs(long maxRetryDelayMs) {
        this.maxRetryDelayMs = maxRetryDelayMs;
    }

    public boolean isEnableExponentialBackoff() {
        return enableExponentialBackoff;
    }

    public void setEnableExponentialBackoff(boolean enableExponentialBackoff) {
        this.enableExponentialBackoff = enableExponentialBackoff;
    }

    public boolean isEnableJitter() {
        return enableJitter;
    }

    public void setEnableJitter(boolean enableJitter) {
        this.enableJitter = enableJitter;
    }

    public int getDeadLetterRetentionDays() {
        return deadLetterRetentionDays;
    }

    public void setDeadLetterRetentionDays(int deadLetterRetentionDays) {
        this.deadLetterRetentionDays = deadLetterRetentionDays;
    }

    public int getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(int isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    public int getPropagationBehavior() {
        return propagationBehavior;
    }

    public void setPropagationBehavior(int propagationBehavior) {
        this.propagationBehavior = propagationBehavior;
    }
}
