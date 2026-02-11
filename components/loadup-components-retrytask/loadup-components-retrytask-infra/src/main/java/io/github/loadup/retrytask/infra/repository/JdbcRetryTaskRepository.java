package io.github.loadup.retrytask.infra.repository;

import io.github.loadup.retrytask.facade.enums.DbType;
import io.github.loadup.retrytask.facade.enums.RetryTaskStatus;
import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.infra.converter.RetryTaskDOConverter;
import io.github.loadup.retrytask.infra.model.RetryTaskDO;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JDBC implementation of {@link RetryTaskRepository}.
 */
public class JdbcRetryTaskRepository implements RetryTaskRepository {

    private final JdbcTemplate jdbcTemplate;
    private final String tableName;
    private final DbType dbType;
    private final RowMapper<RetryTaskDO> rowMapper;
    private final Map<String, String> sqlMap;

    public JdbcRetryTaskRepository(JdbcTemplate jdbcTemplate, String tablePrefix, DbType dbType) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = (tablePrefix == null ? "" : tablePrefix) + "retry_task";
        this.dbType = (dbType == null) ? DbType.MYSQL : dbType;
        this.rowMapper = new BeanPropertyRowMapper<>(RetryTaskDO.class);
        this.sqlMap = new HashMap<>();
        initSqlMap();
    }

    private void initSqlMap() {
        String nowFunc = "NOW()";
        if (DbType.ORACLE == dbType) {
            nowFunc = "CURRENT_TIMESTAMP";
        }

        // INSERT
        sqlMap.put("INSERT", String.format("INSERT INTO %s (biz_type, biz_id, retry_count, max_retry_count, next_retry_time, status, priority, last_failure_reason, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", tableName));

        // UPDATE
        sqlMap.put("UPDATE", String.format("UPDATE %s SET biz_type=?, biz_id=?, retry_count=?, max_retry_count=?, next_retry_time=?, status=?, priority=?, last_failure_reason=?, create_time=?, update_time=? WHERE id=?", tableName));

        // SELECT_BY_ID
        sqlMap.put("SELECT_BY_ID", String.format("SELECT * FROM %s WHERE id = ?", tableName));

        // SELECT_BY_BIZ
        sqlMap.put("SELECT_BY_BIZ", String.format("SELECT * FROM %s WHERE biz_type = ? AND biz_id = ?", tableName));

        // DELETE
        sqlMap.put("DELETE", String.format("DELETE FROM %s WHERE biz_type = ? AND biz_id = ?", tableName));

        // DELETE_BY_ID
        sqlMap.put("DELETE_BY_ID", String.format("DELETE FROM %s WHERE id = ?", tableName));

        // FIND_TASKS_TO_RETRY
        if (DbType.ORACLE == dbType) {
            sqlMap.put("FIND_TASKS_TO_RETRY", String.format("SELECT * FROM %s WHERE next_retry_time < ? AND status = ? ORDER BY priority DESC, next_retry_time ASC OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY", tableName));
        } else {
            // MySQL, PgSQL
            sqlMap.put("FIND_TASKS_TO_RETRY", String.format("SELECT * FROM %s WHERE next_retry_time < ? AND status = ? ORDER BY priority DESC, next_retry_time ASC LIMIT ?", tableName));
        }

        // FIND_TASKS_TO_RETRY_BY_BIZ_TYPE
        if (DbType.ORACLE == dbType) {
             sqlMap.put("FIND_TASKS_TO_RETRY_BY_BIZ_TYPE", String.format("SELECT * FROM %s WHERE biz_type = ? AND next_retry_time < ? AND status = ? ORDER BY priority DESC, next_retry_time ASC OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY", tableName));
        } else {
             // MySQL, PgSQL
             sqlMap.put("FIND_TASKS_TO_RETRY_BY_BIZ_TYPE", String.format("SELECT * FROM %s WHERE biz_type = ? AND next_retry_time < ? AND status = ? ORDER BY priority DESC, next_retry_time ASC LIMIT ?", tableName));
        }

        // TRY_LOCK
        sqlMap.put("TRY_LOCK", String.format("UPDATE %s SET status = 'RUNNING', update_time = %s WHERE id = ? AND status = 'PENDING'", tableName, nowFunc));

        // UPDATE_STATUS
        sqlMap.put("UPDATE_STATUS", String.format("UPDATE %s SET status = ?, update_time = %s WHERE id = ?", tableName, nowFunc));

        // RESET_STUCK_TASKS
        sqlMap.put("RESET_STUCK_TASKS", String.format("UPDATE %s SET status = 'PENDING', update_time = %s WHERE status = 'RUNNING' AND update_time < ?", tableName, nowFunc));
    }

    @Override
    public RetryTask save(RetryTask task) {
        RetryTaskDO dbo = RetryTaskDOConverter.toDbo(task);
        RetryTaskDO savedDbo;
        if (dbo.getId() == null) {
            savedDbo = insert(dbo);
        } else {
            savedDbo = update(dbo);
        }
        return RetryTaskDOConverter.toDomain(savedDbo);
    }

    private RetryTaskDO insert(RetryTaskDO entity) {
        String sql = sqlMap.get("INSERT");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps;
            if (DbType.ORACLE == dbType) {
                ps = connection.prepareStatement(sql, new String[]{"id"});
            } else {
                ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            }

            ps.setString(1, entity.getBizType());
            ps.setString(2, entity.getBizId());
            ps.setInt(3, entity.getRetryCount());
            ps.setInt(4, entity.getMaxRetryCount());
            ps.setTimestamp(5, Timestamp.valueOf(entity.getNextRetryTime()));
            ps.setString(6, entity.getStatus());
            ps.setString(7, entity.getPriority());
            ps.setString(8, entity.getLastFailureReason());
            ps.setTimestamp(9, Timestamp.valueOf(entity.getCreateTime()));
            ps.setTimestamp(10, Timestamp.valueOf(entity.getUpdateTime()));
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            entity.setId(keyHolder.getKey().longValue());
        }
        return entity;
    }

    private RetryTaskDO update(RetryTaskDO entity) {
        String sql = sqlMap.get("UPDATE");
        jdbcTemplate.update(sql,
            entity.getBizType(),
            entity.getBizId(),
            entity.getRetryCount(),
            entity.getMaxRetryCount(),
            entity.getNextRetryTime(),
            entity.getStatus(),
            entity.getPriority(),
            entity.getLastFailureReason(),
            entity.getCreateTime(),
            entity.getUpdateTime(),
            entity.getId());
        return entity;
    }

    @Override
    public Optional<RetryTask> findById(Long id) {
        String sql = sqlMap.get("SELECT_BY_ID");
        try {
            RetryTaskDO result = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(result).map(RetryTaskDOConverter::toDomain);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<RetryTask> findByBizTypeAndBizId(String bizType, String bizId) {
        String sql = sqlMap.get("SELECT_BY_BIZ");
        try {
            RetryTaskDO result = jdbcTemplate.queryForObject(sql, rowMapper, bizType, bizId);
            return Optional.ofNullable(result).map(RetryTaskDOConverter::toDomain);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(String bizType, String bizId) {
        String sql = sqlMap.get("DELETE");
        jdbcTemplate.update(sql, bizType, bizId);
    }

    @Override
    public void deleteById(Long id) {
        String sql = sqlMap.get("DELETE_BY_ID");
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<RetryTask> findTasksToRetry(LocalDateTime nextRetryTime, int limit) {
        String sql = sqlMap.get("FIND_TASKS_TO_RETRY");
        List<RetryTaskDO> list = jdbcTemplate.query(sql, rowMapper, nextRetryTime, RetryTaskStatus.PENDING.name(), limit);
        return list.stream().map(RetryTaskDOConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<RetryTask> findTasksToRetryByBizType(String bizType, LocalDateTime nextRetryTime, int limit) {
        String sql = sqlMap.get("FIND_TASKS_TO_RETRY_BY_BIZ_TYPE");
        List<RetryTaskDO> list = jdbcTemplate.query(sql, rowMapper, bizType, nextRetryTime, RetryTaskStatus.PENDING.name(), limit);
        return list.stream().map(RetryTaskDOConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean tryLock(Long id) {
        String sql = sqlMap.get("TRY_LOCK");
        int rows = jdbcTemplate.update(sql, id);
        return rows > 0;
    }

    @Override
    public void updateStatus(Long id, RetryTaskStatus status) {
        String sql = sqlMap.get("UPDATE_STATUS");
        jdbcTemplate.update(sql, status.name(), id);
    }

    @Override
    public int resetStuckTasks(LocalDateTime deadTime) {
        String sql = sqlMap.get("RESET_STUCK_TASKS");
        return jdbcTemplate.update(sql, deadTime);
    }
}

