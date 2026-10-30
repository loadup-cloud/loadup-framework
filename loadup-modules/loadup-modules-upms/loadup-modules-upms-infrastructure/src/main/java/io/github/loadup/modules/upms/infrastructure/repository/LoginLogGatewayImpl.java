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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LoginLogGatewayImpl implements LoginLogGateway {
    private static final Short SUCCESS = 1;
    private static final Short FAILURE = 0;
    private final NamedParameterJdbcTemplate jdbc;
    private final LoginLogRepository loginLogs;
    private final Clock clock;
    private final DatabaseTenant tenant;
    private final LoginLogConverter converter;

    public LoginLogGatewayImpl(
            NamedParameterJdbcTemplate jdbc,
            LoginLogRepository loginLogs,
            LoginLogConverter converter,
            @Qualifier("databaseClock") Clock clock,
            DatabaseTenant tenant) {
        this.jdbc = jdbc;
        this.loginLogs = loginLogs;
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
        return loginLogs.findById(id).map(converter::toEntity);
    }

    @Override
    public PageResult<LoginLog> findByUserId(String userId, PageQuery query) {
        return page(loginLogs.findByUserId(userId, pageable(query)));
    }

    @Override
    public PageResult<LoginLog> findByUsername(String username, PageQuery query) {
        return page(loginLogs.findByUsername(username, pageable(query)));
    }

    @Override
    public PageResult<LoginLog> findByDateRange(LocalDateTime startTime, LocalDateTime endTime, PageQuery query) {
        return page(loginLogs.findByDateRange(startTime, endTime, pageable(query)));
    }

    @Override
    public PageResult<LoginLog> findFailedLogins(LocalDateTime startTime, LocalDateTime endTime, PageQuery query) {
        return page(loginLogs.findFailedLogins(startTime, endTime, pageable(query)));
    }

    @Override
    public void deleteBeforeDate(LocalDateTime date) {
        jdbc.update(
                "UPDATE upms_login_log SET deleted = TRUE, updated_at = :now WHERE deleted = FALSE AND tenant_id IS NOT DISTINCT FROM :tenant AND login_time < :date",
                params().addValue("date", date).addValue("now", LocalDateTime.now(clock)));
    }

    @Override
    public List<LoginLog> findByLoginTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return loginLogs.findByLoginTimeBetween(startTime, endTime).stream()
                .map(converter::toEntity)
                .toList();
    }

    @Override
    public PageResult<LoginLog> findAll(PageQuery query) {
        return page(loginLogs.findAll(pageable(query)));
    }

    @Override
    public long countLoginAttempts(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        return loginLogs.countLoginAttempts(userId, startTime, endTime);
    }

    @Override
    public long countFailedLoginAttempts(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        return loginLogs.countFailedLoginAttempts(userId, startTime, endTime);
    }

    @Override
    public List<LoginLog> findByUserId(String userId) {
        return loginLogs.findByUserId(userId).stream().map(converter::toEntity).toList();
    }

    @Override
    public Optional<LoginLog> findLastSuccessfulLogin(String userId) {
        return loginLogs.findLastSuccessfulLogin(userId).map(converter::toEntity);
    }

    private PageResult<LoginLog> page(org.springframework.data.domain.Page<LoginLogDO> page) {
        return PageResult.of(
                page.stream().map(converter::toEntity).toList(),
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize());
    }

    private PageRequest pageable(PageQuery query) {
        return PageRequest.of(query.pageNum() - 1, query.pageSize(), Sort.by(Sort.Direction.DESC, "loginTime"));
    }

    private MapSqlParameterSource params() {
        return new MapSqlParameterSource("tenant", tenant.current());
    }
}
