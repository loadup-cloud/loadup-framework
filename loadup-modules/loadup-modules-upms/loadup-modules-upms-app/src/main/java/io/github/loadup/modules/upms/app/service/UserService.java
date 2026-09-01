package io.github.loadup.modules.upms.app.service;

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

import io.github.loadup.commons.dto.PageQuery;
import io.github.loadup.commons.request.query.IdQuery;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.upms.app.dto.UserDetailDTO;
import io.github.loadup.modules.upms.app.query.UserQuery;
import io.github.loadup.modules.upms.client.command.UserCreateCommand;
import io.github.loadup.modules.upms.client.command.UserPasswordChangeCommand;
import io.github.loadup.modules.upms.client.command.UserUpdateCommand;
import io.github.loadup.modules.upms.client.dto.RoleDTO;
import io.github.loadup.modules.upms.domain.entity.Department;
import io.github.loadup.modules.upms.domain.entity.Role;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.DepartmentGateway;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Management Service
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final DepartmentGateway departmentGateway;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create user
     */
    @Transactional
    public UserDetailDTO createUser(UserCreateCommand command) {
        // Validate username uniqueness
        if (userGateway.existsByUsername(command.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // Validate email uniqueness
        if (command.getEmail() != null && userGateway.existsByEmail(command.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // Validate phone uniqueness
        if (command.getMobile() != null && userGateway.existsByMobile(command.getMobile())) {
            throw new RuntimeException("手机号已被注册");
        }

        // Create user entity
        User user = new User();
        user.setUsername(command.getUsername());
        user.setPassword(passwordEncoder.encode(command.getPassword()));
        user.setNickname(command.getNickname());
        user.setRealName(command.getRealName());
        user.setDeptId(command.getDeptId());
        user.setEmail(command.getEmail());
        user.setMobile(command.getMobile());
        user.setAvatar(command.getAvatar());
        user.setGender(command.getGender());
        user.setBirthday(command.getBirthday());
        user.setStatus(command.getStatus() != null ? command.getStatus() : (short) 1);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEmailVerified(false);
        user.setMobileVerified(false);
        user.setDeleted(false);
        user.setLoginFailCount(0);
        user.setRemark(command.getRemark());
        user.setCreatedBy(command.getCreatedBy());
        user.setCreatedTime(LocalDateTime.now());

        user = userGateway.save(user);

        // Assign roles
        if (command.getRoleIds() != null && !command.getRoleIds().isEmpty()) {
            for (String roleId : command.getRoleIds()) {
                roleGateway.assignRoleToUser(user.getId(), roleId, command.getCreatedBy());
            }
        }

        return convertToDetailDTO(user);
    }

    /**
     * Update user
     */
    @Transactional
    public UserDetailDTO updateUser(UserUpdateCommand command) {
        User user = userGateway.findById(command.getId()).orElseThrow(() -> new RuntimeException("用户不存在"));

        // Validate email uniqueness (if changed)
        if (command.getEmail() != null
                && !command.getEmail().equals(user.getEmail())
                && userGateway.existsByEmail(command.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // Validate phone uniqueness (if changed)
        if (command.getMobile() != null
                && !command.getMobile().equals(user.getMobile())
                && userGateway.existsByMobile(command.getMobile())) {
            throw new RuntimeException("手机号已被注册");
        }

        // Update user fields
        if (command.getNickname() != null) {
            user.setNickname(command.getNickname());
        }
        if (command.getRealName() != null) {
            user.setRealName(command.getRealName());
        }
        if (command.getDeptId() != null) {
            user.setDeptId(command.getDeptId());
        }
        if (command.getEmail() != null) {
            user.setEmail(command.getEmail());
            user.setEmailVerified(false);
        }
        if (command.getMobile() != null) {
            user.setMobile(command.getMobile());
            user.setMobileVerified(false);
        }
        if (command.getAvatar() != null) {
            user.setAvatar(command.getAvatar());
        }
        if (command.getGender() != null) {
            user.setGender(command.getGender());
        }
        if (command.getBirthday() != null) {
            user.setBirthday(command.getBirthday());
        }
        if (command.getStatus() != null) {
            user.setStatus(command.getStatus());
        }
        if (command.getRemark() != null) {
            user.setRemark(command.getRemark());
        }

        user.setUpdatedBy(command.getUpdatedBy());
        user.setUpdatedTime(LocalDateTime.now());

        user = userGateway.update(user);

        // Update roles
        if (command.getRoleIds() != null) {
            // Remove old roles
            List<Role> currentRoles = roleGateway.findByUserId(user.getId());
            for (Role role : currentRoles) {
                roleGateway.removeRoleFromUser(user.getId(), role.getId());
            }
            // Assign new roles
            for (String roleId : command.getRoleIds()) {
                roleGateway.assignRoleToUser(user.getId(), roleId, command.getUpdatedBy());
            }
        }

        return convertToDetailDTO(user);
    }

    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(String id) {
        userGateway.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
        userGateway.deleteById(id);
    }

    /**
     * Get user by ID
     */
    public UserDetailDTO getUserById(IdQuery idQuery) {
        User user = userGateway.findById(idQuery.id()).orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToDetailDTO(user);
    }

    /**
     * Query users with pagination
     */
    public PageDTO<UserDetailDTO> queryUsers(UserQuery query) {
        PageQuery pageQuery = PageQuery.of(query.getPage(), query.getSize());

        PageDTO<User> userPage;
        if (query.getUsername() != null || query.getEmail() != null || query.getMobile() != null) {
            String keyword = query.getUsername();
            if (keyword == null) {
                keyword = query.getEmail();
            }
            if (keyword == null) {
                keyword = query.getMobile();
            }
            userPage = userGateway.search(keyword, pageQuery);
        } else {
            userPage = userGateway.findAll(pageQuery);
        }

        List<UserDetailDTO> dtoList =
                userPage.getData().stream().map(this::convertToDetailDTO).collect(Collectors.toList());

        return PageDTO.of(dtoList, userPage.getPageInfo().totalCount(), query.getPage(), query.getSize());
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(UserPasswordChangeCommand command) {
        User user = userGateway.findById(command.getUserId()).orElseThrow(() -> new RuntimeException("用户不存在"));

        // Verify old password
        if (!passwordEncoder.matches(command.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码不正确");
        }

        // Check new password confirmation
        if (!command.getNewPassword().equals(command.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(command.getNewPassword()));
        user.setPasswordUpdateTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());

        userGateway.update(user);
    }

    /**
     * Lock user account
     */
    @Transactional
    public void lockUser(String id) {
        User user = userGateway.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setAccountNonLocked(false);
        user.setLockedTime(LocalDateTime.now());
        userGateway.update(user);
    }

    /**
     * Unlock user account
     */
    @Transactional
    public void unlockUser(String id) {
        User user = userGateway.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setAccountNonLocked(true);
        user.setLoginFailCount(0);
        user.setLockedTime(null);
        userGateway.update(user);
    }

    /**
     * Convert User entity to UserDetailDTO
     */
    private UserDetailDTO convertToDetailDTO(User user) {
        List<Role> roles = roleGateway.findByUserId(user.getId());
        Department dept = null;
        if (user.getDeptId() != null) {
            dept = departmentGateway.findById(user.getDeptId()).orElse(null);
        }

        return UserDetailDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .realName(user.getRealName())
                .deptId(user.getDeptId())
                .deptName(dept != null ? dept.getDeptName() : null)
                .email(user.getEmail())
                .emailVerified(user.getEmailVerified())
                .mobile(user.getMobile())
                .mobileVerified(user.getMobileVerified())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .status(user.getStatus())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .roles(roles.stream().map(this::convertRoleToDTO).collect(Collectors.toList()))
                .remark(user.getRemark())
                .createdTime(user.getCreatedTime())
                .updatedTime(user.getUpdatedTime())
                .build();
    }

    /**
     * Convert Role to RoleDTO
     */
    private RoleDTO convertRoleToDTO(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .roleCode(role.getRoleCode())
                .dataScope(role.getDataScope())
                .status(role.getStatus())
                .build();
    }

    public UserService(
            UserGateway userGateway,
            RoleGateway roleGateway,
            DepartmentGateway departmentGateway,
            PasswordEncoder passwordEncoder) {
        this.userGateway = userGateway;
        this.roleGateway = roleGateway;
        this.departmentGateway = departmentGateway;
        this.passwordEncoder = passwordEncoder;
    }
}
