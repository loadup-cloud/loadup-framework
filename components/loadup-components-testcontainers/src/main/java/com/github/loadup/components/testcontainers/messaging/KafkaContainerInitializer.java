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
package com.github.loadup.components.testcontainers.messaging;

import com.github.loadup.components.testcontainers.BaseContainerInitializer;
import com.github.loadup.components.testcontainers.config.TestContainersProperties;
import com.github.loadup.components.testcontainers.config.TestContainersProperties.ContainerConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Spring Boot test initializer for Kafka TestContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
public class KafkaContainerInitializer extends BaseContainerInitializer {

  @Override
  protected String getContainerName() {
    return "Kafka";
  }

  @Override
  protected ContainerConfig getContainerConfig(TestContainersProperties p) {
    return p.getKafka();
  }

  @Override
  protected void startAndApplyProperties(ContainerConfig config, ConfigurableEnvironment env) {
    SharedKafkaContainer.startContainer(config);
    applyProperties(
        env,
        Map.of(
            "spring.kafka.bootstrap-servers",
            SharedKafkaContainer.getBootstrapServers(),
            "spring.kafka.consumer.bootstrap-servers",
            SharedKafkaContainer.getBootstrapServers(),
            "spring.kafka.producer.bootstrap-servers",
            SharedKafkaContainer.getBootstrapServers()));
  }
}
