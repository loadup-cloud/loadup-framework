package com.github.loadup.modules.upms.repository;

import static org.assertj.core.api.Assertions.*;

import com.github.loadup.modules.upms.domain.entity.Department;
import com.github.loadup.modules.upms.domain.gateway.DepartmentGateway;
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
class DepartmentGatewayTest extends BaseRepositoryTest {

  @Autowired private DepartmentGateway departmentGateway;

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
            .mobile("12345678901")
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
    Department saved = departmentGateway.save(testDepartment);

    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getDeptName()).isEqualTo("Test Department");
  }

  @Test
  @DisplayName("Should find department by code")
  void testFindByDeptCode() {
    // Given
    Department saved = departmentGateway.save(testDepartment);

    // When
    var found = departmentGateway.findByDeptCode("TEST_DEPT_NEW");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
  }

  @Test
  @DisplayName("Should check if department code exists")
  void testExistsByDeptCode() {
    // Given
    departmentGateway.save(testDepartment);

    // When
    boolean exists = departmentGateway.existsByDeptCode("TEST_DEPT_NEW");
    boolean notExists = departmentGateway.existsByDeptCode("NONEXISTENT");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should find root departments")
  void testFindRootDepartments() {
    // Given
    departmentGateway.save(testDepartment);

    // When
    List<Department> roots = departmentGateway.findRootDepartments();

    // Then
    assertThat(roots).isNotEmpty();
    assertThat(roots)
        .allMatch(dept -> dept.getParentId() == null || dept.getParentId().equals("0"));
  }

  @Test
  @DisplayName("Should find children departments")
  void testFindByParentId() {
    // Given
    Department parent = departmentGateway.save(testDepartment);
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
    departmentGateway.save(child);

    // When
    List<Department> children = departmentGateway.findByParentId(parent.getId());

    // Then
    assertThat(children).hasSize(1);
    assertThat(children.get(0).getDeptCode()).isEqualTo("CHILD_DEPT");
  }

  @Test
  @DisplayName("Should check if department has children")
  void testHasChildren() {
    // Given
    Department parent = departmentGateway.save(testDepartment);
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
    child = departmentGateway.save(child);

    // When
    boolean hasChildren = departmentGateway.hasChildren(parent.getId());
    boolean noChildren = departmentGateway.hasChildren(child.getId());

    // Then
    assertThat(hasChildren).isTrue();
    assertThat(noChildren).isFalse();
  }

  @Test
  @DisplayName("Should build department tree")
  void testBuildTree() {
    // Given
    Department root1 = departmentGateway.save(testDepartment);
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
    departmentGateway.save(root2);

    // When
    List<Department> tree = departmentGateway.buildTree();

    // Then
    assertThat(tree).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  @DisplayName("Should update department")
  void testUpdateDepartment() {
    // Given
    Department saved = departmentGateway.save(testDepartment);
    saved.setDeptName("Updated Department");
    saved.setMobile("19900199001");

    // When
    Department updated = departmentGateway.update(saved);

    // Then
    assertThat(updated.getDeptName()).isEqualTo("Updated Department");
    assertThat(updated.getMobile()).isEqualTo("19900199001");
  }

  @Test
  @DisplayName("Should soft delete department")
  void testDeleteDepartment() {
    // Given
    Department saved = departmentGateway.save(testDepartment);

    // When
    departmentGateway.deleteById(saved.getId());

    // Then
    var found = departmentGateway.findByDeptCode("TEST_DEPT_NEW");
    assertThat(found).isEmpty();
  }
}
