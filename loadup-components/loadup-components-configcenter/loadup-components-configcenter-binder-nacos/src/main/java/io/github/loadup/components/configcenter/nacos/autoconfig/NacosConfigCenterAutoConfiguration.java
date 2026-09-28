package io.github.loadup.components.configcenter.nacos.autoconfig;

import com.alibaba.nacos.api.NacosFactory;
import io.github.loadup.components.configcenter.ConfigCenterProvider;
import io.github.loadup.components.configcenter.autoconfig.ConfigCenterAutoConfiguration;
import io.github.loadup.components.configcenter.nacos.NacosConfigCenterProperties;
import io.github.loadup.components.configcenter.nacos.NacosConfigCenterProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(before = ConfigCenterAutoConfiguration.class)
@ConditionalOnClass(NacosFactory.class)
@ConditionalOnProperty(prefix = "loadup.configcenter", name = "binder-type", havingValue = "nacos")
@EnableConfigurationProperties(NacosConfigCenterProperties.class)
public class NacosConfigCenterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConfigCenterProvider nacosConfigCenterProvider(NacosConfigCenterProperties config) {
        return new NacosConfigCenterProvider(config);
    }
}
