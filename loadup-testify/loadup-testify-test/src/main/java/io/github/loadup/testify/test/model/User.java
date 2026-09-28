package io.github.loadup.testify.test.model;

import java.time.LocalDateTime;

/**
 * User entity for demo.
 */
public class User {
    private String userId;
    private String userName;
    private String email;
    private String status;
    private LocalDateTime createdAt;
    private Order order;

    public User() {}

    public User(String userId, String userName, String email) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Order getOrder() {
        return order;
    }

    public User setOrder(Order order) {
        this.order = order;
        return this;
    }
}
