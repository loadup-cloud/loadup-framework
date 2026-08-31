package io.github.loadup.components.globalunique.mapper;

/*-
 * #%L
 * LoadUp Components :: Global Unique
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

import io.github.loadup.commons.enums.DbType;
import io.github.loadup.components.globalunique.entity.GlobalUniqueEntity;
import io.github.loadup.components.globalunique.properties.GlobalUniqueProperties;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * JDBC-backed persistence for the {@code global_unique} idempotency ledger.
 */
public class GlobalUniqueMapper {
    private static final Logger log = LoggerFactory.getLogger(GlobalUniqueMapper.class);

    private final JdbcTemplate jdbcTemplate;
    private final GlobalUniqueProperties properties;

    /** Insert statements per database dialect; the table name is filled in at runtime. */
    private static final Map<DbType, String> INSERT_SQL_MAP = new EnumMap<>(DbType.class);

    static {
        String columns = "id, tenant_id, unique_key, biz_type, biz_id, request_data, created_at, updated_at, deleted";
        String placeholders = "?, ?, ?, ?, ?, ?, ?, ?, ?";
        INSERT_SQL_MAP.put(DbType.MYSQL, "INSERT INTO %s (" + columns + ") VALUES (" + placeholders + ")");
        INSERT_SQL_MAP.put(DbType.POSTGRESQL, "INSERT INTO %s (" + columns + ") VALUES (" + placeholders + ")");
        INSERT_SQL_MAP.put(DbType.ORACLE, "INSERT INTO %s (" + columns + ") VALUES (" + placeholders + ")");
    }

    /**
     * Inserts the entity, generating the id and timestamps when absent.
     *
     * @param entity the entity to insert
     * @return {@code 1} when the row was inserted
     * @throws DuplicateKeyException when the unique key already exists
     */
    public int insert(GlobalUniqueEntity entity) {
        String sql = getInsertSql();
        LocalDateTime now = LocalDateTime.now();

        if (entity.getId() == null || entity.getId().isEmpty()) {
            entity.setId(generateId());
        }
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        if (entity.isDeleted() == null) {
            entity.setDeleted(false);
        }

        try {
            return jdbcTemplate.update(
                    sql,
                    entity.getId(),
                    entity.getTenantId(),
                    entity.getUniqueKey(),
                    entity.getBizType(),
                    entity.getBizId(),
                    entity.getRequestData(),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt(),
                    entity.isDeleted() ? 1 : 0);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Normalize any dialect-specific unique-key violation into DuplicateKeyException.
            if (e.getMessage() != null && e.getMessage().contains("unique_key")) {
                throw new DuplicateKeyException("Duplicate unique_key: " + entity.getUniqueKey(), e);
            }
            throw e;
        }
    }

    /**
     * Finds the record by unique key.
     *
     * @param uniqueKey the unique key
     * @return the entity, or {@code null} when not found
     */
    public GlobalUniqueEntity findByUniqueKey(String uniqueKey) {
        String sql = String.format(
                "SELECT id, tenant_id, unique_key, biz_type, biz_id, request_data, created_at, updated_at, deleted "
                        + "FROM %s WHERE unique_key = ?",
                getTableName());

        try {
            return jdbcTemplate.queryForObject(
                    sql,
                    (rs, rowNum) -> GlobalUniqueEntity.builder()
                            .id(rs.getString("id"))
                            .tenantId(rs.getString("tenant_id"))
                            .uniqueKey(rs.getString("unique_key"))
                            .bizType(rs.getString("biz_type"))
                            .bizId(rs.getString("biz_id"))
                            .requestData(rs.getString("request_data"))
                            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                            .deleted(rs.getInt("deleted") == 1)
                            .build(),
                    uniqueKey);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Resolves the insert SQL for the configured database dialect.
     */
    private String getInsertSql() {
        String sqlTemplate = INSERT_SQL_MAP.get(properties.getDbType());
        if (sqlTemplate == null) {
            throw new IllegalArgumentException("Unsupported database type: " + properties.getDbType());
        }
        return String.format(sqlTemplate, getTableName());
    }

    /**
     * Returns the prefixed table name after validating it against SQL injection.
     */
    private String getTableName() {
        String tableName = properties.getFullTableName();
        if (tableName == null || !tableName.matches("[a-zA-Z_][a-zA-Z0-9_.]*")) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }
        return tableName;
    }

    /**
     * Generates a compact UUID without dashes.
     */
    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public GlobalUniqueMapper(JdbcTemplate jdbcTemplate, GlobalUniqueProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }
}
