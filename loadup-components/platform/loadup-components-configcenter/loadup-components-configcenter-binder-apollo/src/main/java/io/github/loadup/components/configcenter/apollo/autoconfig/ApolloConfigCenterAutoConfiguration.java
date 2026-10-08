package io.github.loadup.components.configcenter.apollo.autoconfig;

import com.ctrip.framework.apollo.ConfigService;
import io.github.loadup.components.configcenter.ConfigCenterProvider;
import io.github.loadup.components.configcenter.apollo.ApolloConfigCenterProperties;
import io.github.loadup.components.configcenter.apollo.ApolloConfigCenterProvider;
import io.github.loadup.components.configcenter.autoconfig.ConfigCenterAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(before = ConfigCenterAutoConfiguration.class)
@ConditionalOnClass(ConfigService.class)
@ConditionalOnProperty(prefix = "loadup.configcenter", name = "binder-type", havingValue = "apollo")
@EnableConfigurationProperties(ApolloConfigCenterProperties.class)
public class ApolloConfigCenterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConfigCenterProvider apolloConfigCenterProvider(ApolloConfigCenterProperties config) {
        return new ApolloConfigCenterProvider(config);
    }
}
