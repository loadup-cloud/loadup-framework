package com.github.loadup.modules.upms.app.service;

import com.github.loadup.commons.result.PageDTO;
import com.github.loadup.modules.upms.app.dto.UserDetailDTO;
import com.github.loadup.modules.upms.app.query.UserQuery;
import com.github.loadup.modules.upms.domain.entity.Department;
import com.github.loadup.modules.upms.domain.entity.Role;
import com.github.loadup.modules.upms.domain.entity.User;
import com.github.loadup.modules.upms.domain.gateway.DepartmentGateway;
import com.github.loadup.modules.upms.domain.gateway.RoleGateway;
import com.github.loadup.modules.upms.domain.gateway.UserGateway;
import com.github.loadup.upms.api.command.UserCreateCommand;
import com.github.loadup.upms.api.command.UserPasswordChangeCommand;
import com.github.loadup.upms.api.command.UserUpdateCommand;
import com.github.loadup.upms.api.dto.RoleDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Management Service
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserGateway userGateway;
  private final RoleGateway roleGateway;
  private final DepartmentGateway departmentGateway;
  private final PasswordEncoder passwordEncoder;

  /** Create user */
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
    User user =
        User.builder()
            .username(command.getUsername())
            .password(passwordEncoder.encode(command.getPassword()))
            .nickname(command.getNickname())
            .realName(command.getRealName())
            .deptId(command.getDeptId())
            .email(command.getEmail())
            .mobile(command.getMobile())
            .avatar(command.getAvatar())
            .gender(command.getGender())
            .birthday(command.getBirthday())
            .status(command.getStatus() != null ? command.getStatus() : (short) 1)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .emailVerified(false)
            .mobileVerified(false)
            .deleted(false)
            .remark(command.getRemark())
            .createdBy(command.getCreatedBy())
            .createdTime(LocalDateTime.now())
            .build();

    user = userGateway.save(user);

    // Assign roles
    if (command.getRoleIds() != null && !command.getRoleIds().isEmpty()) {
      for (String roleId : command.getRoleIds()) {
        roleGateway.assignRoleToUser(user.getId(), roleId, command.getCreatedBy());
      }
    }

    return convertToDetailDTO(user);
  }

  /** Update user */
  @Transactional
  public UserDetailDTO updateUser(UserUpdateCommand command) {
    User user =
        userGateway.findById(command.getId()).orElseThrow(() -> new RuntimeException("用户不存在"));

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

  /** Delete user */
  @Transactional
  public void deleteUser(String id) {
    userGateway.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
    userGateway.deleteById(id);
  }

  /** Get user by ID */
  public UserDetailDTO getUserById(String id) {
    User user = userGateway.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
    return convertToDetailDTO(user);
  }

  /** Query users with pagination */
  public PageDTO<UserDetailDTO> queryUsers(UserQuery query) {
    Sort sort = Sort.by(Sort.Direction.fromString(query.getSortOrder()), query.getSortBy());
    Pageable pageable = PageRequest.of(query.getPage() - 1, query.getSize(), sort);

    Page<User> userPage;
    if (query.getUsername() != null || query.getEmail() != null || query.getMobile() != null) {
      String keyword = query.getUsername();
      if (keyword == null) keyword = query.getEmail();
      if (keyword == null) keyword = query.getMobile();
      userPage = userGateway.search(keyword, pageable);
    } else {
      userPage = userGateway.findAll(pageable);
    }

    List<UserDetailDTO> dtoList =
        userPage.getContent().stream().map(this::convertToDetailDTO).collect(Collectors.toList());

    return PageDTO.of(dtoList, userPage.getTotalElements(), query.getPage(), query.getSize());
  }

  /** Change user password */
  @Transactional
  public void changePassword(UserPasswordChangeCommand command) {
    User user =
        userGateway.findById(command.getUserId()).orElseThrow(() -> new RuntimeException("用户不存在"));

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

  /** Lock user account */
  @Transactional
  public void lockUser(String id) {
    User user = userGateway.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
    user.setAccountNonLocked(false);
    user.setLockedTime(LocalDateTime.now());
    userGateway.update(user);
  }

  /** Unlock user account */
  @Transactional
  public void unlockUser(String id) {
    User user = userGateway.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
    user.setAccountNonLocked(true);
    user.setLoginFailCount(0);
    user.setLockedTime(null);
    userGateway.update(user);
  }

  /** Convert User entity to UserDetailDTO */
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

  /** Convert Role to RoleDTO */
  private RoleDTO convertRoleToDTO(Role role) {
    return RoleDTO.builder()
        .id(role.getId())
        .roleName(role.getRoleName())
        .roleCode(role.getRoleCode())
        .dataScope(role.getDataScope())
        .status(role.getStatus())
        .build();
  }
}
