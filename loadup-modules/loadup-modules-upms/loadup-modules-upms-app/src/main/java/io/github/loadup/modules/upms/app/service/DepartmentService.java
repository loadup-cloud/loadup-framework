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

import io.github.loadup.modules.upms.client.command.DepartmentCreateCommand;
import io.github.loadup.modules.upms.client.command.DepartmentUpdateCommand;
import io.github.loadup.modules.upms.client.dto.DepartmentDTO;
import io.github.loadup.modules.upms.domain.entity.Department;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.DepartmentGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Department Management Service
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Service
public class DepartmentService {
    private static final Logger log = LoggerFactory.getLogger(DepartmentService.class);

    private final DepartmentGateway departmentGateway;
    private final UserGateway userGateway;

    /**
     * Create department
     */
    @Transactional
    public DepartmentDTO createDepartment(DepartmentCreateCommand command) {
        // Validate department code uniqueness
        if (departmentGateway.existsByDeptCode(command.getDeptCode())) {
            throw new RuntimeException("部门编码已存在");
        }

        // Validate parent department exists
        Integer deptLevel = 1;
        if (command.getParentId() != null && !"0".equals(command.getParentId())) {
            Department parentDept =
                    departmentGateway.findById(command.getParentId()).orElseThrow(() -> new RuntimeException("父部门不存在"));
            deptLevel = (parentDept.getDeptLevel() != null ? parentDept.getDeptLevel() : 0) + 1;
        }

        // Validate leader user exists
        if (command.getLeaderUserId() != null) {
            userGateway.findById(command.getLeaderUserId()).orElseThrow(() -> new RuntimeException("部门负责人不存在"));
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
        department.setCreatedTime(LocalDateTime.now());

        department = departmentGateway.save(department);

        return convertToDTO(department);
    }

    /**
     * Update department
     */
    @Transactional
    public DepartmentDTO updateDepartment(DepartmentUpdateCommand command) {
        Department department =
                departmentGateway.findById(command.getId()).orElseThrow(() -> new RuntimeException("部门不存在"));

        // Validate parent department (prevent circular reference)
        if (command.getParentId() != null && !"0".equals(command.getParentId())) {
            if (command.getParentId().equals(command.getId())) {
                throw new RuntimeException("父部门不能是自己");
            }
            Department parentDept =
                    departmentGateway.findById(command.getParentId()).orElseThrow(() -> new RuntimeException("父部门不存在"));

            department.setDeptLevel((parentDept.getDeptLevel() != null ? parentDept.getDeptLevel() : 0) + 1);
        }

        // Validate leader user exists
        if (command.getLeaderUserId() != null) {
            userGateway.findById(command.getLeaderUserId()).orElseThrow(() -> new RuntimeException("部门负责人不存在"));
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
            department.setMobile(command.getMobile());
        }
        if (command.getEmail() != null) {
            department.setEmail(command.getEmail());
        }
        if (command.getStatus() != null) {
            department.setStatus(command.getStatus());
        }
        if (command.getRemark() != null) {
            department.setRemark(command.getRemark());
        }

        department.setUpdatedBy(command.getUpdatedBy());
        department.setUpdatedTime(LocalDateTime.now());

        department = departmentGateway.update(department);

        return convertToDTO(department);
    }

    /**
     * Delete department
     */
    @Transactional
    public void deleteDepartment(String id) {
        departmentGateway.findById(id).orElseThrow(() -> new RuntimeException("部门不存在"));

        // Check if department has children
        if (departmentGateway.hasChildren(id)) {
            throw new RuntimeException("该部门下存在子部门，无法删除");
        }

        // Check if department has users
        if (departmentGateway.hasUsers(id)) {
            throw new RuntimeException("该部门下存在用户，无法删除");
        }

        departmentGateway.deleteById(id);
    }

    /**
     * Get department by ID
     */
    public DepartmentDTO getDepartmentById(String id) {
        Department department = departmentGateway.findById(id).orElseThrow(() -> new RuntimeException("部门不存在"));
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
        Department department = departmentGateway.findById(id).orElseThrow(() -> new RuntimeException("部门不存在"));
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
        Department department = departmentGateway.findById(deptId).orElseThrow(() -> new RuntimeException("部门不存在"));

        // Validate new parent exists
        if (newParentId != null && !"0".equals(newParentId)) {
            if (newParentId.equals(deptId)) {
                throw new RuntimeException("父部门不能是自己");
            }
            Department newParent =
                    departmentGateway.findById(newParentId).orElseThrow(() -> new RuntimeException("新父部门不存在"));

            // Check if new parent is a child of current department (prevent circular)
            if (isDescendant(deptId, newParentId)) {
                throw new RuntimeException("不能将部门移动到其子部门下");
            }

            department.setParentId(newParentId);
            department.setDeptLevel((newParent.getDeptLevel() != null ? newParent.getDeptLevel() : 0) + 1);
        } else {
            department.setParentId(null);
            department.setDeptLevel(1);
        }

        department.setUpdatedTime(LocalDateTime.now());
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
        User leader = null;
        if (department.getLeaderUserId() != null) {
            leader = userGateway.findById(department.getLeaderUserId()).orElse(null);
        }

        return DepartmentDTO.builder()
                .id(department.getId())
                .parentId(department.getParentId())
                .deptName(department.getDeptName())
                .deptCode(department.getDeptCode())
                .deptLevel(department.getDeptLevel())
                .sortOrder(department.getSortOrder())
                .leaderUserId(department.getLeaderUserId())
                .leaderUserName(leader != null ? leader.getUsername() : null)
                .mobile(department.getMobile())
                .email(department.getEmail())
                .status(department.getStatus())
                .remark(department.getRemark())
                .createdTime(department.getCreatedTime())
                .updatedTime(department.getUpdatedTime())
                .build();
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

    public DepartmentService(DepartmentGateway departmentGateway, UserGateway userGateway) {
        this.departmentGateway = departmentGateway;
        this.userGateway = userGateway;
    }
}
