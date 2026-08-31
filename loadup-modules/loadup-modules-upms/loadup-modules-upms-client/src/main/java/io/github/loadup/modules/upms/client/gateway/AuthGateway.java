package io.github.loadup.modules.upms.client.gateway;

/*-
 * #%L
 * Loadup Modules UPMS Client Layer
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

import io.github.loadup.modules.upms.client.dto.AuthUserDTO;
import java.util.Set;

/**
 * UPMS 安全数据网关 供 Security 模块调用，由 Infrastructure 模块实现
 */
public interface AuthGateway {

    /**
     * 根据用户名获取用于认证的用户信息
     *
     * @param username 账号
     * @return 包含密码和权限的 DTO
     */
    AuthUserDTO getAuthUserByUsername(String username);

    /**
     * 根据手机号获取认证信息 (用于短信登录)
     */
    AuthUserDTO getAuthUserByMobile(String mobile);

    /**
     * 更新用户最后登录时间等静态信息
     */
    void updateLastLoginTime(Long userId);

    /**
     * 获取权限
     */
    Set<String> getUserPermissionCodes(String userId);

    AuthUserDTO getAuthUserByUserId(String userId);
}
