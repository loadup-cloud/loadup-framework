package io.github.loadup.components.globalunique.mapper;

import io.github.loadup.components.globalunique.entity.GlobalUniqueEntity;
import io.github.loadup.components.globalunique.enums.DbType;
import io.github.loadup.components.globalunique.properties.GlobalUniqueProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GlobalUnique Mapper 基于 JdbcTemplate
 *
 * @author loadup
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class GlobalUniqueMapper {

    private final JdbcTemplate jdbcTemplate;
    private final GlobalUniqueProperties properties;

    /**
     * SQL 语句映射
     */
    private static final Map<DbType, String> INSERT_SQL_MAP = new HashMap<>();

    static {
        // MySQL
        INSERT_SQL_MAP.put(DbType.MYSQL,
                "INSERT INTO %s (id, unique_key, biz_type, biz_id, request_data, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)");

        // PostgreSQL
        INSERT_SQL_MAP.put(DbType.POSTGRESQL,
                "INSERT INTO %s (id, unique_key, biz_type, biz_id, request_data, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)");

        // Oracle
        INSERT_SQL_MAP.put(DbType.ORACLE,
                "INSERT INTO %s (id, unique_key, biz_type, biz_id, request_data, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)");
    }

    /**
     * 插入记录
     *
     * @param entity 实体对象
     * @return 插入成功返回 1，失败返回 0
     * @throws DuplicateKeyException 唯一键冲突时抛出
     */
    public int insert(GlobalUniqueEntity entity) throws DuplicateKeyException {
        String sql = getInsertSql();
        LocalDateTime now = LocalDateTime.now();

        // 如果 ID 为空，自动生成
        if (entity.getId() == null || entity.getId().isEmpty()) {
            entity.setId(generateId());
        }

        // 设置时间戳
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        try {
            return jdbcTemplate.update(sql,
                    entity.getId(),
                    entity.getUniqueKey(),
                    entity.getBizType(),
                    entity.getBizId(),
                    entity.getRequestData(),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt()
            );
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 转换为 DuplicateKeyException
            if (e.getMessage() != null && e.getMessage().contains("unique_key")) {
                throw new DuplicateKeyException("Duplicate unique_key: " + entity.getUniqueKey(), e);
            }
            throw e;
        }
    }

    /**
     * 根据唯一键查询
     *
     * @param uniqueKey 唯一键
     * @return 实体对象，不存在返回 null
     */
    public GlobalUniqueEntity findByUniqueKey(String uniqueKey) {
        String sql = String.format(
                "SELECT id, unique_key, biz_type, biz_id, request_data, created_at, updated_at FROM %s WHERE unique_key = ?",
                getTableName()
        );

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> GlobalUniqueEntity.builder()
                    .id(rs.getString("id"))
                    .uniqueKey(rs.getString("unique_key"))
                    .bizType(rs.getString("biz_type"))
                    .bizId(rs.getString("biz_id"))
                    .requestData(rs.getString("request_data"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                    .build(), uniqueKey);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 获取插入 SQL
     */
    private String getInsertSql() {
        String sqlTemplate = INSERT_SQL_MAP.get(properties.getDbType());
        if (sqlTemplate == null) {
            throw new IllegalArgumentException("Unsupported database type: " + properties.getDbType());
        }
        return String.format(sqlTemplate, getTableName());
    }

    /**
     * 获取表名（带前缀）
     */
    private String getTableName() {
        return properties.getFullTableName();
    }

    /**
     * 生成 ID（UUID 去掉横线）
     */
    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

