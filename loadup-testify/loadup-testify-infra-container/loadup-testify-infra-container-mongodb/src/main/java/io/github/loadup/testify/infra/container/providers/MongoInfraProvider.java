package io.github.loadup.testify.infra.container.providers;

/*-
 * #%L
 * Testify Infra Container Mongodb
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
import org.testcontainers.mongodb.MongoDBContainer;

@Slf4j
public class MongoInfraProvider implements InfraProvider {
  public static final String DEFAULT_MONGODB_VERSION = "mongo:7.0";

  private MongoDBContainer container;

  @Override
  public String getType() {
    return "mongodb";
  }

  @Override
  public void start(Map<String, Object> config, boolean reuse) {
    var image = (String) config.getOrDefault("image", DEFAULT_MONGODB_VERSION);

    container = new MongoDBContainer(image).withReuse(reuse);

    container.start();
  }

  @Override
  public Map<String, String> getExposedProperties() {
    if (container == null || !container.isRunning()) return Map.of();

    // 动态注入 MongoDB 连接 URI，Spring Data MongoDB 会优先识别此属性
    return Map.of(
        "spring.data.mongodb.uri",
        container.getReplicaSetUrl(),
        "spring.data.mongodb.host",
        container.getReplicaSetUrl(),
        "spring.data.mongodb.port",
        container.getFirstMappedPort().toString());
  }

  @Override
  public void stop() {
    if (container != null) container.stop();
  }
}
