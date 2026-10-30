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
import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.request.query.IdQuery;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.upms.app.converter.UpmsDTOConverter;
import io.github.loadup.modules.upms.client.command.UserCreateCommand;
import io.github.loadup.modules.upms.client.command.UserPasswordChangeCommand;
import io.github.loadup.modules.upms.client.command.UserUpdateCommand;
import io.github.loadup.modules.upms.client.dto.RoleDTO;
import io.github.loadup.modules.upms.client.dto.UserDetailDTO;
import io.github.loadup.modules.upms.client.error.UpmsResultCodeEnum;
import io.github.loadup.modules.upms.client.query.UserPageQuery;
import io.github.loadup.modules.upms.client.validation.PasswordPolicy;
import io.github.loadup.modules.upms.domain.gateway.DepartmentGateway;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.model.Department;
import io.github.loadup.modules.upms.domain.model.Role;
import io.github.loadup.modules.upms.domain.model.User;
import java.time.Clock;
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
    private final Clock clock;
    private final UpmsDTOConverter dtoConverter;

    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final DepartmentGateway departmentGateway;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserGateway userGateway,
            RoleGateway roleGateway,
            DepartmentGateway departmentGateway,
            PasswordEncoder passwordEncoder,
            UpmsDTOConverter dtoConverter,
            Clock clock) {
        this.clock = clock;
        this.dtoConverter = dtoConverter;
        this.userGateway = userGateway;
        this.roleGateway = roleGateway;
        this.departmentGateway = departmentGateway;
        this.passwordEncoder = passwordEncoder;
    }

    private static void rejectMaskedValues(String... values) {
        for (String value : values) {
            if (value != null && value.contains("*")) {
                throw new CommonException(UpmsResultCodeEnum.MASKED_VALUES_CANNOT_BE_SAVED);
            }
        }
    }

    /**
     * Create user
     */
    @Transactional
    public UserDetailDTO createUser(UserCreateCommand command) {
        if (command == null) {
            throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL);
        }
        PasswordPolicy.requireValid(command.getPassword());
        rejectMaskedValues(command.getRealName(), command.getEmail(), command.getMobile());
        // Validate username uniqueness
        if (userGateway.existsByUsername(command.getUsername())) {
            throw new CommonException(UpmsResultCodeEnum.USERNAME_ALREADY_EXISTS);
        }

        // Validate email uniqueness
        if (command.getEmail() != null && userGateway.existsByEmail(command.getEmail())) {
            throw new CommonException(UpmsResultCodeEnum.EMAIL_IS_ALREADY_REGISTERED);
        }

        // Validate phone uniqueness
        if (command.getMobile() != null && userGateway.existsByMobile(command.getMobile())) {
            throw new CommonException(UpmsResultCodeEnum.PHONE_NUMBER_IS_ALREADY_REGISTERED);
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
        User user = userGateway
                .findById(command.getId())
                .orElseThrow(() -> new CommonException(UpmsResultCodeEnum.USER_NOT_FOUND));

        // Validate email uniqueness (if changed)
        if (command.getEmail() != null
                && !command.getEmail().isBlank()
                && !command.getEmail().equals(user.getEmail())
                && userGateway.existsByEmail(command.getEmail())) {
            throw new CommonException(UpmsResultCodeEnum.EMAIL_IS_ALREADY_REGISTERED);
        }

        // Validate phone uniqueness (if changed)
        if (command.getMobile() != null
                && !command.getMobile().isBlank()
                && !command.getMobile().equals(user.getMobile())
                && userGateway.existsByMobile(command.getMobile())) {
            throw new CommonException(UpmsResultCodeEnum.PHONE_NUMBER_IS_ALREADY_REGISTERED);
        }

        // Update user fields
        if (command.getNickname() != null) {
            user.setNickname(command.getNickname());
        }
        if (command.getRealName() != null) {
            user.setRealName(command.getRealName());
        }
        if (command.getDeptId() != null) {
            if (!command.getDeptId().isBlank()) {
                validateDepartment(command.getDeptId());
            }
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
        userGateway.findById(id).orElseThrow(() -> new CommonException(UpmsResultCodeEnum.USER_NOT_FOUND));
        for (Role role : roleGateway.findByUserId(id)) {
            roleGateway.removeRoleFromUser(id, role.getId());
        }
        userGateway.deleteById(id);
    }

    private void validateDepartment(String deptId) {
        if (deptId != null
                && departmentGateway
                        .findById(deptId)
                        .filter(Department::isEnabled)
                        .isEmpty()) {
            throw new CommonException(UpmsResultCodeEnum.DEPARTMENT_DOES_NOT_EXIST_OR_IS_DISABLED);
        }
    }

    private void validateRoles(List<String> roleIds) {
        if (roleIds == null) {
            return;
        }
        for (String roleId : roleIds) {
            if (roleGateway.findById(roleId).filter(Role::isEnabled).isEmpty()) {
                throw new CommonException(UpmsResultCodeEnum.ROLE_DOES_NOT_EXIST_OR_IS_DISABLED);
            }
        }
    }

    /**
     * Get user by ID
     */
    public UserDetailDTO getUserById(IdQuery idQuery) {
        User user = userGateway
                .findById(idQuery.id())
                .orElseThrow(() -> new CommonException(UpmsResultCodeEnum.USER_NOT_FOUND));
        return convertToDetailDTO(user);
    }

    /**
     * Query users with pagination
     */
    public PageDTO<UserDetailDTO> queryUsers(UserPageQuery query) {
        var pagination = query.pagination();
        String sortBy = query.getSortBy() == null ? "createdAt" : query.getSortBy();
        String sortOrder = query.getSortOrder() == null ? "DESC" : query.getSortOrder();
        if (!java.util.Set.of("createdAt", "username", "nickname", "id").contains(sortBy)
                || !java.util.Set.of("ASC", "DESC").contains(sortOrder.toUpperCase(java.util.Locale.ROOT))) {
            throw new CommonException(UpmsResultCodeEnum.UNSUPPORTED_USER_SORT_FIELD_OR_DIRECTION);
        }
        PageQuery pageQuery = pagination;

        PageResult<User> userPage;
        if (query.getUsername() != null || query.getEmail() != null || query.getMobile() != null) {
            String keyword = query.getUsername();
            if (keyword == null) {
                keyword = query.getEmail();
            }
            if (keyword == null) {
                keyword = query.getMobile();
            }
            userPage = userGateway.search(keyword, pageQuery, sortBy, sortOrder);
        } else {
            userPage = userGateway.findAll(pageQuery, sortBy, sortOrder);
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
                || command.getConfirmPassword() == null) {
            throw new CommonException(UpmsResultCodeEnum.INVALID_PASSWORD_CHANGE_REQUEST);
        }
        PasswordPolicy.requireValid(command.getNewPassword());
        User user = userGateway
                .findById(command.getUserId())
                .orElseThrow(() -> new CommonException(UpmsResultCodeEnum.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new CommonException(UpmsResultCodeEnum.ACCOUNT_IS_NOT_ACTIVE);
        }

        // Verify old password
        if (!passwordEncoder.matches(command.getOldPassword(), user.getPassword())) {
            throw new CommonException(UpmsResultCodeEnum.CURRENT_PASSWORD_IS_INCORRECT);
        }

        // Check new password confirmation
        if (!command.getNewPassword().equals(command.getConfirmPassword())) {
            throw new CommonException(UpmsResultCodeEnum.PASSWORD_CONFIRMATION_DOES_NOT_MATCH);
        }
        if (passwordEncoder.matches(command.getNewPassword(), user.getPassword())) {
            throw new CommonException(UpmsResultCodeEnum.PASSWORD_REUSE_FORBIDDEN);
        }

        // Update password
        user.setPassword(passwordEncoder.encode(command.getNewPassword()));
        user.setPasswordUpdateTime(LocalDateTime.ofInstant(clock.instant(), clock.getZone()));

        userGateway.update(user);
    }

    /**
     * Lock user account
     */
    @Transactional
    public void lockUser(String id) {
        User user = userGateway.findById(id).orElseThrow(() -> new CommonException(UpmsResultCodeEnum.USER_NOT_FOUND));
        user.setAccountNonLocked(false);
        user.setLockedTime(LocalDateTime.ofInstant(clock.instant(), clock.getZone()));
        userGateway.update(user);
    }

    /**
     * Unlock user account
     */
    @Transactional
    public void unlockUser(String id) {
        User user = userGateway.findById(id).orElseThrow(() -> new CommonException(UpmsResultCodeEnum.USER_NOT_FOUND));
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

    @Override
    public UserDetailDTO getUserById(String userId) {
        return getUserById(new IdQuery(userId));
    }
}
