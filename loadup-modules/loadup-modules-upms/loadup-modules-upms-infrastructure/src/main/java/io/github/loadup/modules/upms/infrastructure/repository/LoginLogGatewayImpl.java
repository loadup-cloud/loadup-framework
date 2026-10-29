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
import io.github.loadup.modules.upms.domain.gateway.LoginLogGateway;
import io.github.loadup.modules.upms.domain.model.LoginLog;
import io.github.loadup.modules.upms.infrastructure.converter.LoginLogConverter;
import io.github.loadup.modules.upms.infrastructure.dataobject.LoginLogDO;
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

@Repository
public class LoginLogGatewayImpl implements LoginLogGateway {
    private static final String FROM =
            " FROM upms_login_log WHERE deleted = FALSE AND tenant_id IS NOT DISTINCT FROM :tenant AND ";
    private static final String COLUMNS =
            "id, tenant_id, user_id, username, login_time, logout_time, ip_address, login_location, browser, os, login_status, login_message, login_type, provider, created_at, updated_at, CASE WHEN deleted THEN 1 ELSE 0 END AS deleted";
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;
    private final DatabaseTenant tenant;
    private final LoginLogConverter converter;

    public LoginLogGatewayImpl(
            NamedParameterJdbcTemplate jdbc,
            LoginLogConverter converter,
            @Qualifier("databaseClock") Clock clock,
            DatabaseTenant tenant) {
        this.jdbc = jdbc;
        this.clock = clock;
        this.tenant = tenant;
        this.converter = converter;
    }

    @Override
    public LoginLog save(LoginLog entity) {
        LoginLogDO row = converter.toDataObject(entity);
        if (row.getId() == null) row.setId(UUID.randomUUID().toString());
        var now = LocalDateTime.now(clock);
        if (row.getCreatedAt() == null) row.setCreatedAt(now);
        row.setUpdatedAt(now);
        row.setTenantId(tenant.current());
        jdbc.update("""
                INSERT INTO upms_login_log (id, tenant_id, user_id, username, login_time, logout_time, ip_address,
                    login_location, browser, os, login_status, login_message, login_type, provider, created_at, updated_at, deleted)
                VALUES (:id, :tenantId, :userId, :username, :loginTime, :logoutTime, :ipAddress,
                    :loginLocation, :browser, :os, COALESCE(:loginStatus, 1), :loginMessage, :loginType,
                    :provider, :createdAt, :updatedAt, FALSE)
                """, new BeanPropertySqlParameterSource(row));
        return converter.toEntity(row);
    }

    @Override
    public Optional<LoginLog> findById(String id) {
        return list("id = :id", params().addValue("id", id), " LIMIT 1").stream()
                .findFirst();
    }

    @Override
    public PageResult<LoginLog> findByUserId(String userId, PageQuery query) {
        return page("user_id = :userId", params().addValue("userId", userId), query);
    }

    @Override
    public PageResult<LoginLog> findByUsername(String username, PageQuery query) {
        return page("username = :username", params().addValue("username", username), query);
    }

    @Override
    public PageResult<LoginLog> findByDateRange(LocalDateTime startTime, LocalDateTime endTime, PageQuery query) {
        return page("login_time BETWEEN :start AND :end", range(startTime, endTime), query);
    }

    @Override
    public PageResult<LoginLog> findFailedLogins(LocalDateTime startTime, LocalDateTime endTime, PageQuery query) {
        return page("login_status = 0 AND login_time BETWEEN :start AND :end", range(startTime, endTime), query);
    }

    @Override
    public void deleteBeforeDate(LocalDateTime date) {
        jdbc.update(
                "UPDATE upms_login_log SET deleted = TRUE, updated_at = :now WHERE deleted = FALSE AND tenant_id IS NOT DISTINCT FROM :tenant AND login_time < :date",
                params().addValue("date", date).addValue("now", LocalDateTime.now(clock)));
    }

    @Override
    public List<LoginLog> findByLoginTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return list("login_time BETWEEN :start AND :end", range(startTime, endTime), "");
    }

    @Override
    public PageResult<LoginLog> findAll(PageQuery query) {
        return page("TRUE", params(), query);
    }

    @Override
    public long countLoginAttempts(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        return count(
                "user_id = :userId AND login_time BETWEEN :start AND :end",
                range(startTime, endTime).addValue("userId", userId));
    }

    @Override
    public long countFailedLoginAttempts(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        return count(
                "user_id = :userId AND login_status = 0 AND login_time BETWEEN :start AND :end",
                range(startTime, endTime).addValue("userId", userId));
    }

    @Override
    public List<LoginLog> findByUserId(String userId) {
        return list("user_id = :userId", params().addValue("userId", userId), "");
    }

    @Override
    public Optional<LoginLog> findLastSuccessfulLogin(String userId) {
        return list("user_id = :userId AND login_status = 1", params().addValue("userId", userId), " LIMIT 1").stream()
                .findFirst();
    }

    private PageResult<LoginLog> page(String predicate, MapSqlParameterSource params, PageQuery page) {
        long total = count(predicate, params);
        params.addValue("limit", page.pageSize()).addValue("offset", ((long) page.pageNum() - 1) * page.pageSize());
        return PageResult.of(
                list(predicate, params, " LIMIT :limit OFFSET :offset"), total, page.pageNum(), page.pageSize());
    }

    private long count(String predicate, MapSqlParameterSource params) {
        Long count = jdbc.queryForObject("SELECT COUNT(*)" + FROM + predicate, params, Long.class);
        return count == null ? 0 : count;
    }

    private List<LoginLog> list(String predicate, MapSqlParameterSource params, String limit) {
        return jdbc
                .query(
                        "SELECT " + COLUMNS + FROM + predicate + " ORDER BY login_time DESC" + limit,
                        params,
                        BeanPropertyRowMapper.newInstance(LoginLogDO.class))
                .stream()
                .map(converter::toEntity)
                .toList();
    }

    private MapSqlParameterSource range(LocalDateTime start, LocalDateTime end) {
        return params().addValue("start", start).addValue("end", end);
    }

    private MapSqlParameterSource params() {
        return new MapSqlParameterSource("tenant", tenant.current());
    }
}
