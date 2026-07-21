package io.github.loadup.components.scheduler.simplejob.autoconfig;

import io.github.loadup.components.scheduler.SchedulerProvider;
import io.github.loadup.components.scheduler.simplejob.SimpleJobSchedulerConfig;
import io.github.loadup.components.scheduler.simplejob.SimpleJobSchedulerProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.scheduler", name = "binder-type", havingValue = "simplejob", matchIfMissing = true)
@EnableConfigurationProperties(SimpleJobSchedulerConfig.class)
public class SimpleJobSchedulerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public SchedulerProvider simpleJobSchedulerProvider(SimpleJobSchedulerConfig config) {
        return new SimpleJobSchedulerProvider(config);
    }
}
