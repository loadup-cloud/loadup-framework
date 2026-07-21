package io.github.loadup.retrytask.strategy;

/*-
 * #%L
 * Loadup Components Retrytask Strategy
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for retry strategies
 */
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
