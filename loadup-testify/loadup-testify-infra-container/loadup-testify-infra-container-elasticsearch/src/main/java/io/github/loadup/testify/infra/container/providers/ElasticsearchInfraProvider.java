package io.github.loadup.testify.infra.container.providers;

/*-
 * #%L
 * Testify Infra Container Elasticsearch
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
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

@Slf4j
public class ElasticsearchInfraProvider implements InfraProvider {

  public static final String DEFAULT_ELASTICSEARCH_VERSION =
      "docker.elastic.co/elasticsearch/elasticsearch:8.11.0";

  private ElasticsearchContainer container;

  @Override
  public String getType() {
    return "elasticsearch";
  }

  @Override
  public void start(Map<String, Object> config, boolean reuse) {
    var image = (String) config.getOrDefault("image", DEFAULT_ELASTICSEARCH_VERSION);

    container =
        new ElasticsearchContainer(image)
            // 关键配置：关闭 xpack 安全特性
            .withEnv("xpack.security.enabled", "false")
            .withEnv("xpack.security.http.ssl.enabled", "false")
            // 限制内存占用
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
            // 增加启动超时时间（ES 启动确实慢）
            .withStartupTimeout(Duration.ofMinutes(3))
            .waitingFor(Wait.forHttp("/").forStatusCode(200))
            .withReuse(reuse);

    container.start();
  }

  @Override
  public Map<String, String> getExposedProperties() {
    if (container == null || !container.isRunning()) return Map.of();

    // 适配 Spring Boot 3.x 标准配置
    return Map.of(
        "spring.elasticsearch.uris",
        container.getHttpHostAddress(),
        "spring.elasticsearch.rest.uris",
        container.getHttpHostAddress(),
        "spring.data.elasticsearch.client.reactive.endpoints",
        container.getHttpHostAddress());
  }

  @Override
  public void stop() {
    if (container != null) container.stop();
  }
}
