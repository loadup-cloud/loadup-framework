package io.github.loadup.modules.upms.infrastructure.repository;

/*-
 * #%L
 * loadup-modules-upms-infrastructure
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
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
