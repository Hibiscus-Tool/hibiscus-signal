package io.github.signal.core.eventsourcing.example;

import io.github.signal.core.eventsourcing.Event;

/**
 * 用户创建事件
 */
public class UserCreatedEvent extends Event {

    private String username;
    private String email;

    public UserCreatedEvent() {
        super();
    }

    public UserCreatedEvent(String username, String email) {
        super();
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
