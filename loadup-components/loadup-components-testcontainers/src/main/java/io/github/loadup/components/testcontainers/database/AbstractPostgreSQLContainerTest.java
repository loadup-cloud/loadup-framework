package io.github.loadup.components.testcontainers.database;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;

/**
 * Abstract base test class that automatically configures PostgreSQL TestContainer.
 *
 * <p>Test classes can extend this class to automatically use the shared PostgreSQL container
 * without needing to manually configure the context initializer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@EnableTestContainers(ContainerType.POSTGRESQL)
public abstract class AbstractPostgreSQLContainerTest {

    protected String getJdbcUrl() {
        return SharedPostgreSQLContainer.getJdbcUrl();
    }

    protected String getUsername() {
        return SharedPostgreSQLContainer.getUsername();
    }

    protected String getPassword() {
        return SharedPostgreSQLContainer.getPassword();
    }

    protected String getDatabaseName() {
        return SharedPostgreSQLContainer.getDatabaseName();
    }

    protected String getHost() {
        return SharedPostgreSQLContainer.getHost();
    }

    protected Integer getMappedPort() {
        return SharedPostgreSQLContainer.getMappedPort();
    }
}
