package io.github.loadup.gateway.plugins.config;

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.plugins.HttpProxyProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
public class HttpProxyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HttpProxyProcessor httpProxyProcessor(GatewayProperties props) {
        return new HttpProxyProcessor(props);
    }
}
