package io.github.loadup.modules.upms.infrastructure.repository;

/*-
 * #%L
 * loadup-modules-upms-infrastructure
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
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

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.modules.upms.client.dto.AuthUserDTO;
import io.github.loadup.modules.upms.client.gateway.AuthGateway;
import io.github.loadup.modules.upms.infrastructure.dataobject.UserDO;
import io.github.loadup.modules.upms.infrastructure.mapper.UserDOMapper;
import java.util.Collections;
import java.util.Set;
import org.springframework.stereotype.Repository;

/**
 * Security authentication data gateway implementation.
 *
 * <p>Satisfies the {@link AuthGateway} interface from the client layer,
 * separated from {@link UserGatewayImpl} to respect the Interface Segregation Principle.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
public class AuthGatewayImpl implements AuthGateway {

    private final UserDOMapper userDOMapper;

    @Override
    public AuthUserDTO getAuthUserByUsername(String username) {
        QueryWrapper query = QueryWrapper.create().eq("username", username);
        UserDO userDO = userDOMapper.selectOneByQuery(query);
        if (userDO == null) {
            return null;
        }

        AuthUserDTO dto = new AuthUserDTO();
        dto.setUserId(userDO.getId());
        dto.setUsername(userDO.getUsername());
        dto.setPassword(userDO.getPassword());
        dto.setStatus(0);
        dto.setNickname(userDO.getNickname());

        return dto;
    }

    @Override
    public AuthUserDTO getAuthUserByMobile(String mobile) {
        QueryWrapper query = QueryWrapper.create().eq("phone", mobile);
        UserDO userDO = userDOMapper.selectOneByQuery(query);

        if (userDO == null) {
            return null;
        }

        AuthUserDTO dto = new AuthUserDTO();
        dto.setUserId(userDO.getId());
        dto.setUsername(userDO.getUsername());
        dto.setPassword(userDO.getPassword());
        dto.setStatus(0);
        return dto;
    }

    @Override
    public void updateLastLoginTime(Long userId) {
        // TODO: Implement last-login-time update
    }

    @Override
    public Set<String> getUserPermissionCodes(String userId) {
        if (userId == null) {
            return Collections.emptySet();
        }

        // TODO: Implement permission code lookup (requires user_role -> role_menu -> menu join)
        return Collections.emptySet();
    }

    @Override
    public AuthUserDTO getAuthUserByUserId(String userId) {
        // TODO: Implement user lookup by userId (used by token refresh flow)
        UserDO userDO = userDOMapper.selectOneById(userId);
        if (userDO == null) {
            return null;
        }

        AuthUserDTO dto = new AuthUserDTO();
        dto.setUserId(userDO.getId());
        dto.setUsername(userDO.getUsername());
        dto.setPassword(userDO.getPassword());
        dto.setStatus(0);
        dto.setNickname(userDO.getNickname());
        return dto;
    }

    public AuthGatewayImpl(UserDOMapper userDOMapper) {
        this.userDOMapper = userDOMapper;
    }
}
