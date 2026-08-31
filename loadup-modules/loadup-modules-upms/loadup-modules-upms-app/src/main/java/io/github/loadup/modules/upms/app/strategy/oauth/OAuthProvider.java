package io.github.loadup.modules.upms.app.strategy.oauth;

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
