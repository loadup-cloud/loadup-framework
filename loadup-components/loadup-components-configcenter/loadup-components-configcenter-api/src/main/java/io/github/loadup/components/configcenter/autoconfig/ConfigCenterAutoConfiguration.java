package io.github.loadup.components.configcenter.autoconfig;

import io.github.loadup.components.configcenter.ConfigCenterProperties;
import io.github.loadup.components.configcenter.ConfigCenterProvider;
import io.github.loadup.components.configcenter.ConfigCenterTemplate;
import io.github.loadup.components.configcenter.DefaultConfigCenterTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnSingleCandidate(ConfigCenterProvider.class)
@EnableConfigurationProperties(ConfigCenterProperties.class)
public class ConfigCenterAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ConfigCenterTemplate.class)
    public ConfigCenterTemplate configCenterTemplate(ConfigCenterProvider provider) {
        return new DefaultConfigCenterTemplate(provider);
    }
}
