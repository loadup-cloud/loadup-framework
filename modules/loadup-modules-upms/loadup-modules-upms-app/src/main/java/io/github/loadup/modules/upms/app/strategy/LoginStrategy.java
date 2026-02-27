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

import io.github.loadup.upms.api.dto.AuthenticatedUser;
import io.github.loadup.upms.api.dto.LoginCredentials;

/**
 * 登录策略接口（SPI）
 * 每种登录方式实现此接口
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface LoginStrategy {

    /**
     * 登录类型标识
     *
     * @return PASSWORD | MOBILE | EMAIL | OAUTH
     */
    String getLoginType();

    /**
     * 执行认证逻辑
     *
     * @param credentials 登录凭证（通用对象，不同策略自行解析）
     * @return 认证结果（用户信息）
     * @throws RuntimeException 认证失败
     */
    AuthenticatedUser authenticate(LoginCredentials credentials);

    /**
     * 优先级（用于同类型多实现时的选择）
     *
     * @return 优先级，数值越小优先级越高
     */
    default int getOrder() {
        return 0;
    }
}

