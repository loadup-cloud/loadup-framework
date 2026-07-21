package io.github.loadup.components.scheduler.autoconfig;

import io.github.loadup.components.scheduler.DefaultSchedulerTemplate;
import io.github.loadup.components.scheduler.SchedulerProperties;
import io.github.loadup.components.scheduler.SchedulerProvider;
import io.github.loadup.components.scheduler.SchedulerTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnSingleCandidate(SchedulerProvider.class)
@EnableConfigurationProperties(SchedulerProperties.class)
public class SchedulerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(SchedulerTemplate.class)
    public SchedulerTemplate schedulerTemplate(SchedulerProvider provider) {
        return new DefaultSchedulerTemplate(provider);
    }
}
