package io.github.loadup.gateway.plugins.config;

import io.github.loadup.gateway.facade.spi.RouteStore;
import io.github.loadup.gateway.plugins.DatabaseRouteStore;
import io.github.loadup.gateway.plugins.manager.RouteManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.gateway.storage", name = "type", havingValue = "DATABASE")
public class DatabaseRepositoryAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(DatabaseRepositoryAutoConfiguration.class);


    @Bean
    @ConditionalOnMissingBean
    public RouteStore databaseRouteStore(RouteManager routeManager) {
        log.info("DatabaseRouteStore activated");
        return new DatabaseRouteStore(routeManager);
    }
}
