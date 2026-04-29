package io.github.loadup.components.configcenter.autoconfig;

import io.github.loadup.components.configcenter.model.ConfigChangeEvent;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

/**
 * Optional auto-configuration for automatic Spring context refresh on config change.
 *
 * <p>Activates only when {@code spring-cloud-context} is on the classpath.
 * Listens for {@link ConfigChangeEvent} and triggers
 * {@link ContextRefresher#refresh()} so that {@code @Value} /
 * {@code @ConfigurationProperties} beans are refreshed automatically.
 *
 * <p>Import this class on demand via
 * {@link io.github.loadup.components.configcenter.annotation.EnableConfigAutoRefresh};
 * it is not activated automatically.
 */
@AutoConfiguration
@ConditionalOnClass(ContextRefresher.class)
public class ConfigAutoRefreshConfiguration {

    @Bean
    public ApplicationListener<ConfigChangeEvent> configAutoRefreshListener(ContextRefresher contextRefresher) {
        return event -> {
            contextRefresher.refresh();
        };
    }
}
