package io.github.loadup.components.cache.test.redis;

/*-
 * #%L
 * loadup-components-cache-test
 * %%
 * Copyright (C) 2025 LoadUp Cloud
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

import io.github.loadup.components.cache.binding.CacheBinding;
import io.github.loadup.components.cache.test.TestApplication;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.framework.api.annotation.BindingClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

/** Base test class for cache tests */
@Slf4j
@SpringBootTest(classes = TestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableTestContainers(ContainerType.REDIS)
public abstract class BaseRedisCacheTest {
    @BindingClient("redis-biz-type")
    protected CacheBinding redisBinding;

    protected static final String TEST_CACHE_NAME = "test-cache";
    protected static final String TEST_KEY = "test-key";
    protected static final String TEST_VALUE = "test-value";

    @BeforeEach
    public void setUp() {
        log.info("Setting up test: {}", this.getClass().getSimpleName());
        clearCache();
    }

    @AfterEach
    public void tearDown() {
        log.info("Tearing down test: {}", this.getClass().getSimpleName());
        clearCache();
    }

    protected void clearCache() {
        try {
        } catch (Exception e) {
            log.warn("Failed to clear cache: {}", e.getMessage());
        }
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
