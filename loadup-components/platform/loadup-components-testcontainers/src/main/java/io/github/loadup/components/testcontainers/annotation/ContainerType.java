package io.github.loadup.components.testcontainers.annotation;

/**
 * Enumeration of supported testcontainer types.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public enum ContainerType {
    /**
     * MySQL database container
     */
    MYSQL,

    /**
     * PostgreSQL database container
     */
    POSTGRESQL,

    /**
     * MongoDB database container
     */
    MONGODB,

    /**
     * Redis cache container
     */
    REDIS,

    /**
     * Kafka messaging container
     */
    KAFKA,

    /**
     * Elasticsearch search container
     */
    ELASTICSEARCH,

    /**
     * LocalStack AWS services container
     */
    LOCALSTACK
}
