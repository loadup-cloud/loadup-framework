package io.github.loadup.testify.test.service;

/*-
 * #%L
 * Testify Demo
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.github.loadup.testify.test.model.Order;
import io.github.loadup.testify.test.model.User;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * User service for demo - demonstrates integration testing with Testify.
 */
@Service
public class UserService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private OrderService orderService;

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Create a new user.
     */
    public User createUser(User user) {
        LocalDateTime createdAt = LocalDateTime.now();
        //        if(user.getUserId().equals("Exception-123")){
        //            throw new IllegalArgumentException("Simulated exception for userId: " +
        // user.getUserId());
        //        }

        String sql = "INSERT INTO users (user_id, user_name, email, status, created_at) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getUserId(), user.getUserName(), user.getEmail(), "ACTIVE", createdAt);

        System.out.println(">>> [EXEC] OrderService instance: " + System.identityHashCode(orderService));
        Order order = orderService.createOrder(user.getUserId(), user.getUserName());
        user.setOrder(order);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    /**
     * Get user by ID.
     */
    public User getUserById(String userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        User user1 = jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> {
                    User user = new User();
                    user.setUserId(rs.getString("user_id"));
                    user.setUserName(rs.getString("user_name"));
                    user.setEmail(rs.getString("email"));
                    user.setStatus(rs.getString("status"));
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return user;
                },
                userId);
        return user1;
    }

    /**
     * Update user status.
     */
    public void updateUserStatus(String userId, String newStatus) {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, newStatus, userId);
    }

    /**
     * Delete user.
     */
    public void deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }
}
