package io.github.signal.core.config;

import io.github.signal.core.enums.SignalPriority;

/**
 * Signal 配置类
 */
public class SignalConfig {

    /**
     * 是否异步执行
     */
    private boolean async;

    /**
     * 最大重试次数
     */
    private int maxRetries;

    /**
     * 重试间隔时间
     */
    private long retryDelayMs;

    /**
     * 最大处理者数量
     */
    private int maxHandlers;

    /**
     * 超时时间
     */
    private long timeoutMs;

    /**
     * 是否记录指标
     */
    private boolean recordMetrics;

    /**
     * 优先级
     */
    private SignalPriority priority;

    /**
     * 事务配置
     */
    private TransactionConfig transactionConfig;

    /**
     * 静态 builder 工厂方法，使用时无需写 new
     */
    public static Builder builder() {
        return new Builder();
    }

    public SignalConfig() {
        this(new Builder());
    }

    // 默认构造器
    public SignalConfig(Builder builder) {
        this.async = builder.async;
        this.maxRetries = builder.maxRetries;
        this.retryDelayMs = builder.retryDelayMs;
        this.maxHandlers = builder.maxHandlers;
        this.timeoutMs = builder.timeoutMs;
        this.recordMetrics = builder.recordMetrics;
        this.priority = builder.priority;
    }

    public boolean isAsync() {
        return async;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public int getMaxHandlers() {
        return maxHandlers;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public boolean isRecordMetrics() {
        return recordMetrics;
    }

    public SignalPriority getPriority() {
        return priority;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }

    public void setMaxHandlers(int maxHandlers) {
        this.maxHandlers = maxHandlers;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public void setRecordMetrics(boolean recordMetrics) {
        this.recordMetrics = recordMetrics;
    }

    public void setPriority(SignalPriority priority) {
        this.priority = priority;
    }

    public TransactionConfig getTransactionConfig() {
        return transactionConfig;
    }

    public void setTransactionConfig(TransactionConfig transactionConfig) {
        this.transactionConfig = transactionConfig;
    }

    public static class Builder {

        private boolean async = true;

        private int maxRetries = 10;

        private long retryDelayMs = 100L;

        private int maxHandlers = 3;

        private long timeoutMs = 10000;

        private boolean recordMetrics = false;

        private SignalPriority priority = SignalPriority.MEDIUM;

        private TransactionConfig transactionConfig;

        public Builder async(boolean async) {
            this.async = async;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder retryDelayMs(long retryDelayMs) {
            this.retryDelayMs = retryDelayMs;
            return this;
        }

        public Builder maxHandlers(int maxHandlers) {
            this.maxHandlers = maxHandlers;
            return this;
        }

        public Builder timeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public Builder recordMetrics(boolean recordMetrics) {
            this.recordMetrics = recordMetrics;
            return this;
        }

        public Builder priority(SignalPriority priority) {
            this.priority = priority;
            return this;
        }

        public Builder transactionConfig(TransactionConfig transactionConfig) {
            this.transactionConfig = transactionConfig;
            return this;
        }

        public SignalConfig build() {
            return new SignalConfig(this);
        }
    }
}
