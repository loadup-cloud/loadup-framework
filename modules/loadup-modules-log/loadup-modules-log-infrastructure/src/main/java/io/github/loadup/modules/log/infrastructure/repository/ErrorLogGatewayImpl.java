package io.github.loadup.modules.log.infrastructure.repository;

import io.github.loadup.modules.log.domain.gateway.ErrorLogGateway;
import io.github.loadup.modules.log.domain.model.ErrorLog;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
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
        String id = log.getId() != null ? log.getId() : UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
                "INSERT INTO error_log (id, user_id, error_type, error_code, error_message,"
                + " stack_trace, request_url, request_method, request_params, ip, error_time,"
                + " created_at, updated_at, deleted)"
                + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,0)",
                id, log.getUserId(), log.getErrorType(), log.getErrorCode(),
                log.getErrorMessage(), log.getStackTrace(), log.getRequestUrl(),
                log.getRequestMethod(), log.getRequestParams(), log.getIp(),
                log.getErrorTime() != null ? log.getErrorTime() : now,
                log.getCreatedAt() != null ? log.getCreatedAt() : now,
                now);
    }

    @Override
    public List<ErrorLog> findByCondition(String userId, String errorType, String errorCode,
            LocalDateTime startTime, LocalDateTime endTime, int pageNum, int pageSize) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, errorType, errorCode, startTime, endTime, params);
        params.add(pageSize);
        params.add((pageNum - 1) * pageSize);
        return jdbcTemplate.query(
                "SELECT * FROM error_log WHERE deleted = 0" + where
                + " ORDER BY error_time DESC LIMIT ? OFFSET ?",
                ROW_MAPPER, params.toArray());
    }

    @Override
    public long countByCondition(String userId, String errorType, String errorCode,
            LocalDateTime startTime, LocalDateTime endTime) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, errorType, errorCode, startTime, endTime, params);
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM error_log WHERE deleted = 0" + where,
                Long.class, params.toArray());
        return count == null ? 0L : count;
    }

    private String buildWhere(String userId, String errorType, String errorCode,
            LocalDateTime startTime, LocalDateTime endTime, List<Object> params) {
        StringBuilder sb = new StringBuilder();
        if (userId != null)    { sb.append(" AND user_id = ?");     params.add(userId); }
        if (errorType != null) { sb.append(" AND error_type = ?");  params.add(errorType); }
        if (errorCode != null) { sb.append(" AND error_code = ?");  params.add(errorCode); }
        if (startTime != null) { sb.append(" AND error_time >= ?"); params.add(startTime); }
        if (endTime != null)   { sb.append(" AND error_time <= ?"); params.add(endTime); }
        return sb.toString();
    }
}

