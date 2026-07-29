package io.github.loadup.gateway.plugins.config;

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.plugins.RpcProxyProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(org.apache.dubbo.rpc.service.GenericService.class)
public class RpcProxyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RpcProxyProcessor rpcProxyProcessor(GatewayProperties props) {
        return new RpcProxyProcessor(props);
    }
}
