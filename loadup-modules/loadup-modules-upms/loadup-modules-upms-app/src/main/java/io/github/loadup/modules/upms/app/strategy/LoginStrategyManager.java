/*
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
package io.github.loadup.modules.upms.app.strategy;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.log.LogUtil;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Component
public class LoginStrategyManager {

    private final Map<String, LoginStrategy> strategyMap = new ConcurrentHashMap<>();

    public LoginStrategyManager(List<LoginStrategy> strategies) {
        if (strategies != null && !strategies.isEmpty()) {

            strategies.stream()
                    .sorted(Comparator.comparingInt(LoginStrategy::getOrder))
                    .forEach(strategy -> {
                        String loginType = strategy.getLoginType();
                        if (strategyMap.containsKey(loginType)) {
                            LogUtil.warn(
                                    LoginStrategyManager.class,
                                    "Duplicate login strategy for type: {}, using higher priority one",
                                    loginType);
                        } else {
                            strategyMap.put(loginType, strategy);
                            LogUtil.info(
                                    LoginStrategyManager.class,
                                    "Registered login strategy: {} -> {}",
                                    loginType,
                                    strategy.getClass().getSimpleName());
                        }
                    });
        }
        LogUtil.info(
                LoginStrategyManager.class,
                "LoginStrategyManager initialized with {} strategies: {}",
                strategyMap.size(),
                strategyMap.keySet());
    }

    public LoginStrategy getStrategy(String loginType) {
        LoginStrategy strategy = strategyMap.get(loginType);
        if (strategy == null) {
            throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL, "Unsupported login type");
        }
        return strategy;
    }

    public boolean supportsLoginType(String loginType) {
        return strategyMap.containsKey(loginType);
    }
}
