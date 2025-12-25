/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.cache.common;

import com.github.loadup.components.cache.api.CacheBinding;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Base test class for cache tests
 */
@Slf4j
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class BaseCacheTest {

    @Autowired
    protected CacheBinding cacheBinding;

    protected static final String TEST_CACHE_NAME = "test-cache";
    protected static final String TEST_KEY        = "test-key";
    protected static final String TEST_VALUE      = "test-value";

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
            cacheBinding.deleteAll(TEST_CACHE_NAME);
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

