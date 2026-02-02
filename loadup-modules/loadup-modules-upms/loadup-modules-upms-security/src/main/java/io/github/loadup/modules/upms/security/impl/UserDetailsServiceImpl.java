package io.github.loadup.modules.upms.security.impl;

/*-
 * #%L
 * Loadup Modules UPMS Security Layer
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

import io.github.loadup.modules.upms.security.core.SecurityUser;
import io.github.loadup.upms.api.dto.AuthUserDTO;
import io.github.loadup.upms.api.gateway.AuthGateway;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AuthGateway authGateway; // 注入 api 中定义的网关

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 调用 api 层定义的契约获取数据
        AuthUserDTO authUser = authGateway.getAuthUserByUsername(username);
        if (authUser == null) {
            throw new UsernameNotFoundException("用户 " + username + " 不存在");
        }

        if (!authUser.actived()) {
            throw new UsernameNotFoundException("User is not active: " + username);
        }

        Set<String> permissions = authGateway.getUserPermissionCodes(authUser.getUserId());

        return new SecurityUser(authUser, permissions);
    }
}
