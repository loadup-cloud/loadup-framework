package io.github.loadup.components.testcontainers.database;

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

import io.github.loadup.components.testcontainers.config.TestContainersProperties.ContainerConfig;
import java.util.concurrent.atomic.AtomicBoolean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Shared PostgreSQL TestContainer instance that can be reused across multiple tests.
 *
 * <p>This class provides a singleton PostgreSQL container that starts once and is shared across all
 * test classes that use it. This significantly reduces test execution time by avoiding the overhead
 * of starting a new PostgreSQL container for each test class.
 *
 * <p>Usage example:
 *
 * <pre>
 * String jdbcUrl = SharedPostgreSQLContainer.getJdbcUrl();
 * String username = SharedPostgreSQLContainer.getUsername();
 * String password = SharedPostgreSQLContainer.getPassword();
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public final class SharedPostgreSQLContainer {
    private static final Logger log = LoggerFactory.getLogger(SharedPostgreSQLContainer.class);


    /**
     * Default PostgreSQL version to use
     */
    public static final String DEFAULT_POSTGRES_VERSION = "postgres:15-alpine";

    /**
     * Default database name
     */
    private static final String DEFAULT_DATABASE_NAME = "testdb";

    /**
     * Default username
     */
    private static final String DEFAULT_USERNAME = "test";

    /**
     * Default password
     */
    private static final String DEFAULT_PASSWORD = "test";

    /**
     * JDBC driver class name
     */
    private static final String DRIVER_CLASS_NAME = "org.postgresql.Driver";

    /**
     * Enable flag for TestContainers
     */
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    /**
     * The shared PostgreSQL container instance
     */
    private static PostgreSQLContainer postgresContainer;

    /**
     * PostgreSQL JDBC URL for the shared container
     */
    private static String jdbcUrl;

    /**
     * PostgreSQL username for the shared container
     */
    private static String username;

    /**
     * PostgreSQL password for the shared container
     */
    private static String password;

    /**
     * PostgreSQL database name
     */
    private static String databaseName;

    /**
     * PostgreSQL host
     */
    private static String host;

    /**
     * PostgreSQL port
     */
    private static Integer port;

    public static void startContainer(ContainerConfig config) {
        if (STARTED.get()) {
            return;
        }

        synchronized (SharedPostgreSQLContainer.class) {
            if (STARTED.get()) {
                return;
            }

            String imageName = (config.getImage() != null) ? config.getImage() : DEFAULT_POSTGRES_VERSION;

            log.info("🚀 Starting Shared PostgreSQL TestContainer: {}", imageName);

            postgresContainer = new PostgreSQLContainer(DockerImageName.parse(imageName))
                    .withDatabaseName(getValue(config.getDatabase(), DEFAULT_DATABASE_NAME))
                    .withUsername(getValue(config.getUsername(), DEFAULT_USERNAME))
                    .withPassword(getValue(config.getPassword(), DEFAULT_PASSWORD))
                    .withReuse(config.isReusable());

            postgresContainer.start();
            STARTED.set(true);

            jdbcUrl = postgresContainer.getJdbcUrl();
            username = postgresContainer.getUsername();
            password = postgresContainer.getPassword();
            databaseName = postgresContainer.getDatabaseName();
            host = postgresContainer.getHost();
            port = postgresContainer.getFirstMappedPort();
            log.info("✅ PostgreSQL Container started at: {}", postgresContainer.getJdbcUrl());

            // Register shutdown hook if reuse is disabled
            if (config.isReusable()) {
                log.info("♻️ Reuse is enabled. Container will persist after JVM exits.");
            } else {
                log.info("Reuse is disabled. Registering shutdown hook to stop container.");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (postgresContainer != null) {
                        log.info("🛑 Stopping PostgreSQL TestContainer...");
                        postgresContainer.stop();
                    }
                }));
            }
        }
    }

    private static String getValue(String value, String defaultValue) {
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    /**
     * Get the shared PostgreSQL container instance. This method triggers the static initialization if
     * not already done.
     *
     * @return the shared PostgreSQL container instance
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static PostgreSQLContainer getInstance() {
        checkStarted();
        return postgresContainer;
    }

    /**
     * Get the PostgreSQL JDBC URL.
     *
     * @return the JDBC URL
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static String getJdbcUrl() {
        checkStarted();
        return jdbcUrl;
    }

    /**
     * Get the PostgreSQL username.
     *
     * @return the username
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static String getUsername() {
        checkStarted();
        return username;
    }

    /**
     * Get the PostgreSQL password.
     *
     * @return the password
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static String getPassword() {
        checkStarted();
        return password;
    }

    /**
     * Get the PostgreSQL database name.
     *
     * @return the database name
     */
    public static String getDatabaseName() {
        return databaseName;
    }

    /**
     * Get the PostgreSQL JDBC driver class name.
     *
     * @return the driver class name
     */
    public static String getDriverClassName() {
        return DRIVER_CLASS_NAME;
    }

    /**
     * Get the PostgreSQL host.
     *
     * @return the host
     */
    public static String getHost() {
        return host;
    }

    /**
     * Get the PostgreSQL mapped port.
     *
     * @return the mapped port
     */
    public static Integer getMappedPort() {
        return port;
    }

    /**
     * Get all container properties as a Map for Spring environment injection.
     *
     * @return map of property names to values
     */
    public static java.util.Map<String, String> getProperties() {
        return java.util.Map.of(
                "spring.datasource.url", getJdbcUrl(),
                "spring.datasource.username", getUsername(),
                "spring.datasource.password", getPassword(),
                "spring.datasource.driver-class-name", getDriverClassName());
    }

    /**
     * Private constructor to prevent instantiation
     */
    private SharedPostgreSQLContainer() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean isStarted() {
        return STARTED.get();
    }

    private static void checkStarted() {
        if (!STARTED.get()) {
            throw new IllegalStateException("MySQL Container has not been started yet!");
        }
    }
}
