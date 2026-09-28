package io.github.loadup.components.testcontainers.database;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;

/**
 * Abstract base test class that automatically configures MySQL TestContainer.
 *
 * <p>Test classes can extend this class to automatically use the shared MySQL container without
 * needing to manually configure the context initializer.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;SpringBootTest
 * class MyIntegrationTest extends AbstractMySQLContainerTest {
 *     &#64;Autowired
 *     private DataSource dataSource;
 *
 *     &#64;Test
 *     void testDatabaseConnection() {
 *         // Your test code here
 *     }
 * }
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@EnableTestContainers(ContainerType.MYSQL)
public abstract class AbstractMySQLContainerTest {

    /**
     * Get the JDBC URL for the shared MySQL container.
     *
     * @return the JDBC URL
     */
    protected String getJdbcUrl() {
        return SharedMySQLContainer.getJdbcUrl();
    }

    /**
     * Get the username for the shared MySQL container.
     *
     * @return the username
     */
    protected String getUsername() {
        return SharedMySQLContainer.getUsername();
    }

    /**
     * Get the password for the shared MySQL container.
     *
     * @return the password
     */
    protected String getPassword() {
        return SharedMySQLContainer.getPassword();
    }

    /**
     * Get the database name for the shared MySQL container.
     *
     * @return the database name
     */
    protected String getDatabaseName() {
        return SharedMySQLContainer.getDatabaseName();
    }
}
