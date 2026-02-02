package io.github.loadup.testify.demo.service;

/*-
 * #%L
 * Testify Demo
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.testify.demo.model.Order;
import io.github.loadup.testify.demo.model.User;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** User service for demo - demonstrates integration testing with Testify. */
@Service
public class UserService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private OrderService orderService;

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Create a new user. */
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

    /** Get user by ID. */
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

    /** Update user status. */
    public void updateUserStatus(String userId, String newStatus) {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, newStatus, userId);
    }

    /** Delete user. */
    public void deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }
}
