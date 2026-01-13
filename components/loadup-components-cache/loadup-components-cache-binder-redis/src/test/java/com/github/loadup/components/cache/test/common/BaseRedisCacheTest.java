package com.github.loadup.components.cache.test.common;

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

import com.github.loadup.components.cache.binder.CacheBinding;
import com.github.loadup.components.testcontainers.cache.AbstractRedisContainerTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Base test class for Redis cache tests using shared Redis container */
@Slf4j
@SpringBootTest
public abstract class BaseRedisCacheTest extends AbstractRedisContainerTest {

  @Autowired protected CacheBinding cacheBinding;

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
