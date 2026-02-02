package io.github.loadup.testify.infra.container.providers;

/*-
 * #%L
 * Testify Infra Container Kafka
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

import io.github.loadup.testify.infra.container.InfraProvider;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class KafkaInfraProvider implements InfraProvider {
  public static final String DEFAULT_KAFKA_VERSION = "apache/kafka:4.1.1";

  private KafkaContainer container;

  @Override
  public String getType() {
    return "kafka";
  }

  @Override
  public void start(Map<String, Object> config, boolean reuse) {
    var image = (String) config.getOrDefault("image", DEFAULT_KAFKA_VERSION);

    container = new KafkaContainer(DockerImageName.parse(image)).withReuse(reuse);

    container.start();
  }

  @Override
  public Map<String, String> getExposedProperties() {
    if (container == null || !container.isRunning()) return Map.of();

    // 核心：将容器产生的动态随机地址注入到 Spring Kafka 配置中
    return Map.of(
        "spring.kafka.bootstrap-servers",
        container.getBootstrapServers(),
        "spring.kafka.consumer.bootstrap-servers",
        container.getBootstrapServers(),
        "spring.kafka.producer.bootstrap-servers",
        container.getBootstrapServers());
  }

  @Override
  public void stop() {
    if (container != null) container.stop();
  }
}
