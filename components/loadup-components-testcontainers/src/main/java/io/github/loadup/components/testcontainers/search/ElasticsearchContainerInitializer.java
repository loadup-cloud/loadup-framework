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
package io.github.loadup.components.testcontainers.search;

import io.github.loadup.components.testcontainers.BaseContainerInitializer;
import io.github.loadup.components.testcontainers.config.TestContainersProperties;
import io.github.loadup.components.testcontainers.config.TestContainersProperties.ContainerConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Spring Boot test initializer for Elasticsearch TestContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
public class ElasticsearchContainerInitializer extends BaseContainerInitializer {

  @Override
  protected String getContainerName() {
    return "Elasticsearch";
  }

  @Override
  protected ContainerConfig getContainerConfig(TestContainersProperties p) {
    return p.getElasticsearch();
  }

  @Override
  protected void startAndApplyProperties(ContainerConfig config, ConfigurableEnvironment env) {
    SharedElasticsearchContainer.startContainer(config);
    applyProperties(
        env,
        Map.of(
            "spring.elasticsearch.uris", SharedElasticsearchContainer.getHttpHostAddress(),
            "spring.elasticsearch.rest.uris", SharedElasticsearchContainer.getHttpHostAddress(),
            "spring.data.elasticsearch.client.reactive.endpoints",
                SharedElasticsearchContainer.getHttpHostAddress()));
  }
}
