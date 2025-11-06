package io.github.signal.core.eventsourcing;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 事件溯源配置属性
 */
@Component
@ConfigurationProperties("hibiscus.event-sourcing")
public class EventSourcingProperties {

    /**
     * 是否启用事件溯源
     */
    private Boolean enabled = false;

    /**
     * 事件存储类型
     */
    private String storageType = "database"; // database, redis, file

    /**
     * 事件保留天数
     */
    private Integer retentionDays = 365;

    /**
     * 快照间隔（事件数量）
     */
    private Integer snapshotInterval = 100;

    /**
     * 是否启用快照
     */
    private Boolean snapshotEnabled = true;

    /**
     * 是否启用事件订阅
     */
    private Boolean subscriptionEnabled = true;

    /**
     * 事件发布类型
     */
    private String publishType = "signal"; // signal, mq, redis

    /**
     * 是否启用事件审计
     */
    private Boolean auditEnabled = true;

    /**
     * 事件审计保留天数
     */
    private Integer auditRetentionDays = 90;

    // Getters and Setters
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public Integer getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(Integer retentionDays) {
        this.retentionDays = retentionDays;
    }

    public Integer getSnapshotInterval() {
        return snapshotInterval;
    }

    public void setSnapshotInterval(Integer snapshotInterval) {
        this.snapshotInterval = snapshotInterval;
    }

    public Boolean getSnapshotEnabled() {
        return snapshotEnabled;
    }

    public void setSnapshotEnabled(Boolean snapshotEnabled) {
        this.snapshotEnabled = snapshotEnabled;
    }

    public Boolean getSubscriptionEnabled() {
        return subscriptionEnabled;
    }

    public void setSubscriptionEnabled(Boolean subscriptionEnabled) {
        this.subscriptionEnabled = subscriptionEnabled;
    }

    public String getPublishType() {
        return publishType;
    }

    public void setPublishType(String publishType) {
        this.publishType = publishType;
    }

    public Boolean getAuditEnabled() {
        return auditEnabled;
    }

    public void setAuditEnabled(Boolean auditEnabled) {
        this.auditEnabled = auditEnabled;
    }

    public Integer getAuditRetentionDays() {
        return auditRetentionDays;
    }

    public void setAuditRetentionDays(Integer auditRetentionDays) {
        this.auditRetentionDays = auditRetentionDays;
    }
}
