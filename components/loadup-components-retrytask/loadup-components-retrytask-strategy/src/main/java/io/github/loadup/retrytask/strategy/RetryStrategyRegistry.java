package io.github.loadup.retrytask.strategy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Component
public class RetryStrategyRegistry {

    private final Map<String, RetryStrategy> strategies = new ConcurrentHashMap<>();

    public RetryStrategyRegistry(List<RetryStrategy> strategyList) {
        for (RetryStrategy strategy : strategyList) {
            if (strategy != null && strategy.getType() != null) {
                strategies.put(strategy.getType(), strategy);
            }
        }
    }

    public RetryStrategy getStrategy(String type) {
        return strategies.get(type);
    }

    public void register(RetryStrategy strategy) {
        if (strategy != null && strategy.getType() != null) {
            strategies.put(strategy.getType(), strategy);
        }
    }
}
