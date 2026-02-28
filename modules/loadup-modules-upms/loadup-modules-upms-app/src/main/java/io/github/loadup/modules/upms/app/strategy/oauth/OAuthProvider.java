package io.github.loadup.modules.upms.app.strategy.oauth;

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

import io.github.loadup.modules.upms.client.dto.OAuthToken;
import io.github.loadup.modules.upms.client.dto.OAuthUserInfo;

/**
 * OAuth Provider 接口
 * 每个第三方 OAuth 提供商实现此接口
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface OAuthProvider {

    /**
     * Provider 名称
     *
     * @return wechat | github | google
     */
    String getProviderName();

    /**
     * 获取授权 URL
     *
     * @param state       状态参数（防CSRF）
     * @param redirectUri 回调地址
     * @return 授权URL
     */
    String getAuthorizationUrl(String state, String redirectUri);

    /**
     * 通过 code 换取 access_token
     *
     * @param code        授权码
     * @param redirectUri 回调地址
     * @return OAuth Token
     */
    OAuthToken exchangeToken(String code, String redirectUri);

    /**
     * 获取用户信息
     *
     * @param accessToken 访问令牌
     * @return 用户信息
     */
    OAuthUserInfo getUserInfo(String accessToken);
}
