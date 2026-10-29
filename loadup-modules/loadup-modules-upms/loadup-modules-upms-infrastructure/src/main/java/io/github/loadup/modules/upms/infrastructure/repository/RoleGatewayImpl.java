/*
 * #%L
 * Loadup Modules UPMS Infrastructure Layer
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
package io.github.loadup.modules.upms.infrastructure.repository;

import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.commons.dto.PageQuery;
import io.github.loadup.components.database.tenant.DatabaseTenant;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.model.Role;
import io.github.loadup.modules.upms.infrastructure.converter.RoleConverter;
import io.github.loadup.modules.upms.infrastructure.dataobject.RoleDO;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class RoleGatewayImpl implements RoleGateway {
    private static final String FROM =
            " FROM upms_role r WHERE r.deleted = FALSE AND r.tenant_id IS NOT DISTINCT FROM :tenant AND ";
    private static final String COLUMNS =
            "r.id, r.tenant_id, r.role_name, r.role_code, r.parent_role_id AS parent_id, r.role_level, r.data_scope, r.sort_order, r.status, r.remark, r.created_by, r.updated_by, r.created_at, r.updated_at, CASE WHEN r.deleted THEN 1 ELSE 0 END AS deleted";
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;
    private final DatabaseTenant tenant;
    private final RoleConverter converter;

    public RoleGatewayImpl(
            NamedParameterJdbcTemplate jdbc,
            RoleConverter converter,
            @Qualifier("databaseClock") Clock clock,
            DatabaseTenant tenant) {
        this.jdbc = jdbc;
        this.clock = clock;
        this.tenant = tenant;
        this.converter = converter;
    }

    @Override
    @Transactional
    public Role save(Role role) {
        RoleDO row = converter.toDataObject(role);
        lockParent(row.getParentId());
        if (row.getId() == null) row.setId(UUID.randomUUID().toString());
        var now = LocalDateTime.now(clock);
        if (row.getCreatedAt() == null) row.setCreatedAt(now);
        row.setUpdatedAt(now);
        row.setTenantId(tenant.current());
        jdbc.update("""
                INSERT INTO upms_role (id, tenant_id, role_name, role_code, parent_role_id, role_level, data_scope,
                    sort_order, status, remark, created_by, updated_by, created_at, updated_at, deleted)
                VALUES (:id, :tenantId, :roleName, :roleCode, :parentId, COALESCE(:roleLevel, 1), COALESCE(:dataScope, 1),
                    COALESCE(:sortOrder, 0), COALESCE(:status, 1), :remark, :createdBy, :updatedBy, :createdAt, :updatedAt, FALSE)
                """, new BeanPropertySqlParameterSource(row));
        return converter.toEntity(row);
    }

    @Override
    @Transactional
    public Role update(Role role) {
        RoleDO row = converter.toDataObject(role);
        lockParent(row.getParentId());
        row.setTenantId(tenant.current());
        row.setUpdatedAt(LocalDateTime.now(clock));
        int changed = jdbc.update("""
                UPDATE upms_role SET role_name = COALESCE(:roleName, role_name), role_code = COALESCE(:roleCode, role_code),
                    parent_role_id = COALESCE(:parentId, parent_role_id), role_level = COALESCE(:roleLevel, role_level),
                    data_scope = COALESCE(:dataScope, data_scope), sort_order = COALESCE(:sortOrder, sort_order),
                    status = COALESCE(:status, status), remark = COALESCE(:remark, remark),
                    updated_by = COALESCE(:updatedBy, updated_by), updated_at = :updatedAt
                WHERE id = :id AND tenant_id IS NOT DISTINCT FROM :tenantId AND deleted = FALSE
                """, new BeanPropertySqlParameterSource(row));
        if (changed != 1) throw new org.springframework.dao.IncorrectResultSizeDataAccessException(1, changed);
        return converter.toEntity(row);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        var params = params().addValue("id", id).addValue("now", LocalDateTime.now(clock));
        jdbc.queryForList(
                "SELECT id FROM upms_role WHERE id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE FOR UPDATE",
                params,
                String.class);
        if (!findByParentId(id).isEmpty()) {
            throw new io.github.loadup.commons.error.CommonException(io.github.loadup.modules.upms.client.error.UpmsResultCodeEnum.ROLE_STILL_HAS_CHILD_ROLES);
        }
        if (countUsersByRoleId(id) > 0) {
            throw new io.github.loadup.commons.error.CommonException(io.github.loadup.modules.upms.client.error.UpmsResultCodeEnum.ROLE_IS_STILL_ASSIGNED_TO_USERS);
        }
        for (String table : List.of("upms_role_permission", "upms_role_department", "upms_user_role")) {
            jdbc.update(
                    "UPDATE " + table
                            + " SET deleted = TRUE, updated_at = :now WHERE role_id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE",
                    params);
        }
        jdbc.update(
                "UPDATE upms_role SET deleted = TRUE, updated_at = :now WHERE id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE",
                params);
    }

    @Override
    public Optional<Role> findById(String id) {
        return find("r.id = :value", id).stream().findFirst();
    }

    @Override
    public Optional<Role> findByRoleCode(String roleCode) {
        return find("r.role_code = :value", roleCode).stream().findFirst();
    }

    @Override
    public List<Role> findByUserId(String userId) {
        return find(
                "EXISTS (SELECT 1 FROM upms_user_role ur WHERE ur.role_id = r.id AND ur.user_id = :value AND ur.deleted = FALSE AND ur.tenant_id IS NOT DISTINCT FROM :tenant)",
                userId);
    }

    @Override
    public List<Role> findByParentId(String parentId) {
        return find("r.parent_role_id = :value", parentId);
    }

    @Override
    public List<Role> findAll() {
        return find("TRUE", null);
    }

    @Override
    public List<Role> findAllEnabled() {
        return find("r.status = 1", null);
    }

    @Override
    public boolean existsByRoleCode(String roleCode) {
        return findByRoleCode(roleCode).isPresent();
    }

    @Override
    @Transactional
    public void assignRoleToUser(String userId, String roleId, String operatorId) {
        lockActive("upms_role", roleId);
        lockActive("upms_user", userId);
        insertRelation("upms_user_role", "user_id", userId, "role_id", roleId, operatorId);
    }

    @Override
    public void removeRoleFromUser(String userId, String roleId) {
        removeRelation("upms_user_role", "user_id", userId, "role_id", List.of(roleId));
    }

    @Override
    public List<String> getUserRoleIds(String userId) {
        return ids("upms_user_role", "role_id", "user_id", userId);
    }

    @Override
    @Transactional
    public void assignPermissionsToRole(String roleId, List<String> permissionIds) {
        lockActive("upms_role", roleId);
        for (String permissionId : permissionIds.stream().distinct().sorted().toList()) {
            lockActive("upms_permission", permissionId);
            insertRelation("upms_role_permission", "role_id", roleId, "permission_id", permissionId, null);
        }
    }

    @Override
    public void removePermissionsFromRole(String roleId, List<String> permissionIds) {
        removeRelation("upms_role_permission", "role_id", roleId, "permission_id", permissionIds);
    }

    @Override
    @Transactional
    public void assignDepartmentsToRole(String roleId, List<String> departmentIds) {
        lockActive("upms_role", roleId);
        for (String deptId : departmentIds.stream().distinct().sorted().toList()) {
            lockActive("upms_department", deptId);
            insertRelation("upms_role_department", "role_id", roleId, "dept_id", deptId, null);
        }
    }

    @Override
    public void removeDepartmentsFromRole(String roleId, List<String> departmentIds) {
        removeRelation("upms_role_department", "role_id", roleId, "dept_id", departmentIds);
    }

    @Override
    public List<String> findDepartmentIdsByRoleId(String roleId) {
        return ids("upms_role_department", "dept_id", "role_id", roleId);
    }

    @Override
    public PageResult<Role> findAll(PageQuery query) {
        var params = params();
        Long total = jdbc.queryForObject("SELECT COUNT(*)" + FROM + "TRUE", params, Long.class);
        params.addValue("limit", query.pageSize()).addValue("offset", ((long) query.pageNum() - 1) * query.pageSize());
        var rows = select("TRUE", params, " ORDER BY r.id LIMIT :limit OFFSET :offset");
        return PageResult.of(rows, total == null ? 0 : total, query.pageNum(), query.pageSize());
    }

    @Override
    public long countUsersByRoleId(String roleId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM upms_user_role WHERE role_id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE",
                params().addValue("id", roleId),
                Long.class);
        return count == null ? 0 : count;
    }

    private void lockParent(String id) {
        if (id == null) return;
        if (jdbc.queryForList("SELECT id FROM upms_role WHERE id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE FOR UPDATE",
                params().addValue("id", id), String.class).isEmpty()) {
            throw new io.github.loadup.commons.error.CommonException(io.github.loadup.modules.upms.client.error.UpmsResultCodeEnum.PARENT_ROLE_DOES_NOT_EXIST);
        }
    }

    private void lockActive(String table, String id) {
        var rows = jdbc.queryForList(
                "SELECT id FROM " + table
                        + " WHERE id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE AND status = 1 FOR UPDATE",
                params().addValue("id", id),
                String.class);
        if (rows.isEmpty()) throw new IllegalArgumentException("Active related entity not found: " + table);
    }

    private void insertRelation(
            String table, String firstColumn, String first, String secondColumn, String second, String operator) {
        var values = params().addValue("id", UUID.randomUUID().toString())
                .addValue("first", first)
                .addValue("second", second)
                .addValue("operator", operator)
                .addValue("now", LocalDateTime.now(clock));
        int changed = jdbc.update(
                "INSERT INTO " + table + " (id, tenant_id, " + firstColumn + ", " + secondColumn
                        + ", created_by, created_at, updated_at, deleted) "
                        + "VALUES (:id, :tenant, :first, :second, :operator, :now, :now, FALSE) ON CONFLICT ("
                        + firstColumn + ", " + secondColumn + ") "
                        + "DO UPDATE SET deleted = FALSE, updated_at = EXCLUDED.updated_at WHERE " + table
                        + ".tenant_id IS NOT DISTINCT FROM EXCLUDED.tenant_id",
                values);
        if (changed != 1) throw new IllegalStateException("Relation was not saved");
    }

    private void removeRelation(
            String table, String firstColumn, String first, String secondColumn, List<String> seconds) {
        if (seconds.isEmpty()) return;
        jdbc.update(
                "UPDATE " + table + " SET deleted = TRUE, updated_at = :now WHERE " + firstColumn + " = :first AND "
                        + secondColumn
                        + " IN (:seconds) AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE",
                params().addValue("first", first)
                        .addValue("seconds", seconds)
                        .addValue("now", LocalDateTime.now(clock)));
    }

    private List<String> ids(String table, String resultColumn, String keyColumn, String key) {
        return jdbc.queryForList(
                "SELECT " + resultColumn + " FROM " + table + " WHERE " + keyColumn
                        + " = :key AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE",
                params().addValue("key", key),
                String.class);
    }

    private List<Role> find(String predicate, Object value) {
        return select(predicate, params().addValue("value", value), "");
    }

    private List<Role> select(String predicate, MapSqlParameterSource params, String suffix) {
        return jdbc
                .query(
                        "SELECT " + COLUMNS + FROM + predicate + suffix,
                        params,
                        BeanPropertyRowMapper.newInstance(RoleDO.class))
                .stream()
                .map(converter::toEntity)
                .toList();
    }

    private MapSqlParameterSource params() {
        return new MapSqlParameterSource("tenant", tenant.current());
    }
}
