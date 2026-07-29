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
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Shared MySQL TestContainer instance that can be reused across multiple tests.
 *
 * <p>This class provides a singleton MySQL container that starts once and is shared across all test
 * classes that use it. This significantly reduces test execution time by avoiding the overhead of
 * starting a new MySQL container for each test class.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;SpringBootTest
 * &#64;TestPropertySource(properties = {
    private static final Logger log = LoggerFactory.getLogger(SharedMySQLContainer.class);

 *     "spring.datasource.url=" + SharedMySQLContainer.JDBC_URL,
 *     "spring.datasource.username=" + SharedMySQLContainer.USERNAME,
 *     "spring.datasource.password=" + SharedMySQLContainer.PASSWORD
 * })
 * class MyIntegrationTest {
 *     // Your test code here
 * }
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public final class SharedMySQLContainer {

    /**
     * Default MySQL version to use
     */
    public static final String DEFAULT_MYSQL_VERSION = "mysql:8.0";

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
     * The shared MySQL container instance
     */
    private static MySQLContainer mysqlContainer;

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    /**
     * JDBC URL for the shared MySQL container
     */
    private static String jdbcUrl;

    /**
     * Username for the shared MySQL container
     */
    private static String username;

    /**
     * Password for the shared MySQL container
     */
    private static String password;

    /**
     * Database name for the shared MySQL container
     */
    private static String databaseName;

    /**
     * JDBC driver class name
     */
    public static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

    /**
     * 初始化并启动容器
     *
     * @param config 来自 Spring 环境变量的配置对象
     */
    public static void startContainer(ContainerConfig config) {
        if (STARTED.get()) {
            return;
        }

        synchronized (SharedMySQLContainer.class) {
            if (STARTED.get()) {
                return;
            }

            ContainerConfig effectiveConfig = (config != null) ? config : new ContainerConfig();

            String imageName =
                    (effectiveConfig.getImage() != null) ? effectiveConfig.getImage() : DEFAULT_MYSQL_VERSION;

            log.info("🚀 Starting Shared MySQL TestContainer: {}", imageName);

            mysqlContainer = new MySQLContainer(DockerImageName.parse(imageName))
                    .withDatabaseName(getValue(effectiveConfig.getDatabase(), DEFAULT_DATABASE_NAME))
                    .withUsername(getValue(effectiveConfig.getUsername(), DEFAULT_USERNAME))
                    .withPassword(getValue(effectiveConfig.getPassword(), DEFAULT_PASSWORD))
                    .withCommand("--max-allowed-packet=268435456")
                    .withReuse(effectiveConfig.isReusable());

            mysqlContainer.start();
            STARTED.set(true);
            jdbcUrl = mysqlContainer.getJdbcUrl();
            username = mysqlContainer.getUsername();
            password = mysqlContainer.getPassword();
            databaseName = mysqlContainer.getDatabaseName();

            log.info("✅ MySQL Container started at: {}", mysqlContainer.getJdbcUrl());

            // Register shutdown hook if reuse is disabled
            if (effectiveConfig.isReusable()) {
                log.info("♻️ Reuse is enabled. Container will persist after JVM exits.");
            } else {
                log.info("Reuse is disabled. Registering shutdown hook to stop container.");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (mysqlContainer != null) {
                        log.info("🛑 Stopping MySQL TestContainer...");
                        mysqlContainer.stop();
                    }
                }));
            }
        }
    }

    private static String getValue(String value, String defaultValue) {
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    /**
     * Get the shared MySQL container instance. This method triggers the static initialization if not
     * already done.
     *
     * @return the shared MySQL container instance
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static MySQLContainer getInstance() {
        return mysqlContainer;
    }

    /**
     * Get the JDBC URL for the shared MySQL container.
     *
     * @return the JDBC URL
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static String getJdbcUrl() {
        checkStarted();
        return jdbcUrl;
    }

    /**
     * Get the username for the shared MySQL container.
     *
     * @return the username
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static String getUsername() {
        checkStarted();
        return username;
    }

    /**
     * Get the password for the shared MySQL container.
     *
     * @return the password
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static String getPassword() {
        checkStarted();
        return password;
    }

    /**
     * Get the database name for the shared MySQL container.
     *
     * @return the database name
     */
    public static String getDatabaseName() {
        return databaseName;
    }

    /**
     * Get the JDBC driver class name.
     *
     * @return the driver class name
     */
    public static String getDriverClassName() {
        return DRIVER_CLASS_NAME;
    }

    /**
     * Get the mapped port for MySQL.
     *
     * @return the mapped port
     */
    public static Integer getMappedPort() {
        return mysqlContainer.getMappedPort(MySQLContainer.MYSQL_PORT);
    }

    /**
     * Get the host for the MySQL container.
     *
     * @return the host
     */
    public static String getHost() {
        return mysqlContainer.getHost();
    }

    /**
     * Get all container properties as a Map for Spring environment injection.
     *
     * @return map of property names to values
     */
    public static Map<String, String> getProperties() {
        return Map.of(
                "spring.datasource.url", getJdbcUrl(),
                "spring.datasource.username", getUsername(),
                "spring.datasource.password", getPassword(),
                "spring.datasource.driver-class-name", getDriverClassName());
    }

    /**
     * Private constructor to prevent instantiation
     */
    private SharedMySQLContainer() {
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
