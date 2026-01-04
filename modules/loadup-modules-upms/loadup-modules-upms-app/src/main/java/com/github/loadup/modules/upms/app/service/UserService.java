package com.github.loadup.modules.upms.app.service;

import com.github.loadup.modules.upms.app.command.UserCreateCommand;
import com.github.loadup.modules.upms.app.command.UserPasswordChangeCommand;
import com.github.loadup.modules.upms.app.command.UserUpdateCommand;
import com.github.loadup.modules.upms.app.dto.PageResult;
import com.github.loadup.modules.upms.app.dto.RoleDTO;
import com.github.loadup.modules.upms.app.dto.UserDetailDTO;
import com.github.loadup.modules.upms.app.query.UserQuery;
import com.github.loadup.modules.upms.domain.entity.Department;
import com.github.loadup.modules.upms.domain.entity.Role;
import com.github.loadup.modules.upms.domain.entity.User;
import com.github.loadup.modules.upms.domain.repository.DepartmentRepository;
import com.github.loadup.modules.upms.domain.repository.RoleRepository;
import com.github.loadup.modules.upms.domain.repository.UserRepository;
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

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final DepartmentRepository departmentRepository;
  private final PasswordEncoder passwordEncoder;

  /** Create user */
  @Transactional
  public UserDetailDTO createUser(UserCreateCommand command) {
    // Validate username uniqueness
    if (userRepository.existsByUsername(command.getUsername())) {
      throw new RuntimeException("用户名已存在");
    }

    // Validate email uniqueness
    if (command.getEmail() != null && userRepository.existsByEmail(command.getEmail())) {
      throw new RuntimeException("邮箱已被注册");
    }

    // Validate phone uniqueness
    if (command.getPhone() != null && userRepository.existsByPhone(command.getPhone())) {
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
            .phone(command.getPhone())
            .avatarUrl(command.getAvatarUrl())
            .gender(command.getGender())
            .birthday(command.getBirthday())
            .status(command.getStatus() != null ? command.getStatus() : (short) 1)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .emailVerified(false)
            .phoneVerified(false)
            .deleted(false)
            .remark(command.getRemark())
            .createdBy(command.getCreatedBy())
            .createdTime(LocalDateTime.now())
            .build();

    user = userRepository.save(user);

    // Assign roles
    if (command.getRoleIds() != null && !command.getRoleIds().isEmpty()) {
      for (Long roleId : command.getRoleIds()) {
        roleRepository.assignRoleToUser(user.getId(), roleId, command.getCreatedBy());
      }
    }

    return convertToDetailDTO(user);
  }

  /** Update user */
  @Transactional
  public UserDetailDTO updateUser(UserUpdateCommand command) {
    User user =
        userRepository.findById(command.getId()).orElseThrow(() -> new RuntimeException("用户不存在"));

    // Validate email uniqueness (if changed)
    if (command.getEmail() != null
        && !command.getEmail().equals(user.getEmail())
        && userRepository.existsByEmail(command.getEmail())) {
      throw new RuntimeException("邮箱已被注册");
    }

    // Validate phone uniqueness (if changed)
    if (command.getPhone() != null
        && !command.getPhone().equals(user.getPhone())
        && userRepository.existsByPhone(command.getPhone())) {
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
    if (command.getPhone() != null) {
      user.setPhone(command.getPhone());
      user.setPhoneVerified(false);
    }
    if (command.getAvatarUrl() != null) {
      user.setAvatarUrl(command.getAvatarUrl());
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

    user = userRepository.update(user);

    // Update roles
    if (command.getRoleIds() != null) {
      // Remove old roles
      List<Role> currentRoles = roleRepository.findByUserId(user.getId());
      for (Role role : currentRoles) {
        roleRepository.removeRoleFromUser(user.getId(), role.getId());
      }
      // Assign new roles
      for (Long roleId : command.getRoleIds()) {
        roleRepository.assignRoleToUser(user.getId(), roleId, command.getUpdatedBy());
      }
    }

    return convertToDetailDTO(user);
  }

  /** Delete user */
  @Transactional
  public void deleteUser(Long id) {
    userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
    userRepository.deleteById(id);
  }

  /** Get user by ID */
  public UserDetailDTO getUserById(Long id) {
    User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
    return convertToDetailDTO(user);
  }

  /** Query users with pagination */
  public PageResult<UserDetailDTO> queryUsers(UserQuery query) {
    Sort sort = Sort.by(Sort.Direction.fromString(query.getSortOrder()), query.getSortBy());
    Pageable pageable = PageRequest.of(query.getPage() - 1, query.getSize(), sort);

    Page<User> userPage;
    if (query.getUsername() != null || query.getEmail() != null || query.getPhone() != null) {
      String keyword = query.getUsername();
      if (keyword == null) keyword = query.getEmail();
      if (keyword == null) keyword = query.getPhone();
      userPage = userRepository.search(keyword, pageable);
    } else {
      userPage = userRepository.findAll(pageable);
    }

    List<UserDetailDTO> dtoList =
        userPage.getContent().stream().map(this::convertToDetailDTO).collect(Collectors.toList());

    return PageResult.of(dtoList, userPage.getTotalElements(), query.getPage(), query.getSize());
  }

  /** Change user password */
  @Transactional
  public void changePassword(UserPasswordChangeCommand command) {
    User user =
        userRepository
            .findById(command.getUserId())
            .orElseThrow(() -> new RuntimeException("用户不存在"));

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

    userRepository.update(user);
  }

  /** Lock user account */
  @Transactional
  public void lockUser(Long id) {
    User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
    user.setAccountNonLocked(false);
    user.setLockedTime(LocalDateTime.now());
    userRepository.update(user);
  }

  /** Unlock user account */
  @Transactional
  public void unlockUser(Long id) {
    User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
    user.setAccountNonLocked(true);
    user.setLoginFailCount(0);
    user.setLockedTime(null);
    userRepository.update(user);
  }

  /** Convert User entity to UserDetailDTO */
  private UserDetailDTO convertToDetailDTO(User user) {
    List<Role> roles = roleRepository.findByUserId(user.getId());
    Department dept = null;
    if (user.getDeptId() != null) {
      dept = departmentRepository.findById(user.getDeptId()).orElse(null);
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
        .phone(user.getPhone())
        .phoneVerified(user.getPhoneVerified())
        .avatarUrl(user.getAvatarUrl())
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
