package io.github.loadup.components.testcontainers.cache;

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
