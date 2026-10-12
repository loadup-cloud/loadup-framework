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

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.upms.app.converter.UpmsDTOConverter;
import io.github.loadup.modules.upms.client.command.DepartmentCreateCommand;
import io.github.loadup.modules.upms.client.command.DepartmentUpdateCommand;
import io.github.loadup.modules.upms.client.dto.DepartmentDTO;
import io.github.loadup.modules.upms.client.error.UpmsResultCodeEnum;
import io.github.loadup.modules.upms.domain.gateway.DepartmentGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.model.Department;
import io.github.loadup.modules.upms.domain.model.User;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Department Management Service
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Service
public class DepartmentService implements io.github.loadup.modules.upms.client.facade.DepartmentFacade {
    private final UpmsDTOConverter dtoConverter;

    private final DepartmentGateway departmentGateway;
    private final UserGateway userGateway;

    /**
     * Create department
     */
    @Transactional
    public DepartmentDTO createDepartment(DepartmentCreateCommand command) {
        // Validate department code uniqueness
        if (departmentGateway.existsByDeptCode(command.getDeptCode())) {
            throw new CommonException(UpmsResultCodeEnum.DEPARTMENT_CODE_ALREADY_EXISTS);
        }

        // Validate parent department exists
        Integer deptLevel = 1;
        if (command.getParentId() != null && !"0".equals(command.getParentId())) {
            Department parentDept = departmentGateway
                    .findById(command.getParentId())
                    .orElseThrow(() -> new CommonException(UpmsResultCodeEnum.PARENT_DEPARTMENT_NOT_FOUND));
            deptLevel = (parentDept.getDeptLevel() != null ? parentDept.getDeptLevel() : 0) + 1;
        }

        // Validate leader user exists
        if (command.getLeaderUserId() != null) {
            userGateway
                    .findById(command.getLeaderUserId())
                    .orElseThrow(() -> new CommonException(UpmsResultCodeEnum.DEPARTMENT_LEADER_NOT_FOUND));
        }
        Department department = new Department();
        // Create department entity
        department.setParentId(command.getParentId());
        department.setDeptName(command.getDeptName());
        department.setDeptCode(command.getDeptCode());
        department.setDeptLevel(deptLevel);
        department.setSortOrder(command.getSortOrder());
        department.setLeaderUserId(command.getLeaderUserId());
        department.setMobile(command.getMobile());
        department.setEmail(command.getEmail());
        department.setStatus(command.getStatus() != null ? command.getStatus() : (short) 1);
        department.setDeleted(false);
        department.setRemark(command.getRemark());
        department.setCreatedBy(command.getCreatedBy());

        department = departmentGateway.save(department);

        return convertToDTO(department);
    }

    /**
     * Update department
     */
    @Transactional
    public DepartmentDTO updateDepartment(DepartmentUpdateCommand command) {
        Department department = departmentGateway
                .findById(command.getId())
                .orElseThrow(() -> new CommonException(UpmsResultCodeEnum.DEPARTMENT_NOT_FOUND));

        // Validate parent department (prevent circular reference)
        if (command.getParentId() != null && !"0".equals(command.getParentId())) {
            if (command.getParentId().equals(command.getId())) {
                throw new CommonException(UpmsResultCodeEnum.DEPARTMENT_CANNOT_BE_ITS_OWN_PARENT);
            }
            Department parentDept = departmentGateway
                    .findById(command.getParentId())
                    .orElseThrow(() -> new CommonException(UpmsResultCodeEnum.PARENT_DEPARTMENT_NOT_FOUND));

            department.setDeptLevel((parentDept.getDeptLevel() != null ? parentDept.getDeptLevel() : 0) + 1);
        }

        // Validate leader user exists
        if (command.getLeaderUserId() != null) {
            userGateway
                    .findById(command.getLeaderUserId())
                    .orElseThrow(() -> new CommonException(UpmsResultCodeEnum.DEPARTMENT_LEADER_NOT_FOUND));
        }

        // Update department fields
        if (command.getParentId() != null) {
            department.setParentId(command.getParentId());
        }
        if (command.getDeptName() != null) {
            department.setDeptName(command.getDeptName());
        }
        if (command.getSortOrder() != null) {
            department.setSortOrder(command.getSortOrder());
        }
        if (command.getLeaderUserId() != null) {
            department.setLeaderUserId(command.getLeaderUserId());
        }
        if (command.getMobile() != null) {
            department.setMobile(command.getMobile().isBlank() ? null : command.getMobile());
        }
        if (command.getEmail() != null) {
            department.setEmail(command.getEmail().isBlank() ? null : command.getEmail());
        }
        if (command.getStatus() != null) {
            department.setStatus(command.getStatus());
        }
        if (command.getRemark() != null) {
            department.setRemark(command.getRemark());
        }

        department.setUpdatedBy(command.getUpdatedBy());

        department = departmentGateway.update(department);

        return convertToDTO(department);
    }

