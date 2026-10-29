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
import io.github.loadup.modules.upms.domain.gateway.DepartmentGateway;
import io.github.loadup.modules.upms.domain.model.Department;
import io.github.loadup.modules.upms.infrastructure.converter.DepartmentConverter;
import io.github.loadup.modules.upms.infrastructure.dataobject.DepartmentDO;
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
public class DepartmentGatewayImpl implements DepartmentGateway {
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;
    private final DatabaseTenant tenant;
    private final DepartmentConverter converter;

    public DepartmentGatewayImpl(
            NamedParameterJdbcTemplate jdbc,
            DepartmentConverter converter,
            @Qualifier("databaseClock") Clock clock,
            DatabaseTenant tenant) {
        this.jdbc = jdbc;
        this.clock = clock;
        this.tenant = tenant;
        this.converter = converter;
    }

    @Override
    @Transactional
    public Department save(Department department) {
        DepartmentDO row = converter.toDataObject(department);
        lockParent(row.getParentId());
        if (row.getId() == null) row.setId(UUID.randomUUID().toString());
        var now = LocalDateTime.now(clock);
        if (row.getCreatedAt() == null) row.setCreatedAt(now);
        row.setUpdatedAt(now);
        row.setTenantId(tenant.current());
        jdbc.update("""
                INSERT INTO upms_department (id, tenant_id, parent_id, dept_name, dept_code, dept_level,
                    sort_order, leader_user_id, phone, email, status, remark, created_by, updated_by,
                    created_at, updated_at, deleted)
                VALUES (:id, :tenantId, :parentId, :deptName, :deptCode, COALESCE(:deptLevel, 1),
                    COALESCE(:sortOrder, 0), :leaderUserId, :mobile, :email, COALESCE(:status, 1),
                    :remark, :createdBy, :updatedBy, :createdAt, :updatedAt, FALSE)
                """, new BeanPropertySqlParameterSource(row));
        return converter.toEntity(row);
    }

    @Override
    @Transactional
    public Department update(Department department) {
        DepartmentDO row = converter.toDataObject(department);
        lockParent(row.getParentId());
        row.setTenantId(tenant.current());
        row.setUpdatedAt(LocalDateTime.now(clock));
        jdbc.update("""
                UPDATE upms_department SET parent_id = COALESCE(:parentId, parent_id),
                    dept_name = COALESCE(:deptName, dept_name), dept_code = COALESCE(:deptCode, dept_code),
                    dept_level = COALESCE(:deptLevel, dept_level), sort_order = COALESCE(:sortOrder, sort_order),
                    leader_user_id = COALESCE(:leaderUserId, leader_user_id), phone = COALESCE(:mobile, phone),
                    email = COALESCE(:email, email), status = COALESCE(:status, status),
                    remark = COALESCE(:remark, remark), updated_by = COALESCE(:updatedBy, updated_by), updated_at = :updatedAt
                WHERE id = :id AND tenant_id IS NOT DISTINCT FROM :tenantId AND deleted = FALSE
                """, new BeanPropertySqlParameterSource(row));
        return converter.toEntity(row);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        var values = params().addValue("id", id).addValue("now", LocalDateTime.now(clock));
        jdbc.queryForList(
                "SELECT id FROM upms_department WHERE id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE FOR UPDATE",
                values,
                String.class);
        if (hasChildren(id) || hasUsers(id))
            throw new IllegalStateException("Department still has users or child departments");
        jdbc.update(
                "UPDATE upms_role_department SET deleted = TRUE, updated_at = :now WHERE dept_id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE",
                values);
        jdbc.update(
                "UPDATE upms_department SET deleted = TRUE, updated_at = :now WHERE id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE",
                values);
    }

    @Override
    public Optional<Department> findById(String id) {
        return find("id = :value", id).stream().findFirst();
    }

    @Override
    public Optional<Department> findByDeptCode(String deptCode) {
        return find("dept_code = :value", deptCode).stream().findFirst();
    }

    @Override
    public List<Department> findByParentId(String parentId) {
        return find("parent_id = :value", parentId);
    }

    @Override
    public List<Department> findAll() {
        return find("TRUE", null);
    }

    @Override
    public List<Department> findAllEnabled() {
        return find("status = 1", null);
    }

    @Override
    public List<Department> findRootDepartments() {
        return find("(parent_id IS NULL OR parent_id = '0')", null);
    }

    @Override
    public boolean existsByDeptCode(String deptCode) {
        return !findByDeptCode(deptCode).isEmpty();
    }

    @Override
    public boolean hasChildren(String deptId) {
        return !findByParentId(deptId).isEmpty();
    }

    @Override
    public boolean hasUsers(String deptId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM upms_user WHERE dept_id = :dept AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE",
                params().addValue("dept", deptId),
                Long.class);
        return count != null && count > 0;
    }

    @Override
    public List<Department> buildTree() {
        List<Department> departments = findAll();
        return departments.stream()
                .filter(dept -> dept.getParentId() == null || "0".equals(dept.getParentId()))
                .peek(dept -> buildChildren(dept, departments))
                .toList();
    }

    private void buildChildren(Department parent, List<Department> departments) {
        parent.setChildren(departments.stream()
                .filter(dept -> parent.getId().equals(dept.getParentId()))
                .peek(child -> buildChildren(child, departments))
                .toList());
    }

    private void lockParent(String parentId) {
        if (parentId == null || "0".equals(parentId)) return;
        if (jdbc.queryForList(
                        "SELECT id FROM upms_department WHERE id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE AND status = 1 FOR UPDATE",
                        params().addValue("id", parentId),
                        String.class)
                .isEmpty()) {
            throw new IllegalArgumentException("Active parent department not found");
        }
    }

    private List<Department> find(String predicate, Object value) {
        return jdbc
                .query(
                        "SELECT d.id, d.tenant_id, d.parent_id, d.dept_name, d.dept_code, d.dept_level, d.sort_order, d.leader_user_id, d.phone AS mobile, d.email, d.status, d.remark, d.created_by, d.updated_by, d.created_at, d.updated_at, CASE WHEN d.deleted THEN 1 ELSE 0 END AS deleted FROM upms_department d WHERE d.tenant_id IS NOT DISTINCT FROM :tenant AND d.deleted = FALSE AND "
                                + predicate,
                        params().addValue("value", value),
                        BeanPropertyRowMapper.newInstance(DepartmentDO.class))
                .stream()
                .map(converter::toEntity)
                .toList();
    }

    private MapSqlParameterSource params() {
        return new MapSqlParameterSource("tenant", tenant.current());
    }
}
