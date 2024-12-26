package com.github.loadup.components.upms.gateway.impl;

/*-
 * #%L
 * loadup-components-upms-infrastructure
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

import com.github.loadup.components.upms.convertor.*;
import com.github.loadup.components.upms.dal.dataobject.*;
import com.github.loadup.components.upms.dal.repository.*;
import com.github.loadup.components.upms.domain.Role;
import com.github.loadup.components.upms.domain.User;
import com.github.loadup.components.upms.gateway.UserGateway;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserGatewayImpl implements UserGateway {
    @Resource
    private UserRepository         userRepository;
    @Resource
    private UserRoleRepository     userRoleRepository;
    @Resource
    private UserDepartRepository   userDepartRepository;
    @Resource
    private UserPositionRepository userPositionRepository;
    @Resource
    private DepartRepository       departRepository;
    @Resource
    private PositionRepository     positionRepository;
    @Resource
    private RoleRepository         roleRepository;

    @Override
    public User create(User user) {
        UserDO userDO = UserConvertor.INSTANCE.toUserDO(user);
        //userDO.setNew(true);
        userRepository.save(userDO);
        return UserConvertor.INSTANCE.toUser(userDO);
    }

    @Override
    public void changePassword(User user) {
        Assert.notNull(user.getId(), "userId must not be null");
        UserDO userDO = UserConvertor.INSTANCE.toUserDO(user);
        //userDO.setNew(false);
        userRepository.changePassword(user.getId(), userDO.getPassword());
    }

    @Override
    public void delete(String userId) {
        if (StringUtils.isBlank(userId)) {
            return;
        }
        userRepository.deleteById(userId);
    }

    @Override
    public User getById(String userId) {
        User user = userRepository.findById(userId).map(UserConvertor.INSTANCE::toUser).orElse(null);
        if (Objects.isNull(user)) {
            return null;
        }
        List<UserRoleDO> userRoleDOList = userRoleRepository.findAllByUserId(userId);
        List<UserDepartDO> userDepartDOList = userDepartRepository.findAllByUserId(userId);
        List<UserPositionDO> userPositionDOList = userPositionRepository.findAllByUserId(userId);
        List<Role> roleList = userRoleDOList.stream().map(userRoleDO -> roleRepository.findById(userRoleDO.getRoleId())
                .map(RoleConvertor.INSTANCE::toRole).orElse(null)).filter(Objects::nonNull).collect(Collectors.toList());
        user.setRoleList(roleList);
        user.setDepartList(userDepartDOList.stream().map(userRoleDO -> departRepository.findById(userRoleDO.getDepartId())
                .map(DepartConvertor.INSTANCE::toDepart).orElse(null)).filter(Objects::nonNull).collect(Collectors.toList()));
        user.setPositionList(userPositionDOList.stream().map(userRoleDO -> positionRepository.findById(userRoleDO.getPositionId())
                .map(PositionConvertor.INSTANCE::toPosition).orElse(null)).filter(Objects::nonNull).collect(Collectors.toList()));
        return user;
    }

    @Override
    public List<User> getByRoleId(String roleId) {
        List<UserRoleDO> userRoleDOList = userRoleRepository.findAllByRoleId(roleId);
        List<User> result = new ArrayList<>();
        for (UserRoleDO userRoleDO : userRoleDOList) {
            User user = userRepository.findById(userRoleDO.getUserId()).map(UserConvertor.INSTANCE::toUser).orElse(null);
            if (Objects.nonNull(user)) {
                result.add(user);
            }
        }
        return result;
    }

    @Override
    public void saveUserRoles(String userId, List<String> roleIds) {
        List<UserRoleDO> userRoleDOList = roleIds.stream().map(roleId -> new UserRoleDO(userId, roleId)).collect(Collectors.toList());
        userRoleRepository.removeAllByUserId(userId);
        userRoleRepository.saveAll(userRoleDOList);
    }
}
