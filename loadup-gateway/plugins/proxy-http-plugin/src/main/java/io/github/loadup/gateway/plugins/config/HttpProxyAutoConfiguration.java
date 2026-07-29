package io.github.loadup.gateway.plugins.config;

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.plugins.HttpProxyProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@AutoConfiguration
public class HttpProxyAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(HttpProxyAutoConfiguration.class);


    @Bean
    @ConditionalOnMissingBean
    public HttpProxyProcessor httpProxyProcessor(GatewayProperties props) {
        return new HttpProxyProcessor(props);
    }
}
