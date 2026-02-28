package io.github.loadup.modules.upms.app.strategy;

/*-
 * #%L
 * Loadup Modules UPMS App Layer
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 登录策略管理器
 * 管理所有登录策略，根据 loginType 路由
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Component
public class LoginStrategyManager {

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
