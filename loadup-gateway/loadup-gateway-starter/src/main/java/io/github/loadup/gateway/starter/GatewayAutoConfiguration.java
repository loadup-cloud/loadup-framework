/*-
 * #%L
 * LoadUp Gateway Starter
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.gateway.starter;

import io.github.loadup.gateway.api.spi.RouteSource;
import io.github.loadup.gateway.plugins.yaml.YamlRouteSource;
import io.github.loadup.gateway.webmvc.config.GatewayProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

/**
 * Starter-level auto-configuration for LoadUp Gateway.
 *
 * <p>The gateway engine itself lives in {@code loadup-gateway-webmvc} and is registered by
 * its own auto-configuration. Applications may replace the default file source with
 * another {@link RouteSource} implementation.
 */
@AutoConfiguration
@EnableConfigurationProperties(GatewayProperties.class)
@ConditionalOnProperty(prefix = "loadup.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GatewayAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(GatewayAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(RouteSource.class)
    @ConditionalOnProperty(prefix = "loadup.gateway.source", name = "type", havingValue = "file", matchIfMissing = true)
    public RouteSource routeSource(
            GatewayProperties properties,
            ApplicationEventPublisher publisher,
            @org.springframework.beans.factory.annotation.Value(
                            "${loadup.gateway.source.file.path:classpath:gateway-routes.yml}")
                    String path) {
        log.info("Registering managed file route source: {}", path);
        return new YamlRouteSource(path, properties.getRouteRefreshInterval(), publisher);
    }
}
