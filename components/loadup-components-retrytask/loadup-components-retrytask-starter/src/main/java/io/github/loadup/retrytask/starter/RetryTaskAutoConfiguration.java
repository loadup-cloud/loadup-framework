package io.github.loadup.retrytask.starter;

import io.github.loadup.retrytask.core.config.RetryTaskProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for the retry task module.
 */
@Configuration
@EnableConfigurationProperties(RetryTaskProperties.class)
@ComponentScan(basePackages = "io.github.loadup.retrytask")
public class RetryTaskAutoConfiguration {
}
