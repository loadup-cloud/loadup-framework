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
package io.github.loadup.components.testcontainers.messaging;

import org.springframework.test.context.ContextConfiguration;

/**
 * Abstract base test class that automatically configures Kafka TestContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@ContextConfiguration(initializers = KafkaContainerInitializer.class)
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
