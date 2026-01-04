package com.github.loadup.modules.upms.app.service;

import com.github.loadup.modules.upms.app.command.DepartmentCreateCommand;
import com.github.loadup.modules.upms.app.command.DepartmentUpdateCommand;
import com.github.loadup.modules.upms.app.dto.DepartmentDTO;
import com.github.loadup.modules.upms.domain.entity.Department;
import com.github.loadup.modules.upms.domain.entity.User;
import com.github.loadup.modules.upms.domain.repository.DepartmentRepository;
import com.github.loadup.modules.upms.domain.repository.UserRepository;
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

  private final DepartmentRepository departmentRepository;
  private final UserRepository userRepository;

  /** Create department */
  @Transactional
  public DepartmentDTO createDepartment(DepartmentCreateCommand command) {
    // Validate department code uniqueness
    if (departmentRepository.existsByDeptCode(command.getDeptCode())) {
      throw new RuntimeException("部门编码已存在");
    }

    // Validate parent department exists
    Integer deptLevel = 1;
    if (command.getParentId() != null && command.getParentId() > 0) {
      Department parentDept =
          departmentRepository
              .findById(command.getParentId())
              .orElseThrow(() -> new RuntimeException("父部门不存在"));
      deptLevel = (parentDept.getDeptLevel() != null ? parentDept.getDeptLevel() : 0) + 1;
    }

    // Validate leader user exists
    if (command.getLeaderUserId() != null) {
      userRepository
          .findById(command.getLeaderUserId())
          .orElseThrow(() -> new RuntimeException("部门负责人不存在"));
    }

    // Create department entity
    Department department =
        Department.builder()
            .parentId(command.getParentId())
            .deptName(command.getDeptName())
            .deptCode(command.getDeptCode())
            .deptLevel(deptLevel)
            .sortOrder(command.getSortOrder())
            .leaderUserId(command.getLeaderUserId())
            .phone(command.getPhone())
            .email(command.getEmail())
            .status(command.getStatus() != null ? command.getStatus() : (short) 1)
            .deleted(false)
            .remark(command.getRemark())
            .createdBy(command.getCreatedBy())
            .createdTime(LocalDateTime.now())
            .build();

    department = departmentRepository.save(department);

    return convertToDTO(department);
  }

  /** Update department */
  @Transactional
  public DepartmentDTO updateDepartment(DepartmentUpdateCommand command) {
    Department department =
        departmentRepository
            .findById(command.getId())
            .orElseThrow(() -> new RuntimeException("部门不存在"));

    // Validate parent department (prevent circular reference)
    if (command.getParentId() != null && command.getParentId() > 0) {
      if (command.getParentId().equals(command.getId())) {
        throw new RuntimeException("父部门不能是自己");
      }
      Department parentDept =
          departmentRepository
              .findById(command.getParentId())
              .orElseThrow(() -> new RuntimeException("父部门不存在"));

      department.setDeptLevel(
          (parentDept.getDeptLevel() != null ? parentDept.getDeptLevel() : 0) + 1);
    }

    // Validate leader user exists
    if (command.getLeaderUserId() != null) {
      userRepository
          .findById(command.getLeaderUserId())
          .orElseThrow(() -> new RuntimeException("部门负责人不存在"));
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
    if (command.getPhone() != null) {
      department.setPhone(command.getPhone());
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

    department = departmentRepository.update(department);

    return convertToDTO(department);
  }

  /** Delete department */
  @Transactional
  public void deleteDepartment(Long id) {
    departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("部门不存在"));

    // Check if department has children
    if (departmentRepository.hasChildren(id)) {
      throw new RuntimeException("该部门下存在子部门，无法删除");
    }

    // Check if department has users
    if (departmentRepository.hasUsers(id)) {
      throw new RuntimeException("该部门下存在用户，无法删除");
    }

    departmentRepository.deleteById(id);
  }

  /** Get department by ID */
  public DepartmentDTO getDepartmentById(Long id) {
    Department department =
        departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("部门不存在"));
    return convertToDTO(department);
  }

  /** Get all departments as tree */
  public List<DepartmentDTO> getDepartmentTree() {
    List<Department> allDepartments = departmentRepository.findAll();
    return buildDepartmentTree(allDepartments, null);
  }

  /** Get department tree (from specific department) */
  public DepartmentDTO getDepartmentTreeById(Long id) {
    Department department =
        departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("部门不存在"));
    List<Department> allDepartments = departmentRepository.findAll();

    DepartmentDTO dto = convertToDTO(department);
    List<DepartmentDTO> children = buildDepartmentTree(allDepartments, id);
    if (!children.isEmpty()) {
      dto.setChildren(children);
    }
    return dto;
  }

  /** Move department to another parent */
  @Transactional
  public void moveDepartment(Long deptId, Long newParentId) {
    Department department =
        departmentRepository.findById(deptId).orElseThrow(() -> new RuntimeException("部门不存在"));

    // Validate new parent exists
    if (newParentId != null && newParentId > 0) {
      if (newParentId.equals(deptId)) {
        throw new RuntimeException("父部门不能是自己");
      }
      Department newParent =
          departmentRepository
              .findById(newParentId)
              .orElseThrow(() -> new RuntimeException("新父部门不存在"));

      // Check if new parent is a child of current department (prevent circular)
      if (isDescendant(deptId, newParentId)) {
        throw new RuntimeException("不能将部门移动到其子部门下");
      }

      department.setParentId(newParentId);
      department.setDeptLevel(
          (newParent.getDeptLevel() != null ? newParent.getDeptLevel() : 0) + 1);
    } else {
      department.setParentId(null);
      department.setDeptLevel(1);
    }

    department.setUpdatedTime(LocalDateTime.now());
    departmentRepository.update(department);
  }

  /** Check if targetId is a descendant of ancestorId */
  private boolean isDescendant(Long ancestorId, Long targetId) {
    Department target = departmentRepository.findById(targetId).orElse(null);
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
      leader = userRepository.findById(department.getLeaderUserId()).orElse(null);
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
        .phone(department.getPhone())
        .email(department.getEmail())
        .status(department.getStatus())
        .remark(department.getRemark())
        .createdTime(department.getCreatedTime())
        .updatedTime(department.getUpdatedTime())
        .build();
  }

  /** Build department tree recursively */
  private List<DepartmentDTO> buildDepartmentTree(List<Department> allDepartments, Long parentId) {
    List<DepartmentDTO> tree = new ArrayList<>();
    for (Department department : allDepartments) {
      if ((parentId == null && (department.getParentId() == null || department.getParentId() == 0))
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
