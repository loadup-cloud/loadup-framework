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

import io.github.loadup.modules.upms.client.command.PermissionCreateCommand;
import io.github.loadup.modules.upms.client.command.PermissionUpdateCommand;
import io.github.loadup.modules.upms.client.dto.PermissionDTO;
import io.github.loadup.modules.upms.domain.entity.Permission;
import io.github.loadup.modules.upms.domain.gateway.PermissionGateway;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Transactional
    public PermissionDTO createPermission(PermissionCreateCommand command) {
        if (permissionGateway.existsByPermissionCode(command.getPermissionCode())) {
            throw new RuntimeException("权限编码已存在");
        }

        if (command.getParentId() != null && !"0".equals(command.getParentId())) {
            permissionGateway.findById(command.getParentId()).orElseThrow(() -> new RuntimeException("父权限不存在"));
        }

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
        permission.setCreatedTime(LocalDateTime.now());

        permission = permissionGateway.save(permission);
        return convertToDTO(permission);
    }

    @Transactional
    public PermissionDTO updatePermission(PermissionUpdateCommand command) {
        Permission permission =
                permissionGateway.findById(command.getId()).orElseThrow(() -> new RuntimeException("权限不存在"));

        if (command.getParentId() != null && !"0".equals(command.getParentId())) {
            if (command.getParentId().equals(command.getId())) {
                throw new RuntimeException("父权限不能是自己");
            }
            permissionGateway.findById(command.getParentId()).orElseThrow(() -> new RuntimeException("父权限不存在"));
        }

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
            permission.setResourcePath(command.getResourcePath());
        }
        if (command.getHttpMethod() != null) {
            permission.setHttpMethod(command.getHttpMethod());
        }
        if (command.getIcon() != null) {
            permission.setIcon(command.getIcon());
        }
        if (command.getComponentPath() != null) {
            permission.setComponentPath(command.getComponentPath());
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
        permission.setUpdatedTime(LocalDateTime.now());

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
        List<Permission> permissions = permissionGateway.findByUserId(userId);
        return permissions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<PermissionDTO> getUserMenuTree(String userId) {
        List<Permission> menuPermissions = permissionGateway.findByUserId(userId).stream()
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
                .createdTime(permission.getCreatedTime())
                .updatedTime(permission.getUpdatedTime())
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

    public PermissionService(PermissionGateway permissionGateway) {
        this.permissionGateway = permissionGateway;
    }
}
