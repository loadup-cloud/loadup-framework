package io.github.loadup.components.testcontainers.cache;

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
