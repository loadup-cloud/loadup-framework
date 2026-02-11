package io.github.loadup.framework.api.autoconfig;

import io.github.loadup.framework.api.core.BindingPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CoreAutoConfiguration {
    @Bean
    public BindingPostProcessor bindingPostProcessor(ApplicationContext context) {
        return new BindingPostProcessor(context);
    }
}
