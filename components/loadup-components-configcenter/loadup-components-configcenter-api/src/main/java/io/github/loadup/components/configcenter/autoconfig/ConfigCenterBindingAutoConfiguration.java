package io.github.loadup.components.configcenter.autoconfig;

import io.github.loadup.components.configcenter.manager.ConfigCenterBindingManager;
import io.github.loadup.components.configcenter.properties.ConfigCenterGroupProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Core auto-configuration for the config center.
 *
 * <p>Registers the {@link ConfigCenterBindingManager} singleton bean.
 * Each binder module (local / nacos / apollo) registers its own
 * {@link io.github.loadup.framework.api.manager.BindingMetadata} bean into the manager.
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(ConfigCenterGroupProperties.class)
public class ConfigCenterBindingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConfigCenterBindingManager configCenterBindingManager(
            ConfigCenterGroupProperties props, ApplicationContext context) {
        return new ConfigCenterBindingManager(props, context);
    }
}
