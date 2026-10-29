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
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.database.tenant.DatabaseTenant;
import io.github.loadup.modules.upms.client.error.UpmsResultCodeEnum;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.model.User;
import io.github.loadup.modules.upms.infrastructure.converter.UserConverter;
import io.github.loadup.modules.upms.infrastructure.dataobject.UserDO;
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
public class UserGatewayImpl implements UserGateway {
    private static final String FROM =
            " FROM upms_user WHERE deleted = FALSE AND tenant_id IS NOT DISTINCT FROM :tenant AND ";
    private static final String COLUMNS =
            "id, tenant_id, username, password, nickname, real_name, dept_id, email, email_verified, mobile, mobile_verified, avatar, gender, birthday, status, account_non_expired, account_non_locked, credentials_non_expired, last_login_time, last_login_ip, login_fail_count, locked_time, password_update_time, remark, created_by, updated_by, created_at, updated_at, CASE WHEN deleted THEN 1 ELSE 0 END AS deleted";
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;
    private final DatabaseTenant tenant;
    private final UserConverter converter;

    public UserGatewayImpl(
            NamedParameterJdbcTemplate jdbc,
            UserConverter converter,
            @Qualifier("databaseClock") Clock clock,
            DatabaseTenant tenant) {
        this.jdbc = jdbc;
        this.clock = clock;
        this.tenant = tenant;
        this.converter = converter;
    }

    @Override
    @Transactional
    public User save(User user) {
        UserDO row = converter.toDataObject(user);
        lockDepartment(row.getDeptId());
        if (row.getId() == null) row.setId(UUID.randomUUID().toString());
        var now = LocalDateTime.now(clock);
        if (row.getCreatedAt() == null) row.setCreatedAt(now);
        row.setUpdatedAt(now);
        row.setTenantId(tenant.current());
        jdbc.update("""
                INSERT INTO upms_user (id, tenant_id, username, password, nickname, real_name, dept_id, email,
                    email_verified, mobile, mobile_verified, avatar, gender, birthday, status, account_non_expired,
                    account_non_locked, credentials_non_expired, last_login_time, last_login_ip, login_fail_count,
                    locked_time, password_update_time, remark, created_by, updated_by, created_at, updated_at, deleted)
                VALUES (:id, :tenantId, :username, :password, :nickname, :realName, :deptId, :email,
                    COALESCE(:emailVerified, FALSE), :mobile, COALESCE(:mobileVerified, FALSE), :avatar,
                    COALESCE(:gender, 0), :birthday, COALESCE(:status, 1), COALESCE(:accountNonExpired, TRUE),
                    COALESCE(:accountNonLocked, TRUE), COALESCE(:credentialsNonExpired, TRUE), :lastLoginTime,
                    :lastLoginIp, COALESCE(:loginFailCount, 0), :lockedTime, :passwordUpdateTime, :remark,
                    :createdBy, :updatedBy, :createdAt, :updatedAt, FALSE)
                """, new BeanPropertySqlParameterSource(row));
        return converter.toEntity(row);
    }

