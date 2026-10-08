package io.github.loadup.components.testcontainers.messaging;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;

/**
 * Abstract base test class that automatically configures Kafka TestContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@EnableTestContainers(ContainerType.KAFKA)
public abstract class AbstractKafkaContainerTest {

    protected String getBootstrapServers() {
        return SharedKafkaContainer.getBootstrapServers();
    }

    protected String getHost() {
        return SharedKafkaContainer.getHost();
    }

    protected Integer getPort() {
        return SharedKafkaContainer.getPort();
    }
}
