/*-
 * #%L
 * Loadup Gateway WebMVC Engine
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.gateway.webmvc.security;

import io.github.loadup.gateway.facade.spi.SecurityStrategy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry of {@link SecurityStrategy} implementations keyed by security code.
 */
public class SecurityStrategyManager {
    private static final Logger log = LoggerFactory.getLogger(SecurityStrategyManager.class);

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
        if (!strategyMap.containsKey("OFF")) {
            strategyMap.put("OFF", new OffSecurityStrategy());
        }
        log.info("Initialized SecurityStrategyManager with strategies: {}", strategyMap.keySet());
    }

    public SecurityStrategy getStrategy(String code) {
        return strategyMap.get(code);
    }
}