    @Override
    @Transactional
    public User update(User user) {
        UserDO row = converter.toDataObject(user);
        if (row.getDeptId() != null) lockDepartment(row.getDeptId());
        row.setTenantId(tenant.current());
        row.setUpdatedAt(LocalDateTime.now(clock));
        int changed = jdbc.update("""
                UPDATE upms_user SET username = COALESCE(:username, username), password = COALESCE(:password, password),
                    nickname = COALESCE(:nickname, nickname), real_name = COALESCE(:realName, real_name),
                    dept_id = COALESCE(:deptId, dept_id), email = COALESCE(:email, email),
                    email_verified = COALESCE(:emailVerified, email_verified), mobile = COALESCE(:mobile, mobile),
                    mobile_verified = COALESCE(:mobileVerified, mobile_verified), avatar = COALESCE(:avatar, avatar),
                    gender = COALESCE(:gender, gender), birthday = COALESCE(:birthday, birthday), status = COALESCE(:status, status),
                    account_non_expired = COALESCE(:accountNonExpired, account_non_expired),
                    account_non_locked = COALESCE(:accountNonLocked, account_non_locked),
                    credentials_non_expired = COALESCE(:credentialsNonExpired, credentials_non_expired),
                    last_login_time = COALESCE(:lastLoginTime, last_login_time), last_login_ip = COALESCE(:lastLoginIp, last_login_ip),
                    login_fail_count = COALESCE(:loginFailCount, login_fail_count), locked_time = COALESCE(:lockedTime, locked_time),
                    password_update_time = COALESCE(:passwordUpdateTime, password_update_time), remark = COALESCE(:remark, remark),
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
                "SELECT id FROM upms_user WHERE id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE FOR UPDATE",
                params,
                String.class);
        jdbc.update(
                "UPDATE upms_user_role SET deleted = TRUE, updated_at = :now WHERE user_id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE",
                params);
        jdbc.update(
                "UPDATE upms_user_oauth_binding SET deleted = TRUE, updated_at = :now WHERE user_id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE",
                params);
        jdbc.update(
                "UPDATE upms_user SET deleted = TRUE, updated_at = :now WHERE id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE",
                params);
    }

    @Override
    public Optional<User> findById(String id) {
        return one("id = :value", id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return one("username = :value", username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return one("email = :value", email);
    }

    @Override
    public Optional<User> findByMobile(String mobile) {
        return one("mobile = :value", mobile);
    }

    @Override
    public List<User> findByDeptId(String deptId) {
        return list("dept_id = :value", params().addValue("value", deptId), "");
    }

    @Override
    public PageResult<User> findAll(PageQuery query, String sortBy, String sortOrder) {
        return page("TRUE", params(), query, sortBy, sortOrder);
    }

    @Override
    public PageResult<User> search(String keyword, PageQuery query, String sortBy, String sortOrder) {
        return page(
                "(username LIKE :keyword OR nickname LIKE :keyword OR real_name LIKE :keyword)",
                params().addValue("keyword", "%" + keyword + "%"),
                query,
                sortBy,
                sortOrder);
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByMobile(String mobile) {
        return findByMobile(mobile).isPresent();
    }

    @Override
    public long countByDeptId(String deptId) {
        return count("dept_id = :value", params().addValue("value", deptId));
    }

    private void lockDepartment(String id) {
        if (id == null
                || jdbc.queryForList(
                                "SELECT id FROM upms_department WHERE id = :id AND tenant_id IS NOT DISTINCT FROM :tenant AND deleted = FALSE AND status = 1 FOR UPDATE",
                                params().addValue("id", id),
                                String.class)
                        .isEmpty()) {
            throw new IllegalArgumentException("Active department not found");
        }
    }

    private Optional<User> one(String predicate, Object value) {
        return list(predicate, params().addValue("value", value), " LIMIT 1").stream()
                .findFirst();
    }

    private PageResult<User> page(
            String predicate, MapSqlParameterSource params, PageQuery page, String sortBy, String sortOrder) {
        String column =
                switch (sortBy) {
                    case "createdAt" -> "created_at";
                    case "username" -> "username";
                    case "nickname" -> "nickname";
                    case "id" -> "id";
                    default -> throw new CommonException(UpmsResultCodeEnum.UNSUPPORTED_USER_SORT_FIELD);
                };
        long total = count(predicate, params);
        params.addValue("limit", page.pageSize()).addValue("offset", ((long) page.pageNum() - 1) * page.pageSize());
        String order = " ORDER BY " + column + ("DESC".equalsIgnoreCase(sortOrder) ? " DESC" : " ASC")
                + " LIMIT :limit OFFSET :offset";
        return PageResult.of(list(predicate, params, order), total, page.pageNum(), page.pageSize());
    }

    private long count(String predicate, MapSqlParameterSource params) {
        Long count = jdbc.queryForObject("SELECT COUNT(*)" + FROM + predicate, params, Long.class);
        return count == null ? 0 : count;
    }

    private List<User> list(String predicate, MapSqlParameterSource params, String suffix) {
        return jdbc
                .query(
                        "SELECT " + COLUMNS + FROM + predicate + suffix,
                        params,
                        BeanPropertyRowMapper.newInstance(UserDO.class))
                .stream()
                .map(converter::toEntity)
                .toList();
    }

    private MapSqlParameterSource params() {
        return new MapSqlParameterSource("tenant", tenant.current());
    }
}
