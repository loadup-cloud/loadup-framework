package io.github.loadup.modules.upms.app.strategy;

/*-
 * #%L
 * Loadup Modules UPMS App Layer
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 登录策略管理器
 * 管理所有登录策略，根据 loginType 路由
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Component
public class LoginStrategyManager {
    private static final Logger log = LoggerFactory.getLogger(LoginStrategyManager.class);

    private final Map<String, LoginStrategy> strategyMap = new ConcurrentHashMap<>();

    /**
     * 构造器注入所有策略实现
     */
    public LoginStrategyManager(List<LoginStrategy> strategies) {
        if (strategies != null && !strategies.isEmpty()) {
            // 按优先级排序
            strategies.stream()
                    .sorted(Comparator.comparingInt(LoginStrategy::getOrder))
                    .forEach(strategy -> {
                        String loginType = strategy.getLoginType();
                        if (strategyMap.containsKey(loginType)) {
                            log.warn("Duplicate login strategy for type: {}, using higher priority one", loginType);
                        } else {
                            strategyMap.put(loginType, strategy);
                            log.info(
                                    "Registered login strategy: {} -> {}",
                                    loginType,
                                    strategy.getClass().getSimpleName());
                        }
                    });
        }
        log.info("LoginStrategyManager initialized with {} strategies: {}", strategyMap.size(), strategyMap.keySet());
    }

    /**
     * 根据登录类型获取策略
     *
     * @param loginType 登录类型
     * @return 登录策略
     * @throws IllegalArgumentException 不支持的登录类型
     */
    public LoginStrategy getStrategy(String loginType) {
        LoginStrategy strategy = strategyMap.get(loginType);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported login type: " + loginType);
        }
        return strategy;
    }

    /**
     * 检查是否支持某种登录类型
     *
     * @param loginType 登录类型
     * @return true-支持，false-不支持
     */
    public boolean supportsLoginType(String loginType) {
        return strategyMap.containsKey(loginType);
    }
}
