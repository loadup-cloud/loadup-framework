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
import io.github.loadup.modules.upms.domain.gateway.LoginLogGateway;
import io.github.loadup.modules.upms.domain.model.LoginLog;
import io.github.loadup.modules.upms.infrastructure.converter.LoginLogConverter;
import io.github.loadup.modules.upms.infrastructure.dataobject.LoginLogDO;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class LoginLogGatewayImpl implements LoginLogGateway {
    private static final Short SUCCESS = 1;
    private static final Short FAILURE = 0;
    private static final String COLUMNS =
            "id, user_id, username, login_time, logout_time, ip_address, login_location, browser, os, login_status, login_message, login_type, provider, created_at, updated_at, deleted";
    private static final String FROM = " FROM upms_login_log WHERE deleted = FALSE";

    private final JdbcClient jdbc;
    private final Clock clock;
    private final LoginLogConverter converter;

    public LoginLogGatewayImpl(JdbcClient jdbc, LoginLogConverter converter, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
        this.converter = converter;
    }

    private static LoginLogDO map(ResultSet rs, int rowNumber) throws SQLException {
        var row = new LoginLogDO();
        row.setId(rs.getString("id"));
        row.setUserId(rs.getString("user_id"));
        row.setUsername(rs.getString("username"));
        row.setLoginTime(rs.getObject("login_time", LocalDateTime.class));
        row.setLogoutTime(rs.getObject("logout_time", LocalDateTime.class));
        row.setIpAddress(rs.getString("ip_address"));
        row.setLoginLocation(rs.getString("login_location"));
        row.setBrowser(rs.getString("browser"));
        row.setOs(rs.getString("os"));
        row.setLoginStatus(rs.getObject("login_status", Short.class));
        row.setLoginMessage(rs.getString("login_message"));
        row.setLoginType(rs.getString("login_type"));
        row.setProvider(rs.getString("provider"));
        row.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        row.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        row.setDeleted(rs.getBoolean("deleted"));
        return row;
    }

    @Override
    public LoginLog save(LoginLog entity) {
        LoginLogDO row = converter.toDataObject(entity);
        if (row.getId() == null) {
            row.setId(UUID.randomUUID().toString());
        }
        var now = LocalDateTime.now(clock);
        if (row.getCreatedAt() == null) {
            row.setCreatedAt(now);
        }
        row.setUpdatedAt(now);
        jdbc.sql("""
            INSERT INTO upms_login_log (id, user_id, username, login_time, logout_time, ip_address,
                login_location, browser, os, login_status, login_message, login_type, provider, created_at, updated_at, deleted)
            VALUES (:id, :userId, :username, :loginTime, :logoutTime, :ipAddress,
                :loginLocation, :browser, :os, COALESCE(:loginStatus, 1), :loginMessage, :loginType,
                :provider, :createdAt, :updatedAt, FALSE)
            """).paramSource(new BeanPropertySqlParameterSource(row)).update();
        return converter.toEntity(row);
    }

    @Override
    public Optional<LoginLog> findById(String id) {
        return jdbc.sql("SELECT " + COLUMNS + FROM + " AND id = :id")
                .param("id", id)
                .query(LoginLogGatewayImpl::map)
                .optional()
                .map(converter::toEntity);
    }

    @Override
    public PageResult<LoginLog> findByUserId(String userId, PageQuery query) {
        return page(" AND user_id = :userId", new MapSqlParameterSource("userId", userId), query);
    }

    @Override
    public PageResult<LoginLog> findByUsername(String username, PageQuery query) {
        return page(" AND username = :username", new MapSqlParameterSource("username", username), query);
    }

    @Override
    public PageResult<LoginLog> findByDateRange(LocalDateTime startTime, LocalDateTime endTime, PageQuery query) {
        return page(
                " AND login_time BETWEEN :startTime AND :endTime",
                new MapSqlParameterSource("startTime", startTime).addValue("endTime", endTime),
                query);
    }

    @Override
    public PageResult<LoginLog> findFailedLogins(LocalDateTime startTime, LocalDateTime endTime, PageQuery query) {
        return page(
                " AND login_status = :loginStatus AND login_time BETWEEN :startTime AND :endTime",
                new MapSqlParameterSource("loginStatus", FAILURE)
                        .addValue("startTime", startTime)
                        .addValue("endTime", endTime),
                query);
    }

    @Override
    public void deleteBeforeDate(LocalDateTime date) {
        jdbc.sql(
                        "UPDATE upms_login_log SET deleted = TRUE, updated_at = :now WHERE deleted = FALSE AND login_time < :date")
                .param("now", LocalDateTime.now(clock))
                .param("date", date)
                .update();
    }

    @Override
    public List<LoginLog> findByLoginTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return jdbc
                .sql("SELECT " + COLUMNS + FROM
                        + " AND login_time BETWEEN :startTime AND :endTime ORDER BY login_time DESC")
                .param("startTime", startTime)
                .param("endTime", endTime)
                .query(LoginLogGatewayImpl::map)
                .list()
                .stream()
                .map(converter::toEntity)
                .toList();
    }

    @Override
    public PageResult<LoginLog> findAll(PageQuery query) {
        return page("", new MapSqlParameterSource(), query);
    }

    @Override
    public long countLoginAttempts(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        return count(
                " AND user_id = :userId AND login_time BETWEEN :startTime AND :endTime",
                new MapSqlParameterSource("userId", userId)
                        .addValue("startTime", startTime)
                        .addValue("endTime", endTime));
    }

    @Override
    public long countFailedLoginAttempts(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        return count(
                " AND user_id = :userId AND login_status = :loginStatus AND login_time BETWEEN :startTime AND :endTime",
                new MapSqlParameterSource("userId", userId)
                        .addValue("loginStatus", FAILURE)
                        .addValue("startTime", startTime)
                        .addValue("endTime", endTime));
    }

    @Override
    public List<LoginLog> findByUserId(String userId) {
        return jdbc
                .sql("SELECT " + COLUMNS + FROM + " AND user_id = :userId ORDER BY login_time DESC")
                .param("userId", userId)
                .query(LoginLogGatewayImpl::map)
                .list()
                .stream()
                .map(converter::toEntity)
                .toList();
    }

    @Override
    public Optional<LoginLog> findLastSuccessfulLogin(String userId) {
        return jdbc.sql("SELECT " + COLUMNS + FROM
                        + " AND user_id = :userId AND login_status = :loginStatus ORDER BY login_time DESC LIMIT 1")
                .param("userId", userId)
                .param("loginStatus", SUCCESS)
                .query(LoginLogGatewayImpl::map)
                .optional()
                .map(converter::toEntity);
    }

    private PageResult<LoginLog> page(String predicate, MapSqlParameterSource params, PageQuery query) {
        long total = count(predicate, params);
        params.addValue("limit", query.pageSize()).addValue("offset", ((long) query.pageNum() - 1) * query.pageSize());
        List<LoginLog> items = jdbc
                .sql("SELECT " + COLUMNS + FROM + predicate + " ORDER BY login_time DESC LIMIT :limit OFFSET :offset")
                .paramSource(params)
                .query(LoginLogGatewayImpl::map)
                .list()
                .stream()
                .map(converter::toEntity)
                .toList();
        return PageResult.of(items, total, query.pageNum(), query.pageSize());
    }

    private long count(String predicate, MapSqlParameterSource params) {
        Long count = jdbc.sql("SELECT COUNT(*)" + FROM + predicate)
                .paramSource(params)
                .query(Long.class)
                .single();
        return count == null ? 0 : count;
    }
}