    /**
     * Delete department
     */
    @Transactional
    public void deleteDepartment(String id) {
        departmentGateway.findById(id).orElseThrow(() -> new CommonException(UpmsResultCodeEnum.DEPARTMENT_NOT_FOUND));

        // Check if department has children
        if (departmentGateway.hasChildren(id)) {
            throw new CommonException(UpmsResultCodeEnum.DEPARTMENT_CONTAINS_CHILD_DEPARTMENTS);
        }

        // Check if department has users
        if (departmentGateway.hasUsers(id)) {
            throw new CommonException(UpmsResultCodeEnum.DEPARTMENT_CONTAINS_USERS);
        }

        departmentGateway.deleteById(id);
    }

    /**
     * Get department by ID
     */
    public DepartmentDTO getDepartmentById(String id) {
        Department department = departmentGateway
                .findById(id)
                .orElseThrow(() -> new CommonException(UpmsResultCodeEnum.DEPARTMENT_NOT_FOUND));
        return convertToDTO(department);
    }

    /**
     * Get all departments as tree
     */
    public List<DepartmentDTO> getDepartmentTree() {
        List<Department> allDepartments = departmentGateway.findAll();
        return buildDepartmentTree(allDepartments, null);
    }

    /**
     * Get department tree (from specific department)
     */
    public DepartmentDTO getDepartmentTreeById(String id) {
        Department department = departmentGateway
                .findById(id)
                .orElseThrow(() -> new CommonException(UpmsResultCodeEnum.DEPARTMENT_NOT_FOUND));
        List<Department> allDepartments = departmentGateway.findAll();

        DepartmentDTO dto = convertToDTO(department);
        List<DepartmentDTO> children = buildDepartmentTree(allDepartments, id);
        if (!children.isEmpty()) {
            dto.setChildren(children);
        }
        return dto;
    }

    /**
     * Move department to another parent
     */
    @Transactional
    public void moveDepartment(String deptId, String newParentId) {
        Department department = departmentGateway
                .findById(deptId)
                .orElseThrow(() -> new CommonException(UpmsResultCodeEnum.DEPARTMENT_NOT_FOUND));

        // Validate new parent exists
        if (newParentId != null && !"0".equals(newParentId)) {
            if (newParentId.equals(deptId)) {
                throw new CommonException(UpmsResultCodeEnum.DEPARTMENT_CANNOT_BE_ITS_OWN_PARENT);
            }
            Department newParent = departmentGateway
                    .findById(newParentId)
                    .orElseThrow(() -> new CommonException(UpmsResultCodeEnum.NEW_PARENT_DEPARTMENT_NOT_FOUND));

            // Check if new parent is a child of current department (prevent circular)
            if (isDescendant(deptId, newParentId)) {
                throw new CommonException(UpmsResultCodeEnum.DEPARTMENT_CYCLE);
            }

            department.setParentId(newParentId);
            department.setDeptLevel((newParent.getDeptLevel() != null ? newParent.getDeptLevel() : 0) + 1);
        } else {
            department.setParentId(null);
            department.setDeptLevel(1);
        }

        departmentGateway.update(department);
    }

    /**
     * Check if targetId is a descendant of ancestorId
     */
    private boolean isDescendant(String ancestorId, String targetId) {
        Department target = departmentGateway.findById(targetId).orElse(null);
        if (target == null) {
            return false;
        }
        if (target.getParentId() == null) {
            return false;
        }
        if (target.getParentId().equals(ancestorId)) {
            return true;
        }
        return isDescendant(ancestorId, target.getParentId());
    }

    /**
     * Convert Department entity to DepartmentDTO
     */
    private DepartmentDTO convertToDTO(Department department) {
        String leaderName = department.getLeaderUserId() == null
                ? null
                : userGateway
                        .findById(department.getLeaderUserId())
                        .map(User::getUsername)
                        .orElse(null);
        return dtoConverter.toDepartment(department, leaderName);
    }

    /**
     * Build department tree recursively
     */
    private List<DepartmentDTO> buildDepartmentTree(List<Department> allDepartments, String parentId) {
        List<DepartmentDTO> tree = new ArrayList<>();
        for (Department department : allDepartments) {
            if (parentId == null
                            && (department.getParentId() == null
                                    || department.getParentId().equals("0"))
                    || parentId != null && parentId.equals(department.getParentId())) {
                DepartmentDTO dto = convertToDTO(department);
                List<DepartmentDTO> children = buildDepartmentTree(allDepartments, department.getId());
                if (!children.isEmpty()) {
                    dto.setChildren(children);
                }
                tree.add(dto);
            }
        }
        return tree;
    }

    public DepartmentService(
            DepartmentGateway departmentGateway, UserGateway userGateway, UpmsDTOConverter dtoConverter) {
        this.dtoConverter = dtoConverter;
        this.departmentGateway = departmentGateway;
        this.userGateway = userGateway;
    }
}
