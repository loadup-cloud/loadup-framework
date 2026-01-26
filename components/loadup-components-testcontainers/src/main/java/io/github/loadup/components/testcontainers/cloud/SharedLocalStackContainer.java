/*
 * Copyright (c) 2026 LoadUp Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.loadup.components.testcontainers.cloud;

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

  /** Default LocalStack version to use */
  public static final String DEFAULT_LOCALSTACK_VERSION = "localstack/localstack:3.0";

  /** Default access key for LocalStack */
  private static final String ACCESS_KEY = "test";

  /** Default secret key for LocalStack */
  private static final String SECRET_KEY = "test";

  /** Default region */
  private static final String REGION = "us-east-1";

  /** Enable flag for TestContainers */
  private static final boolean ENABLED;

  /** The shared LocalStack container instance */
  private static final LocalStackContainer LOCALSTACK_CONTAINER;

  /** S3 endpoint URL */
  private static final String S3_ENDPOINT;

  static {
    // Check if TestContainers is enabled (global switch AND individual switch)
    boolean globalEnabled =
        Boolean.parseBoolean(System.getProperty("loadup.testcontainers.enabled", "true"));
    boolean localstackEnabled =
        Boolean.parseBoolean(
            System.getProperty("loadup.testcontainers.localstack.enabled", "true"));

    ENABLED = globalEnabled && localstackEnabled;

    if (ENABLED) {
      // Read configuration from system properties or use defaults
      String localstackVersion =
          System.getProperty("testcontainers.localstack.version", DEFAULT_LOCALSTACK_VERSION);

      log.info("Initializing shared LocalStack TestContainer with version: {}", localstackVersion);

      LOCALSTACK_CONTAINER =
          new LocalStackContainer(DockerImageName.parse(localstackVersion)).withReuse(true);

      LOCALSTACK_CONTAINER.start();

      S3_ENDPOINT = LOCALSTACK_CONTAINER.getEndpoint().toString();

      log.info("Shared LocalStack TestContainer started successfully");
      log.info("S3 Endpoint: {}", S3_ENDPOINT);
      log.info("Access Key: {}", ACCESS_KEY);
      log.info("Region: {}", REGION);

      // Add shutdown hook to stop the container when JVM exits
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    log.info("Stopping shared LocalStack TestContainer");
                    LOCALSTACK_CONTAINER.stop();
                  }));
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

  /** Private constructor to prevent instantiation */
  private SharedLocalStackContainer() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }
}
