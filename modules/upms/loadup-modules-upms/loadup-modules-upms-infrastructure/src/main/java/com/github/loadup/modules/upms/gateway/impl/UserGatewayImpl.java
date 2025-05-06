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

import com.github.loadup.commons.error.AssertUtil;
import com.github.loadup.commons.result.CommonResultCodeEnum;
import com.github.loadup.commons.util.PasswordUtils;
import com.github.loadup.modules.upms.convertor.*;
import com.github.loadup.modules.upms.dal.dataobject.*;
import com.github.loadup.modules.upms.dal.repository.*;
import com.github.loadup.modules.upms.domain.UpmsRole;
import com.github.loadup.modules.upms.domain.UpmsUser;
import com.github.loadup.modules.upms.gateway.UserGateway;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserGatewayImpl implements UserGateway {
    @Resource
    private UserRepository userRepository;

    @Resource
    private UserRoleRepository userRoleRepository;

    @Resource
    private UserDepartRepository userDepartRepository;

    @Resource
    private UserPositionRepository userPositionRepository;

    @Resource
    private DepartRepository departRepository;

    @Resource
    private PositionRepository positionRepository;

    @Resource
    private RoleRepository roleRepository;

    @Override
    public UpmsUser create(UpmsUser user) {
        UserDO userDO = UserConvertor.INSTANCE.toUserDO(user);
        String plantPassword = user.getPassword();
        String randomSalt = PasswordUtils.getRandomSalt();
        user.setPassword(PasswordUtils.encrypt(plantPassword, plantPassword, randomSalt));
        userRepository.save(userDO);
        return UserConvertor.INSTANCE.toUser(userDO);
    }

    @Override
    public boolean validatePassword(String userId, String oldPassword) {
        AssertUtil.notBlank(userId, CommonResultCodeEnum.PARAM_ILLEGAL, "userId must not be blank");
        AssertUtil.notBlank(oldPassword, CommonResultCodeEnum.PARAM_ILLEGAL, "oldPassword must not be blank");
        Optional<UserDO> byId = userRepository.findById(userId);
        if (byId.isPresent()) {
            UserDO userDO = byId.get();
            String decrypted = PasswordUtils.decrypt(userDO.getPassword(), oldPassword, userDO.getSalt());
            return StringUtils.equals(decrypted, oldPassword);
        }
        return false;
    }

    @Override
    public void changePassword(String userId, String newPassword) {
        Assert.notNull(userId, "userId must not be null");
        Optional<UserDO> byId = userRepository.findById(userId);
        if (byId.isPresent()) {
            UserDO userDO = byId.get();
            PasswordUtils.encrypt(newPassword, newPassword, userDO.getSalt());
            userRepository.changePassword(userId, userDO.getPassword());
        }

    }

    @Override
    public void delete(String userId) {
        if (StringUtils.isBlank(userId)) {
            return;
        }
        userRepository.deleteById(userId);
    }

    @Override
    public boolean exist(String userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public UpmsUser getById(String userId) {
        UpmsUser user = userRepository.findById(userId).map(UserConvertor.INSTANCE::toUser).orElse(null);
        if (Objects.isNull(user)) {
            return null;
        }
        List<UserRoleDO> userRoleDOList = userRoleRepository.findAllByUserId(userId);
        List<UserDepartDO> userDepartDOList = userDepartRepository.findAllByUserId(userId);
        List<UserPositionDO> userPositionDOList = userPositionRepository.findAllByUserId(userId);
        List<UpmsRole> roleList = userRoleDOList.stream().map(userRoleDO -> roleRepository.findById(userRoleDO.getRoleId())
                .map(RoleConvertor.INSTANCE::toRole).orElse(null)).filter(Objects::nonNull).collect(Collectors.toList());
        user.setRoleList(roleList);
        user.setDepartList(userDepartDOList.stream().map(userRoleDO -> departRepository.findById(userRoleDO.getDepartId())
                .map(DepartConvertor.INSTANCE::toDepart).orElse(null)).filter(Objects::nonNull).collect(Collectors.toList()));
        user.setPositionList(userPositionDOList.stream().map(userRoleDO -> positionRepository.findById(userRoleDO.getPositionId())
                .map(PositionConvertor.INSTANCE::toPosition).orElse(null)).filter(Objects::nonNull).collect(Collectors.toList()));
        return user;
    }

    @Override
    public List<UpmsUser> getByRoleId(String roleId) {
        List<UserRoleDO> userRoleDOList = userRoleRepository.findAllByRoleId(roleId);
        return fetchUserList(userRoleDOList);
    }

    @Override
    public List<UpmsUser> getByRoleIdList(List<String> idList) {
        List<UserRoleDO> userRoleDOList = userRoleRepository.findAllByRoleIdIn(idList);
        return fetchUserList(userRoleDOList);
    }

    private List<UpmsUser> fetchUserList(List<UserRoleDO> userRoleDOList) {
        return userRoleDOList.stream().map(
                userRoleDO -> userRepository.findById(userRoleDO.getUserId()).map(UserConvertor.INSTANCE::toUser)).filter(
                Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    @Override
    public void saveUserRoles(String userId, List<String> roleIds) {
        List<UserRoleDO> userRoleDOList = roleIds.stream().map(roleId -> new UserRoleDO(userId, roleId)).collect(Collectors.toList());
        userRoleRepository.removeAllByUserId(userId);
        userRoleRepository.saveAll(userRoleDOList);
    }

    @Override
    public List<String> getUserRoleList(String userId) {
        List<UserRoleDO> userRoleList = userRoleRepository.findAllByUserId(userId);
        if (CollectionUtils.isEmpty(userRoleList)) {
            return new ArrayList<>();
        }
        return userRoleList.stream().map(v -> roleRepository.findById(v.getRoleId())).filter(Optional::isPresent).map(Optional::get).map(
                RoleDO::getRoleCode).collect(Collectors.toList());
    }

    @Override
    public Set<String> getUserRoleSet(String userId) {
        return new HashSet<>(getUserRoleList(userId));
    }

    @Override
    public Set<String> getUserPermissionsSet(String userId) {
        return Set.of();
    }

    @Override
    public UpmsUser getByAccount(String account) {
        UserDO userDO = userRepository.findByAccount(account);
        return UserConvertor.INSTANCE.toUser(userDO);
    }

    @Override
    public List<UpmsUser> getByDepartIdList(List<String> idList) {
        List<UserDepartDO> doList = userDepartRepository.findAllByDepartIdIn(idList);
        return doList.stream().map(userRoleDO -> userRepository.findById(userRoleDO.getUserId()).map(UserConvertor.INSTANCE::toUser))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    @Override
    public List<UpmsUser> getByDepartId(String departId) {
        List<UserDepartDO> doList = userDepartRepository.findAllByDepartId(departId);
        return doList.stream().map(userRoleDO -> userRepository.findById(userRoleDO.getUserId()).map(UserConvertor.INSTANCE::toUser))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

}
