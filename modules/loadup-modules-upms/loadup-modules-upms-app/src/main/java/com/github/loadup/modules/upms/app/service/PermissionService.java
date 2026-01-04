package com.github.loadup.modules.upms.app.service;

import com.github.loadup.modules.upms.app.command.PermissionCreateCommand;
import com.github.loadup.modules.upms.app.command.PermissionUpdateCommand;
import com.github.loadup.modules.upms.app.dto.PermissionDTO;
import com.github.loadup.modules.upms.domain.entity.Permission;
import com.github.loadup.modules.upms.domain.repository.PermissionRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Permission Management Service
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

  private final PermissionRepository permissionRepository;

  @Transactional
  public PermissionDTO createPermission(PermissionCreateCommand command) {
    if (permissionRepository.existsByPermissionCode(command.getPermissionCode())) {
      throw new RuntimeException("权限编码已存在");
    }

    if (command.getParentId() != null && command.getParentId() != "0") {
      permissionRepository
          .findById(command.getParentId())
          .orElseThrow(() -> new RuntimeException("父权限不存在"));
    }

    Permission permission =
        Permission.builder()
            .parentId(command.getParentId())
            .permissionName(command.getPermissionName())
            .permissionCode(command.getPermissionCode())
            .permissionType(command.getPermissionType())
            .resourcePath(command.getResourcePath())
            .httpMethod(command.getHttpMethod())
            .icon(command.getIcon())
            .componentPath(command.getComponentPath())
            .sortOrder(command.getSortOrder())
            .visible(command.getVisible() != null ? command.getVisible() : true)
            .status(command.getStatus() != null ? command.getStatus() : (short) 1)
            .deleted(false)
            .remark(command.getRemark())
            .createdBy(command.getCreatedBy())
            .createdTime(LocalDateTime.now())
            .build();

    permission = permissionRepository.save(permission);
    return convertToDTO(permission);
  }

  @Transactional
  public PermissionDTO updatePermission(PermissionUpdateCommand command) {
    Permission permission =
        permissionRepository
            .findById(command.getId())
            .orElseThrow(() -> new RuntimeException("权限不存在"));

    if (command.getParentId() != null && command.getParentId() != "0") {
      if (command.getParentId().equals(command.getId())) {
        throw new RuntimeException("父权限不能是自己");
      }
      permissionRepository
          .findById(command.getParentId())
          .orElseThrow(() -> new RuntimeException("父权限不存在"));
    }

    if (command.getParentId() != null) permission.setParentId(command.getParentId());
    if (command.getPermissionName() != null)
      permission.setPermissionName(command.getPermissionName());
    if (command.getPermissionType() != null)
      permission.setPermissionType(command.getPermissionType());
    if (command.getResourcePath() != null) permission.setResourcePath(command.getResourcePath());
    if (command.getHttpMethod() != null) permission.setHttpMethod(command.getHttpMethod());
    if (command.getIcon() != null) permission.setIcon(command.getIcon());
    if (command.getComponentPath() != null) permission.setComponentPath(command.getComponentPath());
    if (command.getSortOrder() != null) permission.setSortOrder(command.getSortOrder());
    if (command.getVisible() != null) permission.setVisible(command.getVisible());
    if (command.getStatus() != null) permission.setStatus(command.getStatus());
    if (command.getRemark() != null) permission.setRemark(command.getRemark());

    permission.setUpdatedBy(command.getUpdatedBy());
    permission.setUpdatedTime(LocalDateTime.now());

    permission = permissionRepository.update(permission);
    return convertToDTO(permission);
  }

  @Transactional
  public void deletePermission(String id) {
    permissionRepository.findById(id).orElseThrow(() -> new RuntimeException("权限不存在"));

    List<Permission> children = permissionRepository.findByParentId(id);
    if (!children.isEmpty()) {
      throw new RuntimeException("该权限下存在子权限，无法删除");
    }

    permissionRepository.deleteById(id);
  }

  public PermissionDTO getPermissionById(String id) {
    Permission permission =
        permissionRepository.findById(id).orElseThrow(() -> new RuntimeException("权限不存在"));
    return convertToDTO(permission);
  }

  public List<PermissionDTO> getPermissionTree() {
    List<Permission> allPermissions = permissionRepository.findAll();
    return buildPermissionTree(allPermissions, null);
  }

  public List<PermissionDTO> getPermissionsByType(Short permissionType) {
    List<Permission> permissions = permissionRepository.findByPermissionType(permissionType);
    return permissions.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  public List<PermissionDTO> getUserPermissions(String userId) {
    List<Permission> permissions = permissionRepository.findByUserId(userId);
    return permissions.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  public List<PermissionDTO> getUserMenuTree(String userId) {
    List<Permission> menuPermissions =
        permissionRepository.findByUserId(userId).stream()
            .filter(p -> p.getPermissionType() == 1 && Boolean.TRUE.equals(p.getVisible()))
            .collect(Collectors.toList());
    return buildPermissionTree(menuPermissions, null);
  }

  private PermissionDTO convertToDTO(Permission permission) {
    return PermissionDTO.builder()
        .id(permission.getId())
        .parentId(permission.getParentId())
        .permissionName(permission.getPermissionName())
        .permissionCode(permission.getPermissionCode())
        .permissionType(permission.getPermissionType())
        .resourcePath(permission.getResourcePath())
        .httpMethod(permission.getHttpMethod())
        .icon(permission.getIcon())
        .componentPath(permission.getComponentPath())
        .sortOrder(permission.getSortOrder())
        .visible(permission.getVisible())
        .status(permission.getStatus())
        .remark(permission.getRemark())
        .createdTime(permission.getCreatedTime())
        .updatedTime(permission.getUpdatedTime())
        .build();
  }

  private List<PermissionDTO> buildPermissionTree(
      List<Permission> allPermissions, String parentId) {
    List<PermissionDTO> tree = new ArrayList<>();
    for (Permission permission : allPermissions) {
      if ((parentId == null
              && (permission.getParentId() == null || permission.getParentId().equals("0")))
          || (parentId != null && parentId.equals(permission.getParentId()))) {
        PermissionDTO dto = convertToDTO(permission);
        List<PermissionDTO> children = buildPermissionTree(allPermissions, permission.getId());
        if (!children.isEmpty()) {
          dto.setChildren(children);
        }
        tree.add(dto);
      }
    }
    return tree;
  }
}
