package io.github.loadup.components.dfs.autoconfig;

import io.github.loadup.components.dfs.DefaultDfsService;
import io.github.loadup.components.dfs.DfsProperties;
import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.DfsService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Creates the facade only when exactly one DFS binder is active. */
@AutoConfiguration
@ConditionalOnSingleCandidate(DfsProvider.class)
@EnableConfigurationProperties(DfsProperties.class)
public class DfsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DfsService dfsService(DfsProvider provider) {
        return new DefaultDfsService(provider);
    }
}
