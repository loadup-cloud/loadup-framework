package com.github.loadup.components.gateway.core;

import com.github.loadup.components.gateway.core.config.GatewayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway Bootstrap Configuration
 *
 * @author user
 * @since 1.0.0
 */
@Configuration
@ComponentScan("com.github.loadup.components.gateway")
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayBootstrap {

}
