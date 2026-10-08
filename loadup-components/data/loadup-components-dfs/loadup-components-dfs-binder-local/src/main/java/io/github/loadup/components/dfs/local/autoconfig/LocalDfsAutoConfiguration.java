package io.github.loadup.components.dfs.local.autoconfig;

import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.autoconfig.DfsAutoConfiguration;
import io.github.loadup.components.dfs.local.LocalDfsProperties;
import io.github.loadup.components.dfs.local.LocalDfsProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Auto-configuration for the local filesystem binder. */
@AutoConfiguration(before = DfsAutoConfiguration.class)
@ConditionalOnProperty(prefix = "loadup.dfs", name = "binder-type", havingValue = "local", matchIfMissing = true)
@EnableConfigurationProperties(LocalDfsProperties.class)
public class LocalDfsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DfsProvider.class)
    public DfsProvider localDfsProvider(LocalDfsProperties properties) {
        return new LocalDfsProvider(properties);
    }
}
