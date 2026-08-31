package io.github.loadup.gateway.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.loadup.commons.dto.DTO;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.spi.RouteStore;
import io.github.loadup.gateway.plugins.yaml.YamlRouteStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
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
 * its own auto-configuration. This class only supplies the default YAML-based
 * {@link RouteStore}; other stores (e.g. database) can be activated by their plugins and
 * take precedence via {@code @ConditionalOnMissingBean}.
 */
@AutoConfiguration
@EnableConfigurationProperties(GatewayProperties.class)
@ConditionalOnProperty(prefix = "loadup.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GatewayAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(GatewayAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(RouteStore.class)
    public YamlRouteStore routeStore(GatewayProperties properties, ApplicationEventPublisher publisher) {
        log.info("Registering default YAML route store (storage.type=FILE)");
        return new YamlRouteStore(properties, publisher);
    }

    @Autowired
    public void configureJsonMapper(ObjectProvider<ObjectMapper> mapperProvider) {
        ObjectMapper mapper = mapperProvider.getIfAvailable();
        if (mapper != null) {
            DTO.setObjectMapper(mapper);
            log.info("DTO ObjectMapper configured from Spring context");
        } else {
            log.info("No Jackson 2 ObjectMapper bean found, DTO keeps its default mapper");
        }
    }
}
