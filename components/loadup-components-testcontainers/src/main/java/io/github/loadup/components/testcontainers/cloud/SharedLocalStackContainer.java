package io.github.loadup.components.testcontainers.cloud;

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

import io.github.loadup.components.testcontainers.config.TestContainersProperties;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared LocalStack TestContainer instance for S3 testing.
 *
 * <p>This class provides a singleton LocalStack container that starts once and is shared across all
 * test classes that use it. LocalStack emulates AWS services including S3, allowing for local
 * testing without connecting to real AWS services.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;SpringBootTest
 * &#64;TestPropertySource(properties = {
 *     "aws.s3.endpoint=" + SharedLocalStackContainer.getS3Endpoint(),
 *     "aws.access-key-id=" + SharedLocalStackContainer.ACCESS_KEY,
 *     "aws.secret-access-key=" + SharedLocalStackContainer.SECRET_KEY
 * })
 * class MyS3Test {
 *     // Your test code here
 * }
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
public class SharedLocalStackContainer {

    /**
     * Default LocalStack version to use
     */
    public static final String DEFAULT_LOCALSTACK_VERSION = "localstack/localstack:3.0";

    /**
     * Default access key for LocalStack
     */
    private static String ACCESS_KEY = "test";

    /**
     * Default secret key for LocalStack
     */
    private static String SECRET_KEY = "test";

    /**
     * Default region
     */
    private static String REGION = "us-east-1";

    /**
     * Enable flag for TestContainers
     */
    private static final boolean ENABLED;

    /**
     * The shared LocalStack container instance
     */
    private static LocalStackContainer LOCALSTACK_CONTAINER;

    /**
     * S3 endpoint URL
     */
    private static String S3_ENDPOINT;

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    static {
        // Check if TestContainers is enabled (global switch AND individual switch)
        boolean globalEnabled = Boolean.parseBoolean(System.getProperty("loadup.testcontainers.enabled", "true"));
        boolean localstackEnabled =
                Boolean.parseBoolean(System.getProperty("loadup.testcontainers.localstack.enabled", "true"));

        ENABLED = globalEnabled && localstackEnabled;

        if (ENABLED) {
            // Read configuration from system properties or use defaults

        } else {
            log.info("LocalStack TestContainer is DISABLED. Using real AWS S3 from configuration.");
            LOCALSTACK_CONTAINER = null;
            S3_ENDPOINT = null;
        }
    }

    /**
     * Check if LocalStack TestContainer is enabled.
     *
     * @return true if enabled, false otherwise
     */
    public static boolean isEnabled() {
        return ENABLED;
    }

    /**
     * Get the shared LocalStack container instance. This method triggers the static initialization if
     * not already done.
     *
     * @return the shared LocalStack container instance
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static LocalStackContainer getInstance() {
        if (!ENABLED) {
            throw new IllegalStateException(
                    "LocalStack TestContainer is disabled. Please configure real AWS S3 in application.yml");
        }
        return LOCALSTACK_CONTAINER;
    }

    /**
     * Get the S3 endpoint URL.
     *
     * @return the S3 endpoint URL
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static String getS3Endpoint() {

        return S3_ENDPOINT;
    }

    /**
     * Get the access key.
     *
     * @return the access key
     */
    public static String getAccessKey() {
        return ACCESS_KEY;
    }

    /**
     * Get the secret key.
     *
     * @return the secret key
     */
    public static String getSecretKey() {
        return SECRET_KEY;
    }

    /**
     * Get the region.
     *
     * @return the region
     */
    public static String getRegion() {
        return REGION;
    }

    /**
     * Private constructor to prevent instantiation
     */
    private SharedLocalStackContainer() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void startContainer(TestContainersProperties.ContainerConfig config) {
        if (STARTED.get()) {
            return;
        }
        synchronized (SharedLocalStackContainer.class) {
            if (STARTED.get()) return;
            String image = (config.getImage() != null) ? config.getImage() : DEFAULT_LOCALSTACK_VERSION;
            LOCALSTACK_CONTAINER = new LocalStackContainer(DockerImageName.parse(image)).withReuse(true);

            LOCALSTACK_CONTAINER.start();
            STARTED.set(true);

            S3_ENDPOINT = LOCALSTACK_CONTAINER.getEndpoint().toString();
            ACCESS_KEY = LOCALSTACK_CONTAINER.getAccessKey();
            SECRET_KEY = LOCALSTACK_CONTAINER.getSecretKey();
            REGION = LOCALSTACK_CONTAINER.getRegion();

            log.info("S3 Endpoint: {}", S3_ENDPOINT);
            log.info("Access Key: {}", ACCESS_KEY);
            log.info("Secret Key: {}", SECRET_KEY);
            log.info("Region: {}", REGION);
            if (!config.isReusable()) {
                log.info("Reuse is disabled. Registering shutdown hook to stop container.");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (LOCALSTACK_CONTAINER != null) {
                        log.info("🛑 Stopping LocalStack TestContainer...");
                        LOCALSTACK_CONTAINER.stop();
                    }
                }));
            } else {
                log.info("♻️ Reuse is enabled. Container will persist after JVM exits.");
            }
        }
    }

    public static Map<String, String> getProperties() {

        return Map.of(
                "aws.s3.endpoint",
                SharedLocalStackContainer.getS3Endpoint(),
                "aws.access-key-id",
                SharedLocalStackContainer.getAccessKey(),
                "aws.secret-access-key",
                SharedLocalStackContainer.getSecretKey(),
                "aws.region",
                SharedLocalStackContainer.getRegion(),
                "spring.cloud.aws.credentials.access-key",
                SharedLocalStackContainer.getAccessKey(),
                "spring.cloud.aws.credentials.secret-key",
                SharedLocalStackContainer.getSecretKey(),
                "spring.cloud.aws.s3.endpoint",
                SharedLocalStackContainer.getS3Endpoint(),
                "spring.cloud.aws.region.static",
                SharedLocalStackContainer.getRegion());
    }
}
