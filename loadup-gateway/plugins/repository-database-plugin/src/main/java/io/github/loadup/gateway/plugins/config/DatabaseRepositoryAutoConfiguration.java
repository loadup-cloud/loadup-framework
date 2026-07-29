package io.github.loadup.gateway.plugins.config;

import io.github.loadup.gateway.facade.spi.RouteStore;
import io.github.loadup.gateway.plugins.DatabaseRouteStore;
import io.github.loadup.gateway.plugins.manager.RouteManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.gateway.storage", name = "type", havingValue = "DATABASE")
public class DatabaseRepositoryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RouteStore databaseRouteStore(RouteManager routeManager) {
        log.info("DatabaseRouteStore activated");
        return new DatabaseRouteStore(routeManager);
    }
}
