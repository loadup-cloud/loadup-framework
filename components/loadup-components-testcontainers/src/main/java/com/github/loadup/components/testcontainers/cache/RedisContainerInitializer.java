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

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Spring Boot test initializer for Redis TestContainer.
 *
 * <p>This initializer configures Spring Boot test context to use the shared Redis container. It
 * automatically sets the necessary Redis connection properties.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;SpringBootTest
 * &#64;ContextConfiguration(initializers = RedisContainerInitializer.class)
 * class MyIntegrationTest {
 *     &#64;Autowired
 *     private RedisTemplate redisTemplate;
 *
 *     &#64;Test
 *     void testRedisConnection() {
 *         // Your test code here
 *     }
 * }
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class RedisContainerInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  /**
   * Initialize the application context with Redis container properties.
   *
   * @param applicationContext the application context to initialize
   */
  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    TestPropertyValues.of(
            "spring.redis.host=" + SharedRedisContainer.getHost(),
            "spring.redis.port=" + SharedRedisContainer.getPort(),
            "spring.data.redis.host=" + SharedRedisContainer.getHost(),
            "spring.data.redis.port=" + SharedRedisContainer.getPort(),
            "loadup.cache.redis.host=" + SharedRedisContainer.getHost(),
            "loadup.cache.redis.port=" + SharedRedisContainer.getPort())
        .applyTo(applicationContext.getEnvironment());
  }
}
