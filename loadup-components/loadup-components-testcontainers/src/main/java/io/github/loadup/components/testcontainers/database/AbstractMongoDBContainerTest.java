package io.github.loadup.components.testcontainers.database;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;

/**
 * Abstract base test class that automatically configures MongoDB TestContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@EnableTestContainers(ContainerType.MONGODB)
public abstract class AbstractMongoDBContainerTest {

    protected String getConnectionString() {
        return SharedMongoDBContainer.getConnectionString();
    }

    protected String getHost() {
        return SharedMongoDBContainer.getHost();
    }

    protected Integer getPort() {
        return SharedMongoDBContainer.getMappedPort();
    }

    protected String getReplicaSetUrl() {
        return SharedMongoDBContainer.getReplicaSetUrl();
    }
}
