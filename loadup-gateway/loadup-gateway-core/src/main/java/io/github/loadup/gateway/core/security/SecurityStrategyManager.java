package io.github.loadup.gateway.core.security;

/*-
 * #%L
 * LoadUp Gateway Core
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

import io.github.loadup.gateway.facade.spi.SecurityStrategy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Manages security strategies.
 */
@Slf4j
@Component
public class SecurityStrategyManager {

    private final Map<String, SecurityStrategy> strategyMap = new ConcurrentHashMap<>();

    public SecurityStrategyManager(List<SecurityStrategy> strategies) {
        if (strategies != null) {
            for (SecurityStrategy strategy : strategies) {
                if (strategyMap.containsKey(strategy.getCode())) {
                    log.warn("Duplicate security strategy for code: {}", strategy.getCode());
                }
                strategyMap.put(strategy.getCode(), strategy);
            }
        }
        // Ensure OFF strategy exists if not provided
        if (!strategyMap.containsKey("OFF")) {
            strategyMap.put("OFF", new OffSecurityStrategy());
        }
        log.info("Initialized SecurityStrategyManager with strategies: {}", strategyMap.keySet());
    }

    public SecurityStrategy getStrategy(String code) {
        return strategyMap.get(code);
    }

    /**
     * Default OFF strategy implementation.
     */
    private static class OffSecurityStrategy implements SecurityStrategy {
        @Override
        public String getCode() {
            return "OFF";
        }

        @Override
        public void process(io.github.loadup.gateway.facade.context.GatewayContext context) {
            // Do nothing
        }
    }
}
