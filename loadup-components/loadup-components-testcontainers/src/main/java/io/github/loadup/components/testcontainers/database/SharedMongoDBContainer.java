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
  private static MongoDBContainer MONGODB_CONTAINER;

  private static final AtomicBoolean STARTED = new AtomicBoolean(false);

  /** MongoDB connection string */
  private static String CONNECTION_STRING;

  /** MongoDB host */
  private static String HOST;

  /** MongoDB port */
  private static Integer PORT;

  /** MongoDB replica set URL */
  private static String REPLICA_SET_URL;

  public static void startContainer(ContainerConfig config) {
    if (STARTED.get()) {
      return;
    }

    synchronized (SharedMongoDBContainer.class) {
      if (STARTED.get()) {
        return;
      }

      String imageName =
          (config.getVersion() != null) ? config.getVersion() : DEFAULT_MONGODB_VERSION;

      log.info("🚀 Starting Shared MongoDB TestContainer: {}", imageName);

      MONGODB_CONTAINER =
          new MongoDBContainer(DockerImageName.parse(imageName)).withReuse(config.isReuse());

      MONGODB_CONTAINER.start();
      STARTED.set(true);

      CONNECTION_STRING = MONGODB_CONTAINER.getConnectionString();
      HOST = MONGODB_CONTAINER.getHost();
      PORT = MONGODB_CONTAINER.getFirstMappedPort();
      REPLICA_SET_URL = MONGODB_CONTAINER.getReplicaSetUrl();

      log.info("✅ MongoDB Container started at: {}", MONGODB_CONTAINER.getReplicaSetUrl());

      // JVM 退出时自动关闭
      // 2. 智能关闭钩子
      if (!config.isReuse()) {
        log.info("Reuse is disabled. Registering shutdown hook to stop container.");
        Runtime.getRuntime()
            .addShutdownHook(
                new Thread(
                    () -> {
                      if (MONGODB_CONTAINER != null) {
                        log.info("🛑 Stopping MongoDB TestContainer...");
                        MONGODB_CONTAINER.stop();
                      }
                    }));
      } else {
        log.info("♻️ Reuse is enabled. Container will persist after JVM exits.");
      }
    }
  }

  /**
   * Get the shared MongoDB container instance.
   *
   * @return the shared MongoDB container instance
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static MongoDBContainer getInstance() {
    return MONGODB_CONTAINER;
  }

  /**
   * Get the MongoDB connection string.
   *
   * @return the connection string
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static String getConnectionString() {
    checkStarted();
    return CONNECTION_STRING;
  }

  /**
   * Get the MongoDB host.
   *
   * @return the host
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static String getHost() {
    checkStarted();
    return HOST;
  }

  /**
   * Get the MongoDB port.
   *
   * @return the port
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static Integer getPort() {
    checkStarted();
    return PORT;
  }

  public static String getReplicaSetUrl() {
    return REPLICA_SET_URL;
  }

  private SharedMongoDBContainer() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static boolean isStarted() {
    return STARTED.get();
  }

  private static void checkStarted() {
    if (!STARTED.get()) {
      throw new IllegalStateException("MongoDB Container has not been started yet!");
    }
  }
}
