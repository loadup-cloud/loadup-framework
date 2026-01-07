package com.github.loadup.modules.upms.app.service;

import com.github.loadup.modules.upms.app.command.RoleCreateCommand;
import com.github.loadup.modules.upms.app.command.RoleUpdateCommand;
import com.github.loadup.modules.upms.app.dto.PageResult;
import com.github.loadup.modules.upms.app.dto.PermissionDTO;
import com.github.loadup.modules.upms.app.dto.RoleDTO;
import com.github.loadup.modules.upms.app.query.RoleQuery;
import com.github.loadup.modules.upms.domain.entity.Permission;
import com.github.loadup.modules.upms.domain.entity.Role;
import com.github.loadup.modules.upms.domain.repository.PermissionRepository;
import com.github.loadup.modules.upms.domain.repository.RoleRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Role Management Service
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;

  /** Create role */
  @Transactional
  public RoleDTO createRole(RoleCreateCommand command) {
    // Validate role code uniqueness
    if (roleRepository.existsByRoleCode(command.getRoleCode())) {
      throw new RuntimeException("角色编码已存在");
    }

    // Validate parent role exists
    if (command.getParentRoleId() != null) {
      roleRepository
          .findById(command.getParentRoleId())
          .orElseThrow(() -> new RuntimeException("父角色不存在"));
    }

    // Create role entity
    Role role =
        Role.builder()
            .roleName(command.getRoleName())
            .roleCode(command.getRoleCode())
            .parentRoleId(command.getParentRoleId())
            .dataScope(command.getDataScope() != null ? command.getDataScope() : (short) 1)
            .sortOrder(command.getSortOrder())
            .status(command.getStatus() != null ? command.getStatus() : (short) 1)
            .deleted(false)
            .remark(command.getRemark())
            .createdBy(command.getCreatedBy())
            .createdTime(LocalDateTime.now())
            .build();

    role = roleRepository.save(role);

    // Assign permissions
    if (command.getPermissionIds() != null && !command.getPermissionIds().isEmpty()) {
      roleRepository.assignPermissionsToRole(role.getId(), command.getPermissionIds());
    }

    // Assign departments (for custom data scope)
    if (command.getDataScope() == 2
        && command.getDepartmentIds() != null
        && !command.getDepartmentIds().isEmpty()) {
      roleRepository.assignDepartmentsToRole(role.getId(), command.getDepartmentIds());
    }

    return convertToDTO(role);
  }

  /** Update role */
  @Transactional
  public RoleDTO updateRole(RoleUpdateCommand command) {
    Role role =
        roleRepository.findById(command.getId()).orElseThrow(() -> new RuntimeException("角色不存在"));

    // Validate parent role (prevent circular reference)
    if (command.getParentRoleId() != null) {
      if (command.getParentRoleId().equals(command.getId())) {
        throw new RuntimeException("父角色不能是自己");
      }
      roleRepository
          .findById(command.getParentRoleId())
          .orElseThrow(() -> new RuntimeException("父角色不存在"));
    }

    // Update role fields
    if (command.getRoleName() != null) {
      role.setRoleName(command.getRoleName());
    }
    if (command.getParentRoleId() != null) {
      role.setParentRoleId(command.getParentRoleId());
    }
    if (command.getDataScope() != null) {
      role.setDataScope(command.getDataScope());
    }
    if (command.getSortOrder() != null) {
      role.setSortOrder(command.getSortOrder());
    }
    if (command.getStatus() != null) {
      role.setStatus(command.getStatus());
    }
    if (command.getRemark() != null) {
      role.setRemark(command.getRemark());
    }

    role.setUpdatedBy(command.getUpdatedBy());
    role.setUpdatedTime(LocalDateTime.now());

    role = roleRepository.update(role);

    // Update permissions
    if (command.getPermissionIds() != null) {
      List<Permission> currentPermissions = permissionRepository.findByRoleId(role.getId());
      List<String> currentPermissionIds =
          currentPermissions.stream().map(Permission::getId).collect(Collectors.toList());
      if (!currentPermissionIds.isEmpty()) {
        roleRepository.removePermissionsFromRole(role.getId(), currentPermissionIds);
      }
      if (!command.getPermissionIds().isEmpty()) {
        roleRepository.assignPermissionsToRole(role.getId(), command.getPermissionIds());
      }
    }

    // Update departments (for custom data scope)
    if (command.getDataScope() != null && command.getDataScope() == 2) {
      if (command.getDepartmentIds() != null) {
        List<String> currentDeptIds = roleRepository.findDepartmentIdsByRoleId(role.getId());
        if (!currentDeptIds.isEmpty()) {
          roleRepository.removeDepartmentsFromRole(role.getId(), currentDeptIds);
        }
        if (!command.getDepartmentIds().isEmpty()) {
          roleRepository.assignDepartmentsToRole(role.getId(), command.getDepartmentIds());
        }
      }
    }

    return convertToDTO(role);
  }

  /** Delete role */
  @Transactional
  public void deleteRole(String id) {
    roleRepository.findById(id).orElseThrow(() -> new RuntimeException("角色不存在"));

    // Check if role has child roles
    List<Role> childRoles = roleRepository.findByParentRoleId(id);
    if (!childRoles.isEmpty()) {
      throw new RuntimeException("该角色下存在子角色，无法删除");
    }

    // Check if role is assigned to users
    long userCount = roleRepository.countUsersByRoleId(id);
    if (userCount > 0) {
      throw new RuntimeException("该角色已分配给用户，无法删除");
    }

    roleRepository.deleteById(id);
  }

  /** Get role by ID */
  public RoleDTO getRoleById(String id) {
    Role role = roleRepository.findById(id).orElseThrow(() -> new RuntimeException("角色不存在"));
    return convertToDTO(role);
  }

  /** Query roles with pagination */
  public PageResult<RoleDTO> queryRoles(RoleQuery query) {
    // Sort sort = Sort.by(Sort.Direction.fromString(query.getSortOrder()), query.getSortBy());
    // Pageable pageable = PageRequest.of(query.getPage() - 1, query.getSize(), sort);

    List<Role> allRoles = roleRepository.findAll();

    // Simple pagination manually since repository might not support it
    int start = (query.getPage() - 1) * query.getSize();
    int end = Math.min(start + query.getSize(), allRoles.size());
    List<Role> pageContent = allRoles.subList(start, end);

    List<RoleDTO> dtoList =
        pageContent.stream().map(this::convertToDTO).collect(Collectors.toList());

    return PageResult.of(dtoList, (long) allRoles.size(), query.getPage(), query.getSize());
  }

  /** Get role tree (hierarchy) */
  public List<RoleDTO> getRoleTree() {
    List<Role> allRoles = roleRepository.findAll();
    return buildRoleTree(allRoles, null);
  }

  /** Assign role to user */
  @Transactional
  public void assignRoleToUser(String roleId, String userId) {
    roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("角色不存在"));
    roleRepository.assignRoleToUser(userId, roleId, "0");
  }

  /** Remove role from user */
  @Transactional
  public void removeRoleFromUser(String roleId, String userId) {
    roleRepository.removeRoleFromUser(userId, roleId);
  }

  /** Assign permissions to role */
  @Transactional
  public void assignPermissionsToRole(String roleId, List<String> permissionIds) {
    roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("角色不存在"));

    for (String permissionId : permissionIds) {
      permissionRepository
          .findById(permissionId)
          .orElseThrow(() -> new RuntimeException("权限不存在: " + permissionId));
    }

    roleRepository.assignPermissionsToRole(roleId, permissionIds);
  }

  /** Convert Role entity to RoleDTO */
  private RoleDTO convertToDTO(Role role) {
    List<Permission> permissions = permissionRepository.findByRoleId(role.getId());
    List<String> departmentIds = roleRepository.findDepartmentIdsByRoleId(role.getId());

    Role parentRole = null;
    if (role.getParentRoleId() != null) {
      parentRole = roleRepository.findById(role.getParentRoleId()).orElse(null);
    }

    return RoleDTO.builder()
        .id(role.getId())
        .roleName(role.getRoleName())
        .roleCode(role.getRoleCode())
        .parentRoleId(role.getParentRoleId())
        .parentRoleName(parentRole != null ? parentRole.getRoleName() : null)
        .roleLevel(role.getRoleLevel())
        .dataScope(role.getDataScope())
        .sortOrder(role.getSortOrder())
        .status(role.getStatus())
        .permissions(
            permissions.stream().map(this::convertPermissionToDTO).collect(Collectors.toList()))
        .departmentIds(departmentIds)
        .remark(role.getRemark())
        .createdTime(role.getCreatedTime())
        .updatedTime(role.getUpdatedTime())
        .build();
  }

  /** Convert Permission to PermissionDTO */
  private PermissionDTO convertPermissionToDTO(Permission permission) {
    return PermissionDTO.builder()
        .id(permission.getId())
        .permissionName(permission.getPermissionName())
        .permissionCode(permission.getPermissionCode())
        .permissionType(permission.getPermissionType())
        .build();
  }

  /** Build role tree recursively */
  private List<RoleDTO> buildRoleTree(List<Role> allRoles, Long parentId) {
    List<RoleDTO> tree = new ArrayList<>();
    for (Role role : allRoles) {
      if ((parentId == null && role.getParentRoleId() == null)
          || (parentId != null && parentId.equals(role.getParentRoleId()))) {
        RoleDTO dto = convertToDTO(role);
        tree.add(dto);
      }
    }
    return tree;
  }
}
