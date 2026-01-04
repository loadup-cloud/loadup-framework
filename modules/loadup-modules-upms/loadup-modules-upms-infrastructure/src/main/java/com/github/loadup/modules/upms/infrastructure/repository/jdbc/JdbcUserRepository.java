package com.github.loadup.modules.upms.infrastructure.repository.jdbc;

import com.github.loadup.modules.upms.domain.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JDBC User Repository
 *
 * <p>使用Spring Data方法名规范，自动生成查询SQL
 *
 * <p>通过TenantAwareRepository自动添加tenant_id和deleted过滤条件
 *
 * <p>方法命名规范：
 *
 * <ul>
 *   <li>findBy{Property}: 查询单个或列表
 *   <li>countBy{Property}: 统计数量
 *   <li>existsBy{Property}: 判断存在
 *   <li>deleteBy{Property}: 删除
 * </ul>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface JdbcUserRepository
    extends CrudRepository<User, Long>, PagingAndSortingRepository<User, Long> {

  // ==================== 简化后：使用方法名规范，无需写SQL ====================

  /**
   * 根据用户名查找用户 自动生成: SELECT * FROM upms_user WHERE username = ? AND deleted = false AND tenant_id =
   * ?
   */
  Optional<User> findByUsername(String username);

  /** 根据邮箱查找用户 */
  Optional<User> findByEmail(String email);

  /** 根据手机号查找用户 */
  Optional<User> findByPhone(String phone);

  /** 根据部门ID查找用户列表 */
  List<User> findByDeptId(String deptId);

  /** 统计用户名数量（判断用户名是否存在） */
  long countByUsername(String username);

  /** 统计邮箱数量 */
  long countByEmail(String email);

  /** 统计手机号数量 */
  long countByPhone(String phone);

  /** 统计部门用户数 */
  long countByDeptId(String deptId);

  /** 判断用户名是否存在 */
  boolean existsByUsername(String username);

  // ==================== 复杂查询：仍需使用@Query ====================

  /** 根据角色ID查找用户（多表JOIN） */
  @Query(
      """
        SELECT u.* FROM upms_user u
        INNER JOIN upms_user_role ur ON u.id = ur.user_id
        WHERE ur.role_id = :roleId
        AND u.deleted = false
        AND u.tenant_id = :tenantId
    """)
  List<User> findByRoleId(@Param("roleId") String roleId, @Param("tenantId") String tenantId);

  /** 关键字搜索用户（多字段LIKE） */
  @Query(
      """
        SELECT * FROM upms_user
        WHERE deleted = false
        AND tenant_id = :tenantId
        AND (username LIKE CONCAT('%', :keyword, '%')
            OR nickname LIKE CONCAT('%', :keyword, '%')
            OR real_name LIKE CONCAT('%', :keyword, '%'))
    """)
  List<User> searchByKeyword(@Param("keyword") String keyword, @Param("tenantId") String tenantId);

  /** 逻辑删除用户 */
  @Modifying
  @Query(
      """
        UPDATE upms_user
        SET deleted = true, updated_by = :updatedBy, updated_time = CURRENT_TIMESTAMP
        WHERE id = :id AND tenant_id = :tenantId
      """)
  void softDelete(
      @Param("id") Long id,
      @Param("updatedBy") String updatedBy,
      @Param("tenantId") String tenantId);

  /** 查找所有活跃用户（按创建时间倒序） */
  @Query(
      "SELECT * FROM upms_user WHERE deleted = false AND tenant_id = :tenantId ORDER BY created_time DESC")
  List<User> findAllActive(@Param("tenantId") String tenantId);
}
