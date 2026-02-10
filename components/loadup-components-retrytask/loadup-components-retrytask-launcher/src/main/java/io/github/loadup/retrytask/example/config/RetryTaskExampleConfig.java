package io.github.loadup.retrytask.example.config;

import io.github.loadup.retrytask.example.strategy.RandomRetryStrategy;
import io.github.loadup.retrytask.strategy.RetryStrategyRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetryTaskExampleConfig {

    @Autowired
    private RetryStrategyRegistry retryStrategyRegistry;

    @Autowired
    private RandomRetryStrategy randomRetryStrategy;

    @PostConstruct
    public void registerCustomStrategies() {
        retryStrategyRegistry.register(randomRetryStrategy);
    }
}
