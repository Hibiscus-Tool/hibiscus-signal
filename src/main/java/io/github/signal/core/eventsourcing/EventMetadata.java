package io.github.signal.core.eventsourcing;

import java.util.HashMap;
import java.util.Map;

/**
 * 事件元数据
 * 包含事件的额外信息，如用户ID、会话ID、IP地址等
 */
public class EventMetadata {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 额外的元数据
     */
    private Map<String, Object> additionalData;

    public EventMetadata() {
        this.additionalData = new HashMap<>();
    }

    public EventMetadata(String userId, String sessionId) {
        this();
        this.userId = userId;
        this.sessionId = sessionId;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }

    /**
     * 添加额外的元数据
     */
    public void addMetadata(String key, Object value) {
        this.additionalData.put(key, value);
    }

    /**
     * 获取额外的元数据
     */
    public Object getMetadata(String key) {
        return this.additionalData.get(key);
    }

    /**
     * 检查是否包含指定的元数据
     */
    public boolean hasMetadata(String key) {
        return this.additionalData.containsKey(key);
    }
}
