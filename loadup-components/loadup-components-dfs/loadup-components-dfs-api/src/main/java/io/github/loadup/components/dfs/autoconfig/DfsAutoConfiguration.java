package io.github.loadup.components.dfs.autoconfig;

import io.github.loadup.components.dfs.DfsProperties;
import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.DfsTemplate;
import io.github.loadup.components.dfs.DefaultDfsTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnSingleCandidate(DfsProvider.class)
@EnableConfigurationProperties(DfsProperties.class)
public class DfsAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(DfsTemplate.class)
    public DfsTemplate dfsTemplate(DfsProvider provider) {
        return new DefaultDfsTemplate(provider);
    }
}
