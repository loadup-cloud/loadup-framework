/*
 * #%L
 * LoadUp Dictionary Infrastructure
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
package io.github.loadup.modules.dictionary.infrastructure.repository;

import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.modules.dictionary.client.error.DictionaryResultCodeEnum;
import io.github.loadup.modules.dictionary.domain.gateway.DictionaryGateway;
import io.github.loadup.modules.dictionary.domain.model.*;
import io.github.loadup.modules.dictionary.infrastructure.converter.DictionaryStorageConverter;
import io.github.loadup.modules.dictionary.infrastructure.dataobject.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

public class DictionaryGatewayImpl implements DictionaryGateway {
    private static final String TYPE_COLUMNS =
            "id, tenant_id, type_code AS code, type_name AS name, description, enabled";
    private static final String ITEM_COLUMNS =
            "id, tenant_id, type_id, item_value AS value, item_label AS label, description, sort_order, enabled";
    private final NamedParameterJdbcTemplate jdbc;
    private final IdGenerator ids;
    private final DictionaryStorageConverter converter;
    private final Clock clock;

    public DictionaryGatewayImpl(
            NamedParameterJdbcTemplate jdbc, IdGenerator ids, DictionaryStorageConverter converter, Clock clock) {
        this.jdbc = jdbc;
        this.ids = ids;
        this.converter = converter;
        this.clock = clock;
    }

    @Override
    public Optional<DictionaryType> findTypeById(String tenant, String id) {
        var args = new MapSqlParameterSource("tenant", tenant).addValue("id", id);
        return jdbc
                .query(
                        "SELECT " + TYPE_COLUMNS
                                + " FROM dictionary_type WHERE tenant_id = :tenant AND id = :id AND deleted = FALSE",
                        args,
                        BeanPropertyRowMapper.newInstance(DictionaryTypeDO.class))
                .stream()
                .findFirst()
                .map(converter::toDomain);
    }

    @Override
    public Optional<DictionaryType> findTypeByCode(String tenant, String code) {
        var args = new MapSqlParameterSource("tenant", tenant).addValue("code", code);
        return jdbc
                .query(
                        "SELECT " + TYPE_COLUMNS
                                + " FROM dictionary_type WHERE tenant_id = :tenant AND type_code = :code AND deleted = FALSE",
                        args,
                        BeanPropertyRowMapper.newInstance(DictionaryTypeDO.class))
                .stream()
                .findFirst()
                .map(converter::toDomain);
    }

    @Override
    public void insertType(DictionaryType type) {
        var row = converter.toDO(type);
        if (row.getId() == null || row.getId().isBlank()) row.setId(ids.generate());
        var now = LocalDateTime.now(clock);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        jdbc.update("""
                INSERT INTO dictionary_type (id, tenant_id, type_code, type_name, description, enabled, created_at, updated_at, deleted)
                VALUES (:id, :tenantId, :code, :name, :description, :enabled, :createdAt, :updatedAt, FALSE)
                """, new BeanPropertySqlParameterSource(row));
    }

    @Override
    public void updateType(DictionaryType type) {
        var row = converter.toDO(type);
        row.setUpdatedAt(LocalDateTime.now(clock));
        int changed = jdbc.update(
                "UPDATE dictionary_type SET type_name = :name, description = :description, enabled = :enabled, updated_at = :updatedAt "
                        + "WHERE tenant_id = :tenantId AND id = :id AND deleted = FALSE",
                new BeanPropertySqlParameterSource(row));
        if (changed != 1) throw new CommonException(DictionaryResultCodeEnum.DICTIONARY_TYPE_NOT_FOUND);
    }

    @Override
    @Transactional
    public void deleteType(String tenant, String id) {
        lockType(tenant, id);
        if (countItems(tenant, id) != 0) throw new CommonException(DictionaryResultCodeEnum.DICTIONARY_TYPE_NOT_EMPTY);
        jdbc.update(
                "DELETE FROM dictionary_type WHERE tenant_id = :tenant AND id = :id",
                new MapSqlParameterSource("tenant", tenant).addValue("id", id));
    }

    @Override
    public PageResult<DictionaryType> listTypes(String tenant, int page, int size) {
        var args = new MapSqlParameterSource("tenant", tenant)
                .addValue("limit", size)
                .addValue("offset", (long) (page - 1) * size);
        long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM dictionary_type WHERE tenant_id = :tenant AND deleted = FALSE", args, Long.class);
        var rows = jdbc.query(
                "SELECT " + TYPE_COLUMNS
                        + " FROM dictionary_type WHERE tenant_id = :tenant AND deleted = FALSE ORDER BY type_code ASC LIMIT :limit OFFSET :offset",
                args,
                BeanPropertyRowMapper.newInstance(DictionaryTypeDO.class));
        return new PageResult<>(rows.stream().map(converter::toDomain).toList(), total, page, size);
    }

    @Override
    public Optional<DictionaryItem> findItemById(String tenant, String id) {
        var args = new MapSqlParameterSource("tenant", tenant).addValue("id", id);
        return jdbc
                .query(
                        "SELECT " + ITEM_COLUMNS
                                + " FROM dictionary_item WHERE tenant_id = :tenant AND id = :id AND deleted = FALSE",
                        args,
                        BeanPropertyRowMapper.newInstance(DictionaryItemDO.class))
                .stream()
                .findFirst()
                .map(converter::toDomain);
    }

    @Override
    @Transactional
    public void insertItem(DictionaryItem item) {
        lockType(item.tenantId(), item.typeId());
        var row = converter.toDO(item);
        if (row.getId() == null || row.getId().isBlank()) row.setId(ids.generate());
        var now = LocalDateTime.now(clock);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        jdbc.update("""
                INSERT INTO dictionary_item (id, tenant_id, type_id, item_value, item_label, description, sort_order, enabled, created_at, updated_at, deleted)
                VALUES (:id, :tenantId, :typeId, :value, :label, :description, :sortOrder, :enabled, :createdAt, :updatedAt, FALSE)
                """, new BeanPropertySqlParameterSource(row));
    }

    @Override
    public void updateItem(DictionaryItem item) {
        var row = converter.toDO(item);
        row.setUpdatedAt(LocalDateTime.now(clock));
        int changed = jdbc.update(
                "UPDATE dictionary_item SET item_label = :label, description = :description, sort_order = :sortOrder, enabled = :enabled, updated_at = :updatedAt "
                        + "WHERE tenant_id = :tenantId AND id = :id AND deleted = FALSE",
                new BeanPropertySqlParameterSource(row));
        if (changed != 1) throw new CommonException(DictionaryResultCodeEnum.DICTIONARY_ITEM_NOT_FOUND);
    }

    @Override
    public void deleteItem(String tenant, String id) {
        jdbc.update(
                "DELETE FROM dictionary_item WHERE tenant_id = :tenant AND id = :id",
                new MapSqlParameterSource("tenant", tenant).addValue("id", id));
    }

    private void lockType(String tenant, String id) {
        var args = new MapSqlParameterSource("tenant", tenant).addValue("id", id);
        if (jdbc.queryForList(
                        "SELECT id FROM dictionary_type WHERE tenant_id = :tenant AND id = :id AND deleted = FALSE FOR UPDATE",
                        args,
                        String.class)
                .isEmpty()) {
            throw new CommonException(DictionaryResultCodeEnum.DICTIONARY_TYPE_NOT_FOUND);
        }
    }

    @Override
    public long countItems(String tenant, String type) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM dictionary_item WHERE tenant_id = :tenant AND type_id = :type AND deleted = FALSE",
                new MapSqlParameterSource("tenant", tenant).addValue("type", type),
                Long.class);
    }

    @Override
    public PageResult<DictionaryItem> listItems(String tenant, String type, int page, int size) {
        var args = new MapSqlParameterSource("tenant", tenant)
                .addValue("type", type)
                .addValue("limit", size)
                .addValue("offset", (long) (page - 1) * size);
        var rows = jdbc.query(
                "SELECT " + ITEM_COLUMNS
                        + " FROM dictionary_item WHERE tenant_id = :tenant AND type_id = :type AND deleted = FALSE "
                        + "ORDER BY sort_order ASC, item_label ASC, item_value ASC LIMIT :limit OFFSET :offset",
                args,
                BeanPropertyRowMapper.newInstance(DictionaryItemDO.class));
        return new PageResult<>(rows.stream().map(converter::toDomain).toList(), countItems(tenant, type), page, size);
    }

    @Override
    public List<DictionaryItem> listEnabledItems(String tenant, String type) {
        var args = new MapSqlParameterSource("tenant", tenant).addValue("type", type);
        return jdbc
                .query(
                        "SELECT " + ITEM_COLUMNS
                                + " FROM dictionary_item WHERE tenant_id = :tenant AND type_id = :type AND enabled = TRUE AND deleted = FALSE "
                                + "ORDER BY sort_order ASC, item_label ASC, item_value ASC",
                        args,
                        BeanPropertyRowMapper.newInstance(DictionaryItemDO.class))
                .stream()
                .map(converter::toDomain)
                .toList();
    }
}
