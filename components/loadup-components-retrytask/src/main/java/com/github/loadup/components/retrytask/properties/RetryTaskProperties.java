package com.github.loadup.components.retrytask.properties;

import com.github.loadup.components.retrytask.config.RetryStrategyConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "retry-task")
public class RetryTaskProperties {

    private Map<String, RetryStrategyConfig> strategies = new HashMap<>();

    public Map<String, RetryStrategyConfig> getStrategies() {
        return strategies;
    }
}