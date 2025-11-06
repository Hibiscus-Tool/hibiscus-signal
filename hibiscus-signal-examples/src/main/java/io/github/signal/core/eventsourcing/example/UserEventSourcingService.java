package io.github.signal.core.eventsourcing.example;

import io.github.signal.core.eventsourcing.EventSourcingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 用户事件溯源服务
 * 演示如何使用事件溯源模式管理用户状态
 */
@Service
public class UserEventSourcingService {

    private static final Logger log = LoggerFactory.getLogger(UserEventSourcingService.class);

    @Autowired
    private EventSourcingManager eventSourcingManager;

    @Autowired
    private UserAggregateFactory userAggregateFactory;

    /**
     * 创建用户
     */
    public CompletableFuture<String> createUser(String username, String email) {
        UserAggregate user = new UserAggregate();
        user.createUser(username, email);

        return eventSourcingManager.save(user)
                .thenApply(v -> {
                    log.info("用户创建成功: {} - {}", username, user.getId());
                    return user.getId();
                });
    }

    /**
     * 更新用户信息
     */
    public CompletableFuture<Void> updateUser(String userId, String username, String email) {
        return eventSourcingManager.getAggregate(userId, "UserAggregate")
                .thenCompose(user -> {
                    if (user == null) {
                        throw new RuntimeException("用户不存在: " + userId);
                    }
                    UserAggregate userAggregate = (UserAggregate) user;
                    userAggregate.updateUser(username, email);
                    return eventSourcingManager.save(userAggregate);
                })
                .thenRun(() -> log.info("用户信息更新成功: {}", userId));
    }

    /**
     * 用户登录
     */
    public CompletableFuture<Void> userLogin(String userId) {
        return eventSourcingManager.getAggregate(userId, "UserAggregate")
                .thenCompose(user -> {
                    if (user == null) {
                        throw new RuntimeException("用户不存在: " + userId);
                    }
                    UserAggregate userAggregate = (UserAggregate) user;
                    userAggregate.userLogin();
                    return eventSourcingManager.save(userAggregate);
                })
                .thenRun(() -> log.info("用户登录成功: {}", userId));
    }

    /**
     * 用户登出
     */
    public CompletableFuture<Void> userLogout(String userId) {
        return eventSourcingManager.getAggregate(userId, "UserAggregate")
                .thenCompose(user -> {
                    if (user == null) {
                        throw new RuntimeException("用户不存在: " + userId);
                    }
                    UserAggregate userAggregate = (UserAggregate) user;
                    userAggregate.userLogout();
                    return eventSourcingManager.save(userAggregate);
                })
                .thenRun(() -> log.info("用户登出成功: {}", userId));
    }

    /**
     * 删除用户
     */
    public CompletableFuture<Void> deleteUser(String userId) {
        return eventSourcingManager.getAggregate(userId, "UserAggregate")
                .thenCompose(user -> {
                    if (user == null) {
                        throw new RuntimeException("用户不存在: " + userId);
                    }
                    UserAggregate userAggregate = (UserAggregate) user;
                    userAggregate.deleteUser();
                    return eventSourcingManager.save(userAggregate);
                })
                .thenRun(() -> log.info("用户删除成功: {}", userId));
    }

    /**
     * 获取用户信息
     */
    public CompletableFuture<UserAggregate> getUser(String userId) {
        return eventSourcingManager.getAggregate(userId, "UserAggregate");
    }

    /**
     * 获取用户事件历史
     */
    public CompletableFuture<java.util.List<io.github.signal.core.eventsourcing.Event>> getUserEvents(String userId) {
        return eventSourcingManager.getAggregateEvents(userId);
    }

    /**
     * 获取用户事件历史（从指定版本开始）
     */
    public CompletableFuture<java.util.List<io.github.signal.core.eventsourcing.Event>> getUserEvents(String userId, long fromVersion) {
        return eventSourcingManager.getAggregateEvents(userId, fromVersion);
    }

    /**
     * 检查用户是否存在
     */
    public CompletableFuture<Boolean> userExists(String userId) {
        return eventSourcingManager.getAggregate(userId, "UserAggregate")
                .thenApply(user -> user != null);
    }

    /**
     * 获取用户当前版本
     */
    public CompletableFuture<Long> getUserVersion(String userId) {
        return eventSourcingManager.getAggregateVersion(userId);
    }
}
