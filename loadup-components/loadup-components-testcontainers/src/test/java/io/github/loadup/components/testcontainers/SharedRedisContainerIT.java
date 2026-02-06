
package io.github.loadup.components.testcontainers;

/*-
 * #%L
 * Loadup Components TestContainers
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

import static org.junit.jupiter.api.Assertions.*;

import io.github.loadup.components.testcontainers.cache.AbstractRedisContainerTest;
import io.github.loadup.components.testcontainers.cache.SharedRedisContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;

/**
 * Integration test class for SharedRedisContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@ActiveProfiles("test")
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(properties = {"loadup.testcontainers.enabled=true", "loadup.testcontainers.redis.enabled=true"})
class SharedRedisContainerIT extends AbstractRedisContainerTest {

    @Test
    void testContainerIsRunning() {
        GenericContainer<?> container = SharedRedisContainer.getInstance();
        assertNotNull(container, "Container should not be null");
        assertTrue(container.isRunning(), "Container should be running");
    }

    @Test
    void testContainerProperties() {
        assertNotNull(SharedRedisContainer.getHost(), "Host should not be null");
        assertNotNull(SharedRedisContainer.getPort(), "Port should not be null");
        assertNotNull(SharedRedisContainer.getUrl(), "Redis URL should not be null");
        assertNotNull(SharedRedisContainer.getMappedPort(), "Mapped port should not be null");

        log.info("Host: {}", SharedRedisContainer.getHost());
        log.info("Port: {}", SharedRedisContainer.getPort());
        log.info("Redis URL: {}", SharedRedisContainer.getUrl());
    }

    @Test
    void testRedisConnection() {
        String redisUrl = SharedRedisContainer.getUrl();
        RedisClient redisClient = RedisClient.create(redisUrl);

        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            assertNotNull(connection, "Connection should not be null");

            RedisCommands<String, String> commands = connection.sync();
            String pingResponse = commands.ping();
            assertEquals("PONG", pingResponse, "Ping response should be PONG");

            log.info("Successfully connected to Redis");
        } finally {
            redisClient.shutdown();
        }
    }

    @Test
    void testRedisOperations() {
        String redisUrl = SharedRedisContainer.getUrl();
        RedisClient redisClient = RedisClient.create(redisUrl);

        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisCommands<String, String> commands = connection.sync();

            // Set a value
            commands.set("testKey", "testValue");

            // Get the value
            String value = commands.get("testKey");
            assertEquals("testValue", value, "Value should be 'testValue'");

            // Delete the key
            Long deleted = commands.del("testKey");
            assertEquals(1L, deleted, "Should delete 1 key");

            // Verify key is deleted
            String deletedValue = commands.get("testKey");
            assertNull(deletedValue, "Value should be null after deletion");
        } finally {
            redisClient.shutdown();
        }
    }

    @Test
    void testSameContainerAcrossTests() {
        GenericContainer<?> container1 = SharedRedisContainer.getInstance();
        GenericContainer<?> container2 = SharedRedisContainer.getInstance();

        assertSame(container1, container2, "Should return the same container instance");
    }
}
