package io.github.loadup.modules.upms.app.service;

/*-
 * #%L
 * Loadup Modules UPMS App Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.modules.upms.domain.entity.Department;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.DepartmentGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.client.command.DepartmentCreateCommand;
import io.github.loadup.modules.upms.client.command.DepartmentUpdateCommand;
import io.github.loadup.modules.upms.client.dto.DepartmentDTO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Department Management Service
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentGateway departmentGateway;
    private final UserGateway userGateway;

    /** Create department */
    @Transactional
    public DepartmentDTO createDepartment(DepartmentCreateCommand command) {
        // Validate department code uniqueness
        if (departmentGateway.existsByDeptCode(command.getDeptCode())) {
            throw new RuntimeException("部门编码已存在");
        }

        // Validate parent department exists
        Integer deptLevel = 1;
        if (command.getParentId() != null && command.getParentId() != "0") {
            Department parentDept =
                    departmentGateway.findById(command.getParentId()).orElseThrow(() -> new RuntimeException("父部门不存在"));
            deptLevel = (parentDept.getDeptLevel() != null ? parentDept.getDeptLevel() : 0) + 1;
        }

        // Validate leader user exists
        if (command.getLeaderUserId() != null) {
            userGateway.findById(command.getLeaderUserId()).orElseThrow(() -> new RuntimeException("部门负责人不存在"));
        }

        // Create department entity
        Department department = Department.builder()
                .parentId(command.getParentId())
                .deptName(command.getDeptName())
                .deptCode(command.getDeptCode())
                .deptLevel(deptLevel)
                .sortOrder(command.getSortOrder())
                .leaderUserId(command.getLeaderUserId())
                .mobile(command.getMobile())
                .email(command.getEmail())
                .status(command.getStatus() != null ? command.getStatus() : (short) 1)
                .deleted(false)
                .remark(command.getRemark())
                .createdBy(command.getCreatedBy())
                .createdTime(LocalDateTime.now())
                .build();

        department = departmentGateway.save(department);

        return convertToDTO(department);
    }

    /** Update department */
    @Transactional
    public DepartmentDTO updateDepartment(DepartmentUpdateCommand command) {
        Department department =
                departmentGateway.findById(command.getId()).orElseThrow(() -> new RuntimeException("部门不存在"));

        // Validate parent department (prevent circular reference)
        if (command.getParentId() != null && command.getParentId() != "0") {
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

    /** Delete department */
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

    /** Get department by ID */
    public DepartmentDTO getDepartmentById(String id) {
        Department department = departmentGateway.findById(id).orElseThrow(() -> new RuntimeException("部门不存在"));
        return convertToDTO(department);
    }

    /** Get all departments as tree */
    public List<DepartmentDTO> getDepartmentTree() {
        List<Department> allDepartments = departmentGateway.findAll();
        return buildDepartmentTree(allDepartments, null);
    }

    /** Get department tree (from specific department) */
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

    /** Move department to another parent */
    @Transactional
    public void moveDepartment(String deptId, String newParentId) {
        Department department = departmentGateway.findById(deptId).orElseThrow(() -> new RuntimeException("部门不存在"));

        // Validate new parent exists
        if (newParentId != null && newParentId != "0") {
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

    /** Check if targetId is a descendant of ancestorId */
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

    /** Convert Department entity to DepartmentDTO */
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

    /** Build department tree recursively */
    private List<DepartmentDTO> buildDepartmentTree(List<Department> allDepartments, String parentId) {
        List<DepartmentDTO> tree = new ArrayList<>();
        for (Department department : allDepartments) {
            if ((parentId == null
                            && (department.getParentId() == null
                                    || department.getParentId().equals("0")))
                    || (parentId != null && parentId.equals(department.getParentId()))) {
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
}
