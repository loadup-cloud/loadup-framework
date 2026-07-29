package io.github.loadup.gateway.plugins.config;

import io.github.loadup.gateway.plugins.SpringBeanProxyProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SpringBeanProxyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SpringBeanProxyProcessor springBeanProxyProcessor(ApplicationContext ctx) {
        return new SpringBeanProxyProcessor(ctx);
    }
}
