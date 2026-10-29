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

import io.github.loadup.components.database.tenant.DatabaseTenant;
import io.github.loadup.modules.upms.domain.gateway.PermissionGateway;
import io.github.loadup.modules.upms.domain.model.Permission;
import io.github.loadup.modules.upms.infrastructure.converter.PermissionConverter;
import io.github.loadup.modules.upms.infrastructure.dataobject.PermissionDO;
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
public class PermissionGatewayImpl implements PermissionGateway {
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;
    private final DatabaseTenant tenant;
    private final PermissionConverter converter;

    public PermissionGatewayImpl(
            NamedParameterJdbcTemplate jdbc,
            PermissionConverter converter,
            @Qualifier("databaseClock") Clock clock,
            DatabaseTenant tenant) {
        this.jdbc = jdbc;
        this.clock = clock;
        this.tenant = tenant;
        this.converter = converter;
    }

    @Override
    @Transactional
    public Permission save(Permission permission) {
        PermissionDO row = converter.toDataObject(permission);
        lockParent(row.getParentId());
        if (row.getId() == null) row.setId(UUID.randomUUID().toString());
        var now = LocalDateTime.now(clock);
        if (row.getCreatedAt() == null) row.setCreatedAt(now);
        row.setUpdatedAt(now);
        row.setTenantId(tenant.current());
        jdbc.update("""
                INSERT INTO upms_permission (id, tenant_id, parent_id, permission_name, permission_code,
                    permission_type, resource_path, http_method, icon, component_path, sort_order, visible,
                    status, remark, created_by, updated_by, created_at, updated_at, deleted)
                VALUES (:id, :tenantId, :parentId, :permissionName, :permissionCode, :permissionType,
                    :resourcePath, :httpMethod, :icon, :componentPath, COALESCE(:sortOrder, 0),
                    COALESCE(:visible, TRUE), COALESCE(:status, 1), :remark, :createdBy, :updatedBy,
                    :createdAt, :updatedAt, FALSE)
                """, new BeanPropertySqlParameterSource(row));
        return converter.toEntity(row);
    }

    @Override
    @Transactional
    public Permission update(Permission permission) {
        PermissionDO row = converter.toDataObject(permission);
        lockParent(row.getParentId());
        row.setTenantId(tenant.current());
        row.setUpdatedAt(LocalDateTime.now(clock));
        int changed = jdbc.update("""
                UPDATE upms_permission SET parent_id = COALESCE(:parentId, parent_id),
                    permission_name = COALESCE(:permissionName, permission_name),
                    permission_code = COALESCE(:permissionCode, permission_code),
                    permission_type = COALESCE(:permissionType, permission_type),
                    resource_path = COALESCE(:resourcePath, resource_path), http_method = COALESCE(:httpMethod, http_method),
                    icon = COALESCE(:icon, icon), component_path = COALESCE(:componentPath, component_path),
                    sort_order = COALESCE(:sortOrder, sort_order), visible = COALESCE(:visible, visible),
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
                "SELECT id FROM upms_permission WHERE id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE FOR UPDATE",
                params,
                String.class);
        if (!findByParentId(id).isEmpty()) {
            throw new io.github.loadup.commons.error.CommonException(
                    io.github.loadup.modules.upms.client.error.UpmsResultCodeEnum.PERMISSION_CONTAINS_CHILD_PERMISSIONS);
        }
        jdbc.update(
                "UPDATE upms_role_permission SET deleted = TRUE, updated_at = :now WHERE permission_id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE",
                params);
        jdbc.update(
                "UPDATE upms_permission SET deleted = TRUE, updated_at = :now WHERE id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE",
                params);
    }

    @Override
    public Optional<Permission> findById(String id) {
        return find("p.id = :value", id).stream().findFirst();
    }

    @Override
    public Optional<Permission> findByPermissionCode(String permissionCode) {
        return find("p.permission_code = :value", permissionCode).stream().findFirst();
    }

    @Override
    public List<Permission> findByRoleId(String roleId) {
        return find(
                "EXISTS (SELECT 1 FROM upms_role_permission rp WHERE rp.permission_id = p.id AND rp.role_id = :value AND rp.deleted = FALSE AND rp.tenant_id IS NOT DISTINCT FROM :tenant)",
                roleId);
    }

    @Override
    public List<Permission> findByParentId(String parentId) {
        return find("p.parent_id = :value", parentId);
    }

    @Override
    public List<Permission> findByPermissionType(Short permissionType) {
        return find("p.permission_type = :value", permissionType);
    }

    @Override
    public List<Permission> findAll() {
        return find("TRUE", null);
    }

    @Override
    public List<Permission> findAllEnabled() {
        return find("p.status = 1", null);
    }

    @Override
    public List<Permission> findMenuPermissions() {
        return find("p.permission_type IN (1, 2) AND p.status = 1 ORDER BY p.sort_order ASC", null);
    }

    @Override
    public boolean existsByPermissionCode(String permissionCode) {
        return findByPermissionCode(permissionCode).isPresent();
    }

    private void lockParent(String id) {
        if (id == null || "0".equals(id)) return;
        if (jdbc.queryForList("SELECT id FROM upms_permission WHERE id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE FOR UPDATE",
                params().addValue("id", id), String.class).isEmpty()) {
            throw new io.github.loadup.commons.error.CommonException(
                    io.github.loadup.modules.upms.client.error.UpmsResultCodeEnum.PERMISSION_NOT_FOUND);
        }
    }

    private List<Permission> find(String predicate, Object value) {
        return jdbc
                .query(
                        "SELECT p.id, p.tenant_id, p.parent_id, p.permission_name, p.permission_code, p.permission_type, p.resource_path, p.http_method, p.icon, p.component_path, p.sort_order, p.visible, p.status, p.remark, p.created_by, p.updated_by, p.created_at, p.updated_at, CASE WHEN p.deleted THEN 1 ELSE 0 END AS deleted FROM upms_permission p WHERE p.tenant_id IS NOT DISTINCT FROM :tenant AND p.deleted = FALSE AND "
                                + predicate,
                        params().addValue("value", value),
                        BeanPropertyRowMapper.newInstance(PermissionDO.class))
                .stream()
                .map(converter::toEntity)
                .toList();
    }

    private MapSqlParameterSource params() {
        return new MapSqlParameterSource("tenant", tenant.current());
    }
}
