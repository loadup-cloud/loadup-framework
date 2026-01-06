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
package com.github.loadup.components.testcontainers.database;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared MongoDB TestContainer instance that can be reused across multiple tests.
 *
 * <p>This class provides a singleton MongoDB container that starts once and is shared across all
 * test classes that use it.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
public class SharedMongoDBContainer {

  /** Default MongoDB version to use */
  public static final String DEFAULT_MONGODB_VERSION = "mongo:7.0";

  /** Default replica set name */
  public static final String DEFAULT_REPLICA_SET_NAME = "rs0";

  /** The shared MongoDB container instance */
  private static final MongoDBContainer MONGODB_CONTAINER;

  /** MongoDB connection string */
  public static final String CONNECTION_STRING;

  /** MongoDB host */
  public static final String HOST;

  /** MongoDB port */
  public static final Integer PORT;

  /** MongoDB replica set URL */
  public static final String REPLICA_SET_URL;

  static {
    // Read configuration from system properties or use defaults
    String mongoVersion =
        System.getProperty("testcontainers.mongodb.version", DEFAULT_MONGODB_VERSION);

    log.info("Initializing shared MongoDB TestContainer with version: {}", mongoVersion);

    MONGODB_CONTAINER = new MongoDBContainer(DockerImageName.parse(mongoVersion)).withReuse(true);

    MONGODB_CONTAINER.start();

    CONNECTION_STRING = MONGODB_CONTAINER.getConnectionString();
    HOST = MONGODB_CONTAINER.getHost();
    PORT = MONGODB_CONTAINER.getFirstMappedPort();
    REPLICA_SET_URL = MONGODB_CONTAINER.getReplicaSetUrl();

    log.info("Shared MongoDB TestContainer started successfully");
    log.info("Connection String: {}", CONNECTION_STRING);
    log.info("Host: {}", HOST);
    log.info("Port: {}", PORT);

    // Add shutdown hook
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  log.info("Stopping shared MongoDB TestContainer");
                  MONGODB_CONTAINER.stop();
                }));
  }

  public static MongoDBContainer getInstance() {
    return MONGODB_CONTAINER;
  }

  public static String getConnectionString() {
    return CONNECTION_STRING;
  }

  public static String getHost() {
    return HOST;
  }

  public static Integer getPort() {
    return PORT;
  }

  public static String getReplicaSetUrl() {
    return REPLICA_SET_URL;
  }

  private SharedMongoDBContainer() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }
}
