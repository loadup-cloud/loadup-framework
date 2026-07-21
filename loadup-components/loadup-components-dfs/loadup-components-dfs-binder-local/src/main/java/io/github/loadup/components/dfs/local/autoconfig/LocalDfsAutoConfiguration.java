package io.github.loadup.components.dfs.local.autoconfig;

import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.local.LocalDfsConfig;
import io.github.loadup.components.dfs.local.LocalDfsProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.dfs", name = "binder-type", havingValue = "local", matchIfMissing = true)
@EnableConfigurationProperties(LocalDfsConfig.class)
public class LocalDfsAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public DfsProvider localDfsProvider(LocalDfsConfig config) {
        return new LocalDfsProvider(config);
    }
}
