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
package com.github.loadup.components.testcontainers.search;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Elasticsearch TestContainer instance that can be reused across multiple tests.
 *
 * <p>This class provides a singleton Elasticsearch container that starts once and is shared across
 * all test classes that use it.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
public class SharedElasticsearchContainer {

  /** Default Elasticsearch version to use */
  public static final String DEFAULT_ELASTICSEARCH_VERSION =
      "docker.elastic.co/elasticsearch/elasticsearch:8.11.0";

  /** The shared Elasticsearch container instance */
  private static final ElasticsearchContainer ELASTICSEARCH_CONTAINER;

  /** Elasticsearch HTTP host URL */
  public static final String HTTP_HOST_ADDRESS;

  /** Elasticsearch host */
  public static final String HOST;

  /** Elasticsearch port */
  public static final Integer PORT;

  static {
    // Read configuration from system properties or use defaults
    String elasticsearchVersion =
        System.getProperty("testcontainers.elasticsearch.version", DEFAULT_ELASTICSEARCH_VERSION);

    log.info(
        "Initializing shared Elasticsearch TestContainer with version: {}", elasticsearchVersion);

    ELASTICSEARCH_CONTAINER =
        new ElasticsearchContainer(DockerImageName.parse(elasticsearchVersion))
            .withEnv("xpack.security.enabled", "false")
            .withEnv("xpack.security.http.ssl.enabled", "false")
            .withReuse(true);

    ELASTICSEARCH_CONTAINER.start();

    HTTP_HOST_ADDRESS = ELASTICSEARCH_CONTAINER.getHttpHostAddress();
    HOST = ELASTICSEARCH_CONTAINER.getHost();
    PORT = ELASTICSEARCH_CONTAINER.getFirstMappedPort();

    log.info("Shared Elasticsearch TestContainer started successfully");
    log.info("HTTP Host Address: {}", HTTP_HOST_ADDRESS);
    log.info("Host: {}", HOST);
    log.info("Port: {}", PORT);

    // Add shutdown hook
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  log.info("Stopping shared Elasticsearch TestContainer");
                  ELASTICSEARCH_CONTAINER.stop();
                }));
  }

  public static ElasticsearchContainer getInstance() {
    return ELASTICSEARCH_CONTAINER;
  }

  public static String getHttpHostAddress() {
    return HTTP_HOST_ADDRESS;
  }

  public static String getHost() {
    return HOST;
  }

  public static Integer getPort() {
    return PORT;
  }

  private SharedElasticsearchContainer() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }
}
