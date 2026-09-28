package io.github.loadup.components.testcontainers.search;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;

/**
 * Abstract base test class that automatically configures Elasticsearch TestContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@EnableTestContainers(ContainerType.ELASTICSEARCH)
public abstract class AbstractElasticsearchContainerTest {

    protected String getHttpHostAddress() {
        return SharedElasticsearchContainer.getHttpHostAddress();
    }

    protected String getHost() {
        return SharedElasticsearchContainer.getHost();
    }

    protected Integer getPort() {
        return SharedElasticsearchContainer.getPort();
    }
}
