package com.github.loadup.modules.upms.repository;

import static org.assertj.core.api.Assertions.*;

import com.github.loadup.modules.upms.domain.entity.Department;
import com.github.loadup.modules.upms.domain.repository.DepartmentRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Department Repository Tests
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@DisplayName("DepartmentRepository Tests")
class DepartmentRepositoryTest extends BaseRepositoryTest {

  @Autowired private DepartmentRepository departmentRepository;

  private Department testDepartment;

  @BeforeEach
  void setUp() {
    testDepartment =
        Department.builder()
            .parentId("0")
            .deptName("Test Department")
            .deptCode("TEST_DEPT_NEW")
            .deptLevel(1)
            .sortOrder(0)
            .phone("12345678901")
            .email("dept@example.com")
            .status((short) 1)
            .deleted(false)
            .createdBy("1")
            .createdTime(LocalDateTime.now())
            .build();
  }

  @Test
  @DisplayName("Should save department successfully")
  void testSaveDepartment() {
    // When
    Department saved = departmentRepository.save(testDepartment);

    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getDeptName()).isEqualTo("Test Department");
  }

  @Test
  @DisplayName("Should find department by code")
  void testFindByDeptCode() {
    // Given
    Department saved = departmentRepository.save(testDepartment);

    // When
    var found = departmentRepository.findByDeptCode("TEST_DEPT_NEW");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
  }

  @Test
  @DisplayName("Should check if department code exists")
  void testExistsByDeptCode() {
    // Given
    departmentRepository.save(testDepartment);

    // When
    boolean exists = departmentRepository.existsByDeptCode("TEST_DEPT_NEW");
    boolean notExists = departmentRepository.existsByDeptCode("NONEXISTENT");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should find root departments")
  void testFindRootDepartments() {
    // Given
    departmentRepository.save(testDepartment);

    // When
    List<Department> roots = departmentRepository.findRootDepartments();

    // Then
    assertThat(roots).isNotEmpty();
    assertThat(roots)
        .allMatch(dept -> dept.getParentId() == null || dept.getParentId().equals("0"));
  }

  @Test
  @DisplayName("Should find children departments")
  void testFindByParentId() {
    // Given
    Department parent = departmentRepository.save(testDepartment);
    Department child =
        Department.builder()
            .parentId(parent.getId())
            .deptName("Child Department")
            .deptCode("CHILD_DEPT")
            .deptLevel(2)
            .sortOrder(0)
            .status((short) 1)
            .deleted(false)
            .createdBy("1")
            .createdTime(LocalDateTime.now())
            .build();
    departmentRepository.save(child);

    // When
    List<Department> children = departmentRepository.findByParentId(parent.getId());

    // Then
    assertThat(children).hasSize(1);
    assertThat(children.get(0).getDeptCode()).isEqualTo("CHILD_DEPT");
  }

  @Test
  @DisplayName("Should check if department has children")
  void testHasChildren() {
    // Given
    Department parent = departmentRepository.save(testDepartment);
    Department child =
        Department.builder()
            .parentId(parent.getId())
            .deptName("Child")
            .deptCode("CHILD")
            .deptLevel(2)
            .sortOrder(0)
            .status((short) 1)
            .deleted(false)
            .createdBy("1")
            .createdTime(LocalDateTime.now())
            .build();
    child = departmentRepository.save(child);

    // When
    boolean hasChildren = departmentRepository.hasChildren(parent.getId());
    boolean noChildren = departmentRepository.hasChildren(child.getId());

    // Then
    assertThat(hasChildren).isTrue();
    assertThat(noChildren).isFalse();
  }

  @Test
  @DisplayName("Should build department tree")
  void testBuildTree() {
    // Given
    Department root1 = departmentRepository.save(testDepartment);
    Department root2 =
        Department.builder()
            .parentId("0")
            .deptName("Root 2")
            .deptCode("ROOT_2")
            .deptLevel(1)
            .sortOrder(1)
            .status((short) 1)
            .deleted(false)
            .createdBy("1")
            .createdTime(LocalDateTime.now())
            .build();
    departmentRepository.save(root2);

    // When
    List<Department> tree = departmentRepository.buildTree();

    // Then
    assertThat(tree).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  @DisplayName("Should update department")
  void testUpdateDepartment() {
    // Given
    Department saved = departmentRepository.save(testDepartment);
    saved.setDeptName("Updated Department");
    saved.setPhone("19900199001");

    // When
    Department updated = departmentRepository.update(saved);

    // Then
    assertThat(updated.getDeptName()).isEqualTo("Updated Department");
    assertThat(updated.getPhone()).isEqualTo("19900199001");
  }

  @Test
  @DisplayName("Should soft delete department")
  void testDeleteDepartment() {
    // Given
    Department saved = departmentRepository.save(testDepartment);

    // When
    departmentRepository.deleteById(saved.getId());

    // Then
    var found = departmentRepository.findByDeptCode("TEST_DEPT_NEW");
    assertThat(found).isEmpty();
  }
}
