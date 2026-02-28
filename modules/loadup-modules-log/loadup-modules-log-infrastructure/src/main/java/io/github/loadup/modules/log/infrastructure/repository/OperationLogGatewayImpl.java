package io.github.loadup.modules.log.infrastructure.repository;

/*-
 * #%L
 * Loadup Modules Log Infrastructure
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static io.github.loadup.modules.log.infrastructure.dataobject.table.Tables.OPERATION_LOG_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.modules.log.domain.gateway.OperationLogGateway;
import io.github.loadup.modules.log.domain.model.OperationLog;
import io.github.loadup.modules.log.infrastructure.mapper.OperationLogDOMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OperationLogGatewayImpl implements OperationLogGateway {

    private static final int EXPORT_MAX = 10_000;

    private final OperationLogDOMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(OperationLog log) {
        var entity = toEntity(log);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        mapper.insert(entity);
    }

    @Override
    public List<OperationLog> findByCondition(
            String userId,
            String module,
            String operationType,
            Boolean success,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int pageNum,
            int pageSize) {
        QueryWrapper qw = buildQuery(userId, module, operationType, success, startTime, endTime);
        qw.orderBy(OPERATION_LOG_DO.OPERATION_TIME.desc());
        qw.limit((long) (pageNum - 1) * pageSize, pageSize);
        return mapper.selectListByQuery(qw).stream().map(this::toModel).toList();
    }

    @Override
    public long countByCondition(
            String userId,
            String module,
            String operationType,
            Boolean success,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        return mapper.selectCountByQuery(buildQuery(userId, module, operationType, success, startTime, endTime));
    }

    @Override
    public List<OperationLog> findAllForExport(
            String userId,
            String module,
            String operationType,
            Boolean success,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        QueryWrapper qw = buildQuery(userId, module, operationType, success, startTime, endTime);
        qw.orderBy(OPERATION_LOG_DO.OPERATION_TIME.desc());
        qw.limit(EXPORT_MAX);
        return mapper.selectListByQuery(qw).stream().map(this::toModel).toList();
    }

    @Override
    public Map<String, Long> countGroupBy(
            String groupField,
            String userId,
            String module,
            String operationType,
            Boolean success,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        String col = resolveGroupColumnName(groupField);
        StringBuilder sql =
                new StringBuilder("SELECT " + col + " AS grp, COUNT(*) AS cnt FROM operation_log WHERE deleted = 0");
        List<Object> params = new ArrayList<>();
        sql.append(buildWhereSuffix(userId, module, operationType, success, startTime, endTime, params));
        sql.append(" GROUP BY ").append(col).append(" ORDER BY cnt DESC");

        Map<String, Long> result = new LinkedHashMap<>();
        jdbcTemplate.query(
                sql.toString(),
                rs -> {
                    String key = rs.getString("grp");
                    result.put(key == null ? "unknown" : key, rs.getLong("cnt"));
                },
                params.toArray());
        return result;
    }

    @Override
    public Map<String, Object> durationStats(
            String userId, String module, String operationType, LocalDateTime startTime, LocalDateTime endTime) {
        StringBuilder sql = new StringBuilder("SELECT AVG(duration) AS avgDuration, MAX(duration) AS maxDuration"
                + " FROM operation_log WHERE deleted = 0");
        List<Object> params = new ArrayList<>();
        sql.append(buildWhereSuffix(userId, module, operationType, null, startTime, endTime, params));

        Map<String, Object> stats = new LinkedHashMap<>();
        jdbcTemplate.query(
                sql.toString(),
                rs -> {
                    stats.put("avgDuration", rs.getObject("avgDuration"));
                    stats.put("maxDuration", rs.getObject("maxDuration"));
                },
                params.toArray());
        return stats;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private QueryWrapper buildQuery(
            String userId,
            String module,
            String operationType,
            Boolean success,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        QueryWrapper qw = QueryWrapper.create();
        if (userId != null) qw.and(OPERATION_LOG_DO.USER_ID.eq(userId));
        if (module != null) qw.and(OPERATION_LOG_DO.MODULE.eq(module));
        if (operationType != null) qw.and(OPERATION_LOG_DO.OPERATION_TYPE.eq(operationType));
        if (success != null) qw.and(OPERATION_LOG_DO.SUCCESS.eq(success));
        if (startTime != null) qw.and(OPERATION_LOG_DO.OPERATION_TIME.ge(startTime));
        if (endTime != null) qw.and(OPERATION_LOG_DO.OPERATION_TIME.le(endTime));
        return qw;
    }

    private String buildWhereSuffix(
            String userId,
            String module,
            String operationType,
            Boolean success,
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<Object> params) {
        StringBuilder sb = new StringBuilder();
        if (userId != null) {
            sb.append(" AND user_id = ?");
            params.add(userId);
        }
        if (module != null) {
            sb.append(" AND module = ?");
            params.add(module);
        }
        if (operationType != null) {
            sb.append(" AND operation_type = ?");
            params.add(operationType);
        }
        if (success != null) {
            sb.append(" AND success = ?");
            params.add(success);
        }
        if (startTime != null) {
            sb.append(" AND operation_time >= ?");
            params.add(startTime);
        }
        if (endTime != null) {
            sb.append(" AND operation_time <= ?");
            params.add(endTime);
        }
        return sb.toString();
    }

    private String resolveGroupColumnName(String groupField) {
        return switch (groupField) {
            case "operation_type" -> "operation_type";
            case "date" -> "DATE(operation_time)";
            default -> "module";
        };
    }

    private io.github.loadup.modules.log.infrastructure.dataobject.OperationLogDO toEntity(OperationLog m) {
        var e = new io.github.loadup.modules.log.infrastructure.dataobject.OperationLogDO();
        e.setId(m.getId());
        e.setUserId(m.getUserId());
        e.setUsername(m.getUsername());
        e.setModule(m.getModule());
        e.setOperationType(m.getOperationType());
        e.setDescription(m.getDescription());
        e.setMethod(m.getMethod());
        e.setRequestParams(m.getRequestParams());
        e.setResponseResult(m.getResponseResult());
        e.setDuration(m.getDuration());
        e.setSuccess(m.getSuccess());
        e.setErrorMessage(m.getErrorMessage());
        e.setIp(m.getIp());
        e.setUserAgent(m.getUserAgent());
        e.setOperationTime(m.getOperationTime());
        e.setCreatedAt(m.getCreatedAt());
        return e;
    }

    private OperationLog toModel(io.github.loadup.modules.log.infrastructure.dataobject.OperationLogDO e) {
        return OperationLog.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .username(e.getUsername())
                .module(e.getModule())
                .operationType(e.getOperationType())
                .description(e.getDescription())
                .method(e.getMethod())
                .requestParams(e.getRequestParams())
                .responseResult(e.getResponseResult())
                .duration(e.getDuration())
                .success(e.getSuccess())
                .errorMessage(e.getErrorMessage())
                .ip(e.getIp())
                .userAgent(e.getUserAgent())
                .operationTime(e.getOperationTime())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
