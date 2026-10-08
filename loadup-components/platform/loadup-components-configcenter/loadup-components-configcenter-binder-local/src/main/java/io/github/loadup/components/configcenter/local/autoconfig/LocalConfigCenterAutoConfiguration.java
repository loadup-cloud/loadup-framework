package io.github.loadup.components.configcenter.local.autoconfig;

import io.github.loadup.components.configcenter.ConfigCenterProvider;
import io.github.loadup.components.configcenter.autoconfig.ConfigCenterAutoConfiguration;
import io.github.loadup.components.configcenter.local.LocalConfigCenterProperties;
import io.github.loadup.components.configcenter.local.LocalConfigCenterProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(before = ConfigCenterAutoConfiguration.class)
@ConditionalOnProperty(
        prefix = "loadup.configcenter",
        name = "binder-type",
        havingValue = "local",
        matchIfMissing = true)
@EnableConfigurationProperties(LocalConfigCenterProperties.class)
public class LocalConfigCenterAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ConfigCenterProvider localConfigCenterProvider() {
        return new LocalConfigCenterProvider();
    }
}
