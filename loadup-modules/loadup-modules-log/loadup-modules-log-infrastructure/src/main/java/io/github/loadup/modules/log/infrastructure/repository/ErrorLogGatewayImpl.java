package io.github.loadup.modules.log.infrastructure.repository;

/*-
 * #%L
 * Loadup Modules Log Infrastructure
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.loadup.modules.log.domain.gateway.ErrorLogGateway;
import io.github.loadup.modules.log.domain.model.ErrorLog;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ErrorLogGatewayImpl implements ErrorLogGateway {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<ErrorLog> ROW_MAPPER = (rs, rowNum) -> ErrorLog.builder()
            .id(rs.getString("id"))
            .userId(rs.getString("user_id"))
            .errorType(rs.getString("error_type"))
            .errorCode(rs.getString("error_code"))
            .errorMessage(rs.getString("error_message"))
            .stackTrace(rs.getString("stack_trace"))
            .requestUrl(rs.getString("request_url"))
            .requestMethod(rs.getString("request_method"))
            .requestParams(rs.getString("request_params"))
            .ip(rs.getString("ip"))
            .errorTime(rs.getObject("error_time", LocalDateTime.class))
            .createdAt(rs.getObject("created_at", LocalDateTime.class))
            .build();

    @Override
    public void save(ErrorLog log) {
        String id =
                log.getId() != null ? log.getId() : UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
                "INSERT INTO error_log (id, user_id, error_type, error_code, error_message,"
                        + " stack_trace, request_url, request_method, request_params, ip, error_time,"
                        + " created_at, updated_at, deleted)"
                        + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,0)",
                id,
                log.getUserId(),
                log.getErrorType(),
                log.getErrorCode(),
                log.getErrorMessage(),
                log.getStackTrace(),
                log.getRequestUrl(),
                log.getRequestMethod(),
                log.getRequestParams(),
                log.getIp(),
                log.getErrorTime() != null ? log.getErrorTime() : now,
                log.getCreatedAt() != null ? log.getCreatedAt() : now,
                now);
    }

    @Override
    @SuppressFBWarnings(
            value = "SQL_INJECTION_SPRING_JDBC",
            justification = "Dynamic WHERE uses only bound '?' parameters; no user input is concatenated into the"
                    + " statement.")
    public List<ErrorLog> findByCondition(
            String userId,
            String errorType,
            String errorCode,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int pageNum,
            int pageSize) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, errorType, errorCode, startTime, endTime, params);
        params.add(pageSize);
        params.add((pageNum - 1) * pageSize);
        return jdbcTemplate.query(
                "SELECT * FROM error_log WHERE deleted = 0" + where + " ORDER BY error_time DESC LIMIT ? OFFSET ?",
                ROW_MAPPER,
                params.toArray());
    }

    @Override
    @SuppressFBWarnings(
            value = "SQL_INJECTION_SPRING_JDBC",
            justification = "Dynamic WHERE uses only bound '?' parameters; no user input is concatenated into the"
                    + " statement.")
    public long countByCondition(
            String userId, String errorType, String errorCode, LocalDateTime startTime, LocalDateTime endTime) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, errorType, errorCode, startTime, endTime, params);
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM error_log WHERE deleted = 0" + where, Long.class, params.toArray());
        return count == null ? 0L : count;
    }

    private String buildWhere(
            String userId,
            String errorType,
            String errorCode,
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<Object> params) {
        StringBuilder sb = new StringBuilder();
        if (userId != null) {
            sb.append(" AND user_id = ?");
            params.add(userId);
        }
        if (errorType != null) {
            sb.append(" AND error_type = ?");
            params.add(errorType);
        }
        if (errorCode != null) {
            sb.append(" AND error_code = ?");
            params.add(errorCode);
        }
        if (startTime != null) {
            sb.append(" AND error_time >= ?");
            params.add(startTime);
        }
        if (endTime != null) {
            sb.append(" AND error_time <= ?");
            params.add(endTime);
        }
        return sb.toString();
    }

    public ErrorLogGatewayImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
