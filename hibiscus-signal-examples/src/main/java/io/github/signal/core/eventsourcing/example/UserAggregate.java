package io.github.signal.core.eventsourcing.example;

import io.github.signal.core.eventsourcing.AggregateRoot;
import io.github.signal.core.eventsourcing.Event;

import java.time.LocalDateTime;

/**
 * 用户聚合根示例
 * 演示如何使用事件溯源模式
 */
public class UserAggregate extends AggregateRoot {

    private String username;
    private String email;
    private String status;
    private LocalDateTime lastLoginTime;
    private int loginCount;

    public UserAggregate() {
        super();
    }

    public UserAggregate(String id) {
        super(id, 0);
    }

    /**
     * 创建用户
     */
    public void createUser(String username, String email) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }

        UserCreatedEvent event = new UserCreatedEvent(username, email);
        applyAndRecord(event);
    }

    /**
     * 更新用户信息
     */
    public void updateUser(String username, String email) {
        if (username != null && !username.trim().isEmpty()) {
            UserUpdatedEvent event = new UserUpdatedEvent(username, email);
            applyAndRecord(event);
        }
    }

    /**
     * 用户登录
     */
    public void userLogin() {
        UserLoginEvent event = new UserLoginEvent();
        applyAndRecord(event);
    }

    /**
     * 用户登出
     */
    public void userLogout() {
        UserLogoutEvent event = new UserLogoutEvent();
        applyAndRecord(event);
    }

    /**
     * 删除用户
     */
    public void deleteUser() {
        UserDeletedEvent event = new UserDeletedEvent();
        applyAndRecord(event);
    }

    @Override
    protected void apply(Event event) {
        if (event instanceof UserCreatedEvent) {
            apply((UserCreatedEvent) event);
        } else if (event instanceof UserUpdatedEvent) {
            apply((UserUpdatedEvent) event);
        } else if (event instanceof UserLoginEvent) {
            apply((UserLoginEvent) event);
        } else if (event instanceof UserLogoutEvent) {
            apply((UserLogoutEvent) event);
        } else if (event instanceof UserDeletedEvent) {
            apply((UserDeletedEvent) event);
        }
    }

    private void apply(UserCreatedEvent event) {
        this.username = event.getUsername();
        this.email = event.getEmail();
        this.status = "ACTIVE";
        this.loginCount = 0;
    }

    private void apply(UserUpdatedEvent event) {
        if (event.getUsername() != null) {
            this.username = event.getUsername();
        }
        if (event.getEmail() != null) {
            this.email = event.getEmail();
        }
    }

    private void apply(UserLoginEvent event) {
        this.lastLoginTime = LocalDateTime.now();
        this.loginCount++;
        this.status = "ONLINE";
    }

    private void apply(UserLogoutEvent event) {
        this.status = "OFFLINE";
    }

    private void apply(UserDeletedEvent event) {
        this.status = "DELETED";
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public int getLoginCount() {
        return loginCount;
    }

    public boolean isActive() {
        return "ACTIVE".equals(status) || "ONLINE".equals(status);
    }

    public boolean isDeleted() {
        return "DELETED".equals(status);
    }
}
