package com.github.loadup.modules.upms.gateway.impl;

/*-
 * #%L
 * loadup-modules-upms-infrastructure
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.modules.upms.convertor.RoleConvertor;
import com.github.loadup.modules.upms.convertor.UserConvertor;
import com.github.loadup.modules.upms.dal.dataobject.RoleDO;
import com.github.loadup.modules.upms.dal.dataobject.UserRoleDO;
import com.github.loadup.modules.upms.dal.repository.RoleRepository;
import com.github.loadup.modules.upms.dal.repository.UserRepository;
import com.github.loadup.modules.upms.dal.repository.UserRoleRepository;
import com.github.loadup.modules.upms.domain.UpmsRole;
import com.github.loadup.modules.upms.domain.UpmsUser;
import com.github.loadup.modules.upms.gateway.RoleGateway;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RoleGatewayImpl implements RoleGateway {
    @Resource
    private RoleRepository roleRepository;

    @Resource
    private UserRoleRepository userRoleRepository;

    @Resource
    private UserRepository userRepository;

    @Override
    public UpmsRole create(UpmsRole role) {
        RoleDO roleDO = RoleConvertor.INSTANCE.toRoleDO(role);
        roleRepository.save(roleDO);
        return RoleConvertor.INSTANCE.toRole(roleDO);
    }

    @Override
    public void update(UpmsRole role) {
        if (StringUtils.isBlank(role.getId())) {
            return;
        }
        roleRepository.save(RoleConvertor.INSTANCE.toRoleDO(role));
    }

    @Override
    public void delete(String roleId) {
        if (StringUtils.isBlank(roleId)) {
            return;
        }
        roleRepository.deleteById(roleId);
    }

    @Override
    public UpmsRole getById(String roleId) {
        UpmsRole role = roleRepository
                .findById(roleId)
                .map(RoleConvertor.INSTANCE::toRole)
                .orElse(null);
        List<UserRoleDO> userRoleDOList = userRoleRepository.findAllByRoleId(roleId);
        List<UpmsUser> userList = userRoleDOList.stream()
                .map(userRoleDO -> userRepository
                        .findById(userRoleDO.getUserId())
                        .map(UserConvertor.INSTANCE::toUser)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        role.setUserList(userList);
        return role;
    }

    @Override
    public List<UpmsRole> getByUserId(String userId) {
        List<UserRoleDO> userRoleDOList = userRoleRepository.findAllByUserId(userId);
        List<UpmsRole> result = new ArrayList<>();
        for (UserRoleDO userRoleDO : userRoleDOList) {
            UpmsRole role = roleRepository
                    .findById(userRoleDO.getUserId())
                    .map(RoleConvertor.INSTANCE::toRole)
                    .orElse(null);
            if (Objects.nonNull(role)) {
                result.add(role);
            }
        }
        return result;
    }

    @Override
    public void saveRoleUsers(String roleId, List<String> userIdList) {
        List<UserRoleDO> userRoleDOList = userIdList.stream()
                .map(userId -> new UserRoleDO(userId, roleId))
                .collect(Collectors.toList());
        userRoleRepository.removeAllByRoleId(roleId);
        userRoleRepository.saveAll(userRoleDOList);
    }
}
