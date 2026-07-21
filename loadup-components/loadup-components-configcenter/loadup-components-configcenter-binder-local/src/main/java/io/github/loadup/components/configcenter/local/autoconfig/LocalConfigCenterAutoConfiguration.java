package io.github.loadup.components.configcenter.local.autoconfig;

import io.github.loadup.components.configcenter.ConfigCenterProvider;
import io.github.loadup.components.configcenter.local.LocalConfigCenterConfig;
import io.github.loadup.components.configcenter.local.LocalConfigCenterProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.configcenter", name = "binder-type", havingValue = "local", matchIfMissing = true)
@EnableConfigurationProperties(LocalConfigCenterConfig.class)
public class LocalConfigCenterAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ConfigCenterProvider localConfigCenterProvider() {
        return new LocalConfigCenterProvider();
    }
}
