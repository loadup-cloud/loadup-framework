/*
 * Copyright (c) 2026 LoadUp Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.loadup.components.testcontainers.cache;

import org.springframework.test.context.ContextConfiguration;

/**
 * Abstract base test class that automatically configures Redis TestContainer.
 *
 * <p>Test classes can extend this class to automatically use the shared Redis container without
 * needing to manually configure the context initializer.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;SpringBootTest
 * class MyRedisTest extends AbstractRedisContainerTest {
 *     &#64;Autowired
 *     private RedisTemplate redisTemplate;
 *
 *     &#64;Test
 *     void testRedisOperations() {
 *         // Your test code here
 *     }
 * }
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@ContextConfiguration(initializers = RedisContainerInitializer.class)
public abstract class AbstractRedisContainerTest {

  /**
   * Get the Redis host.
   *
   * @return the Redis host
   */
  protected String getRedisHost() {
    return SharedRedisContainer.getHost();
  }

  /**
   * Get the Redis port.
   *
   * @return the Redis port
   */
  protected Integer getRedisPort() {
    return SharedRedisContainer.getPort();
  }

  /**
   * Get the Redis connection URL.
   *
   * @return the Redis URL
   */
  protected String getRedisUrl() {
    return SharedRedisContainer.getUrl();
  }
}
