package io.github.loadup.upms.api.gateway;

/*-
 * #%L
 * Loadup Modules UPMS Client Layer
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

import io.github.loadup.upms.api.dto.AuthUserDTO;
import java.util.Set;

/** UPMS 安全数据网关 供 Security 模块调用，由 Infrastructure 模块实现 */
public interface AuthGateway {

    /**
     * 根据用户名获取用于认证的用户信息
     *
     * @param username 账号
     * @return 包含密码和权限的 DTO
     */
    AuthUserDTO getAuthUserByUsername(String username);

    /** 根据手机号获取认证信息 (用于短信登录) */
    AuthUserDTO getAuthUserByMobile(String mobile);

    /** 更新用户最后登录时间等静态信息 */
    void updateLastLoginTime(Long userId);

    /** 获取权限 */
    Set<String> getUserPermissionCodes(String userId);

    AuthUserDTO getAuthUserByUserId(String userId);
}
