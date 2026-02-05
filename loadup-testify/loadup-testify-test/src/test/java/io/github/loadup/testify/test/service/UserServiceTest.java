package io.github.loadup.testify.test.service;

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

import io.github.loadup.testify.core.annotation.TestifyParam;
import io.github.loadup.testify.test.model.User;
import io.github.loadup.testify.starter.base.TestifyBase;
import io.github.loadup.testify.test.TestApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;

/**
 * Integration test for UserService demonstrating YAML-driven testing with Testify.
 *
 * <p>This test uses: - YAML-defined test data and variables - Automatic database setup and cleanup
 * - Database assertions with rich diff reporting - Zero-code test logic (just method signature)
 */
@SpringBootTest(classes = TestApplication.class)
public class UserServiceTest extends TestifyBase {

    @Autowired
    private UserService userService;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    /**
     * YAML-driven test for createUser method. Test data, setup, and assertions are defined in
     * testCreateUser.yaml
     */
    @Test(dataProvider = "testifyData")
    public void testCreateUserWithParam(@TestifyParam("users") User user) {
        runTest(() -> userService.createUser(user));
    }

    @Test(dataProvider = "testifyData")
    public void testCreateUserWithOutParam(User user) {
        runTest(() -> userService.createUser(user));
    }

    @Test(dataProvider = "testifyData")
    public void testCreateUserWithException(User user) {
        runTest(() -> userService.createUser(user));
    }

    @Test(dataProvider = "testifyData")
    public void testGetUserWithSetup(String id) {
        runTest(() -> userService.getUserById(id));
    }

    @Test(dataProvider = "testifyData")
    public void testGetUserWithCsvSetup(String id) {
        runTest(() -> userService.getUserById(id));
    }

    @Test(dataProvider = "testifyData")
    public void testGetUserWithMultiRows(String id) {
        runTest(() -> userService.getUserById(id));
    }

    //    /**
    //     * Traditional test for updateUserStatus (can coexist with YAML tests).
    //     */
    //    @Test
    //    public void testUpdateUserStatus() {
    //        // Arrange
    //        String userId = "test-user-update-456";
    //
    //        // Cleanup and setup
    //        jdbcTemplate.execute("DELETE FROM users WHERE user_id = '" + userId + "'");
    //        jdbcTemplate.update(
    //                "INSERT INTO users (user_id, user_name, email, status, created_at) VALUES (?, ?,
    // ?, ?, ?)",
    //                userId,
    //                "Jane Smith",
    //                "jane@example.com",
    //                "ACTIVE",
    //                System.currentTimeMillis());
    //
    //        // Act
    //        userService.updateUserStatus(userId, "INACTIVE");
    //
    //        // Assert
    //        String status =
    //                jdbcTemplate.queryForObject(
    //                        "SELECT status FROM users WHERE user_id = ?", String.class, userId);
    //        assertEquals(status, "INACTIVE", "Status should be updated to INACTIVE");
    //    }
    //
    //    /**
    //     * Traditional test for deleteUser.
    //     */
    //    @Test
    //    public void testDeleteUser() {
    //        // Arrange
    //        String userId = "test-user-delete-789";
    //
    //        // Setup
    //        jdbcTemplate.update(
    //                "INSERT INTO users (user_id, user_name, email, status, created_at) VALUES (?, ?,
    // ?, ?, ?)",
    //                userId,
    //                "Bob Wilson",
    //                "bob@example.com",
    //                "ACTIVE",
    //                System.currentTimeMillis());
    //
    //        // Act
    //        userService.deleteUser(userId);
    //
    //        // Assert
    //        Integer count =
    //                jdbcTemplate.queryForObject(
    //                        "SELECT COUNT(*) FROM users WHERE user_id = ?", Integer.class, userId);
    //        assertEquals(count, 0, "User should be deleted");
    //    }
}
