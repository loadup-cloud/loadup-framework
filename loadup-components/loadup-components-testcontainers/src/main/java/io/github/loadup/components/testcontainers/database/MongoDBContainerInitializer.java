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

import io.github.loadup.components.testcontainers.BaseContainerInitializer;
import io.github.loadup.components.testcontainers.config.TestContainersProperties;
import io.github.loadup.components.testcontainers.config.TestContainersProperties.ContainerConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Spring Boot test initializer for MongoDB TestContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
public class MongoDBContainerInitializer extends BaseContainerInitializer {

  @Override
  protected String getContainerName() {
    return "MongoDB";
  }

  @Override
  protected ContainerConfig getContainerConfig(TestContainersProperties p) {
    return p.getMongodb();
  }

  @Override
  protected void startAndApplyProperties(ContainerConfig config, ConfigurableEnvironment env) {
    SharedMongoDBContainer.startContainer(config);
    applyProperties(
        env,
        Map.of(
            "spring.data.mongodb.uri",
            SharedMongoDBContainer.getReplicaSetUrl(),
            "spring.data.mongodb.host",
            SharedMongoDBContainer.getHost(),
            "spring.data.mongodb.port",
            String.valueOf(SharedMongoDBContainer.getPort())));
  }
}
