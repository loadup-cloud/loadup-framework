/*
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
package io.github.loadup.modules.upms.app.service;

import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.commons.dto.PageQuery;
import io.github.loadup.commons.request.query.IdQuery;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.upms.app.converter.UpmsDTOConverter;
import io.github.loadup.modules.upms.client.command.UserCreateCommand;
import io.github.loadup.modules.upms.client.command.UserPasswordChangeCommand;
import io.github.loadup.modules.upms.client.command.UserUpdateCommand;
import io.github.loadup.modules.upms.client.dto.RoleDTO;
import io.github.loadup.modules.upms.client.dto.UserDetailDTO;
import io.github.loadup.modules.upms.client.query.UserQuery;
import io.github.loadup.modules.upms.domain.entity.Department;
import io.github.loadup.modules.upms.domain.entity.Role;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.DepartmentGateway;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
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
public class UserService implements io.github.loadup.modules.upms.client.facade.UserFacade {
    private final UpmsDTOConverter dtoConverter;

    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final DepartmentGateway departmentGateway;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create user
     */
    @Transactional
    public UserDetailDTO createUser(UserCreateCommand command) {
        rejectMaskedValues(command.getRealName(), command.getEmail(), command.getMobile());
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
        validateDepartment(command.getDeptId());
        validateRoles(command.getRoleIds());

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
        user.setCreatedAt(LocalDateTime.now());

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
        rejectMaskedValues(command.getRealName(), command.getEmail(), command.getMobile());
        User user = userGateway.findById(command.getId()).orElseThrow(() -> new RuntimeException("用户不存在"));

        // Validate email uniqueness (if changed)
        if (command.getEmail() != null
                && !command.getEmail().isBlank()
                && !command.getEmail().equals(user.getEmail())
                && userGateway.existsByEmail(command.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // Validate phone uniqueness (if changed)
        if (command.getMobile() != null
                && !command.getMobile().isBlank()
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
            if (!command.getDeptId().isBlank()) validateDepartment(command.getDeptId());
            user.setDeptId(command.getDeptId().isBlank() ? null : command.getDeptId());
        }
        if (command.getEmail() != null) {
            user.setEmail(command.getEmail().isBlank() ? null : command.getEmail());
            user.setEmailVerified(false);
        }
        if (command.getMobile() != null) {
            user.setMobile(command.getMobile().isBlank() ? null : command.getMobile());
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
        user.setUpdatedAt(LocalDateTime.now());

        user = userGateway.update(user);

        // Update roles
        if (command.getRoleIds() != null) {
            validateRoles(command.getRoleIds());
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
        for (Role role : roleGateway.findByUserId(id)) {
            roleGateway.removeRoleFromUser(id, role.getId());
        }
        userGateway.deleteById(id);
    }

    private static void rejectMaskedValues(String... values) {
        for (String value : values) {
            if (value != null && value.contains("*"))
                throw new IllegalArgumentException("Masked values cannot be saved");
        }
    }

    private void validateDepartment(String deptId) {
        if (deptId != null
                && departmentGateway
                        .findById(deptId)
                        .filter(Department::isEnabled)
                        .isEmpty()) {
            throw new IllegalArgumentException("Department does not exist or is disabled: " + deptId);
        }
    }

    private void validateRoles(List<String> roleIds) {
        if (roleIds == null) return;
        for (String roleId : roleIds) {
            if (roleGateway.findById(roleId).filter(Role::isEnabled).isEmpty()) {
                throw new IllegalArgumentException("Role does not exist or is disabled: " + roleId);
            }
        }
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

        PageResult<User> userPage;
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
                userPage.records().stream().map(this::convertToDetailDTO).collect(Collectors.toList());

        return PageDTO.of(dtoList, userPage.total(), userPage.page(), userPage.size());
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(UserPasswordChangeCommand command) {
        if (command == null
                || command.getOldPassword() == null
                || command.getOldPassword().isBlank()
                || command.getNewPassword() == null
                || command.getNewPassword().length() < 8
                || command.getNewPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72
                || command.getConfirmPassword() == null) {
            throw new IllegalArgumentException("invalid password change request");
        }
        User user = userGateway.findById(command.getUserId()).orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!user.isActive()) throw new IllegalStateException("account is not active");

        // Verify old password
        if (!passwordEncoder.matches(command.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码不正确");
        }

        // Check new password confirmation
        if (!command.getNewPassword().equals(command.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        if (passwordEncoder.matches(command.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("new password must differ from old password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(command.getNewPassword()));
        user.setPasswordUpdateTime(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

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
        List<RoleDTO> roles = roleGateway.findByUserId(user.getId()).stream()
                .map(dtoConverter::toRoleSummary)
                .toList();
        String departmentName = user.getDeptId() == null
                ? null
                : departmentGateway
                        .findById(user.getDeptId())
                        .map(Department::getDeptName)
                        .orElse(null);
        return dtoConverter.toUser(user, departmentName, roles);
    }

    /**
     * Convert Role to RoleDTO
     */
    private RoleDTO convertRoleToDTO(Role role) {
        return dtoConverter.toRoleSummary(role);
    }

    public UserService(
            UserGateway userGateway,
            RoleGateway roleGateway,
            DepartmentGateway departmentGateway,
            PasswordEncoder passwordEncoder,
            UpmsDTOConverter dtoConverter) {
        this.dtoConverter = dtoConverter;
        this.userGateway = userGateway;
        this.roleGateway = roleGateway;
        this.departmentGateway = departmentGateway;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetailDTO getUserById(String userId) {
        return getUserById(new io.github.loadup.commons.request.query.IdQuery(userId));
    }
}
