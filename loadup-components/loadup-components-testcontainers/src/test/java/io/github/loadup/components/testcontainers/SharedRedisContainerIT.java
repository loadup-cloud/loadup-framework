package io.github.loadup.components.testcontainers;

/*-
 * #%L
 * Loadup Components TestContainers
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.components.testcontainers.cache.SharedRedisContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;

/**
 * Integration test class for SharedRedisContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@SpringBootTest(classes = TestApplication.class)
@EnableTestContainers(ContainerType.REDIS)
public class SharedRedisContainerIT {
    private static final Logger log = LoggerFactory.getLogger(SharedRedisContainerIT.class);

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
