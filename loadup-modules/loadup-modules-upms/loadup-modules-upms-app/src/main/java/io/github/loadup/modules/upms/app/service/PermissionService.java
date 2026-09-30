package io.github.loadup.modules.upms.app.service;

import io.github.loadup.modules.upms.client.command.PermissionCreateCommand;
import io.github.loadup.modules.upms.client.command.PermissionUpdateCommand;
import io.github.loadup.modules.upms.client.dto.PermissionDTO;
import io.github.loadup.modules.upms.domain.entity.Permission;
import io.github.loadup.modules.upms.domain.gateway.PermissionGateway;
import io.github.loadup.modules.upms.domain.service.UserPermissionService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Permission Management Service
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Service
public class PermissionService {
    private static final Logger log = LoggerFactory.getLogger(PermissionService.class);

    private final PermissionGateway permissionGateway;
    private final UserPermissionService userPermissionService;

    @Transactional
    public PermissionDTO createPermission(PermissionCreateCommand command) {
        if (permissionGateway.existsByPermissionCode(command.getPermissionCode())) {
            throw new RuntimeException("权限编码已存在");
        }

        validateParent(null, command.getParentId());

        Permission permission = new Permission();
        permission.setParentId(command.getParentId());
        permission.setPermissionName(command.getPermissionName());
        permission.setPermissionCode(command.getPermissionCode());
        permission.setPermissionType(command.getPermissionType());
        permission.setResourcePath(command.getResourcePath());
        permission.setHttpMethod(command.getHttpMethod());
        permission.setIcon(command.getIcon());
        permission.setComponentPath(command.getComponentPath());
        permission.setSortOrder(command.getSortOrder());
        permission.setVisible(command.isVisible() != null ? command.isVisible() : true);
        permission.setStatus(command.getStatus() != null ? command.getStatus() : (short) 1);
        permission.setDeleted(false);
        permission.setRemark(command.getRemark());
        permission.setCreatedBy(command.getCreatedBy());
        permission.setCreatedAt(LocalDateTime.now());

        permission = permissionGateway.save(permission);
        return convertToDTO(permission);
    }

    @Transactional
    public PermissionDTO updatePermission(PermissionUpdateCommand command) {
        Permission permission =
                permissionGateway.findById(command.getId()).orElseThrow(() -> new RuntimeException("权限不存在"));

        if (command.getParentId() != null) validateParent(command.getId(), command.getParentId());

        if (command.getParentId() != null) {
            permission.setParentId(command.getParentId());
        }
        if (command.getPermissionName() != null) {
            permission.setPermissionName(command.getPermissionName());
        }
        if (command.getPermissionType() != null) {
            permission.setPermissionType(command.getPermissionType());
        }
        if (command.getResourcePath() != null) {
            permission.setResourcePath(
                    command.getResourcePath().isBlank() ? null : command.getResourcePath());
        }
        if (command.getHttpMethod() != null) {
            permission.setHttpMethod(command.getHttpMethod().isBlank() ? null : command.getHttpMethod());
        }
        if (command.getIcon() != null) {
            permission.setIcon(command.getIcon().isBlank() ? null : command.getIcon());
        }
        if (command.getComponentPath() != null) {
            permission.setComponentPath(
                    command.getComponentPath().isBlank() ? null : command.getComponentPath());
        }
        if (command.getSortOrder() != null) {
            permission.setSortOrder(command.getSortOrder());
        }
        if (command.isVisible() != null) {
            permission.setVisible(command.isVisible());
        }
        if (command.getStatus() != null) {
            permission.setStatus(command.getStatus());
        }
        if (command.getRemark() != null) {
            permission.setRemark(command.getRemark());
        }

        permission.setUpdatedBy(command.getUpdatedBy());
        permission.setUpdatedAt(LocalDateTime.now());

        permission = permissionGateway.update(permission);
        return convertToDTO(permission);
    }

    @Transactional
    public void deletePermission(String id) {
        permissionGateway.findById(id).orElseThrow(() -> new RuntimeException("权限不存在"));

        List<Permission> children = permissionGateway.findByParentId(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("该权限下存在子权限，无法删除");
        }

        permissionGateway.deleteById(id);
    }

    public PermissionDTO getPermissionById(String id) {
        Permission permission = permissionGateway.findById(id).orElseThrow(() -> new RuntimeException("权限不存在"));
        return convertToDTO(permission);
    }

    public List<PermissionDTO> getPermissionTree() {
        List<Permission> allPermissions = permissionGateway.findAll();
        return buildPermissionTree(allPermissions, null);
    }

    public List<PermissionDTO> getPermissionsByType(Short permissionType) {
        List<Permission> permissions = permissionGateway.findByPermissionType(permissionType);
        return permissions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<PermissionDTO> getUserPermissions(String userId) {
        List<Permission> permissions = userPermissionService.getUserPermissions(userId);
        return permissions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<PermissionDTO> getUserMenuTree(String userId) {
        List<Permission> menuPermissions = userPermissionService.getUserPermissions(userId).stream()
                .filter(p -> p.getPermissionType() == 1 && Boolean.TRUE.equals(p.isVisible()))
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
                .visible(permission.isVisible())
                .status(permission.getStatus())
                .remark(permission.getRemark())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }

    private List<PermissionDTO> buildPermissionTree(List<Permission> allPermissions, String parentId) {
        List<PermissionDTO> tree = new ArrayList<>();
        for (Permission permission : allPermissions) {
            if (parentId == null
                            && (permission.getParentId() == null
                                    || permission.getParentId().equals("0"))
                    || parentId != null && parentId.equals(permission.getParentId())) {
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

    public PermissionService(PermissionGateway permissionGateway, UserPermissionService userPermissionService) {
        this.permissionGateway = permissionGateway;
        this.userPermissionService = userPermissionService;
    }

    private void validateParent(String permissionId, String parentId) {
        Set<String> visited = new HashSet<>();
        String current = parentId;
        while (current != null && !"0".equals(current)) {
            if (current.equals(permissionId) || !visited.add(current)) {
                throw new IllegalArgumentException("Permission hierarchy contains a cycle");
            }
            String parentPermissionId = current;
            Permission parent = permissionGateway
                    .findById(parentPermissionId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Parent permission does not exist: " + parentPermissionId));
            current = parent.getParentId();
        }
    }
}
