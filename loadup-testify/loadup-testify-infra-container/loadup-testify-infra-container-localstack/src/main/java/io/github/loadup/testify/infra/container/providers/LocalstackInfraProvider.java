package io.github.loadup.testify.infra.container.providers;

/*-
 * #%L
 * Testify Infra Container Localstack
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class LocalstackInfraProvider implements InfraProvider {
  public static final String DEFAULT_LOCALSTACK_VERSION = "localstack/localstack:3.0";
  public static final String DEFAULT_LOCALSTACK_SERVICES = "s3";

  private LocalStackContainer container;

  @Override
  public String getType() {
    return "localstack";
  }

  @Override
  @SuppressWarnings("unchecked")
  public void start(Map<String, Object> config, boolean reuse) {
    var imageName = (String) config.getOrDefault("image", DEFAULT_LOCALSTACK_VERSION);
    container = new LocalStackContainer(DockerImageName.parse(imageName));

    // 从 YAML 配置中读取需要开启的服务列表，例如: services: ["s3", "sqs"]
    Object servicesObj = config.get("services");

    if (servicesObj instanceof List) {
      List<String> services = (List<String>) servicesObj;
      container.withServices(services.toArray(String[]::new));
    } else if (servicesObj instanceof String) {
      // 支持单个服务以字符串形式配置
      container.withServices((String) servicesObj);
    } else {
      // 默认启用 S3 服务
      container.withServices(DEFAULT_LOCALSTACK_SERVICES);
    }

    container.withReuse(reuse);
    container.start();
    log.info(">>> [TESTIFY-INFRA] LocalStack started with endpoint: {}", container.getEndpoint());
  }

  @Override
  public Map<String, String> getExposedProperties() {
    if (container == null || !container.isRunning()) {
      return Map.of();
    }

    Map<String, String> props = new HashMap<>();
    String endpoint = container.getEndpoint().toString();
    String region = container.getRegion();
    String accessKey = container.getAccessKey();
    String secretKey = container.getSecretKey();

    // 适配 Spring Cloud AWS 3.x (Spring Boot 3 标准) 的属性前缀
    props.put("spring.cloud.aws.endpoint", endpoint);
    props.put("spring.cloud.aws.region.static", region);
    props.put("spring.cloud.aws.credentials.access-key", accessKey);
    props.put("spring.cloud.aws.credentials.secret-key", secretKey);

    // 同时也兼容一些常见的 S3/SQS 客户端自定义配置
    props.put("aws.s3.endpoint", endpoint);
    props.put("aws.sqs.endpoint", endpoint);

    return props;
  }

  @Override
  public void stop() {
    if (container != null) {
      container.stop();
    }
  }
}
