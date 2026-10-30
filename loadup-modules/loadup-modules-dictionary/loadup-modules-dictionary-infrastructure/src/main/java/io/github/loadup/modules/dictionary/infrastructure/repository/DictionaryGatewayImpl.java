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

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.modules.dictionary.client.error.DictionaryResultCodeEnum;
import io.github.loadup.modules.dictionary.domain.gateway.DictionaryGateway;
import io.github.loadup.modules.dictionary.domain.model.DictionaryItem;
import io.github.loadup.modules.dictionary.domain.model.DictionaryType;
import io.github.loadup.modules.dictionary.infrastructure.converter.DictionaryStorageConverter;
import io.github.loadup.modules.dictionary.infrastructure.dataobject.DictionaryTypeDO;
import io.github.loadup.modules.dictionary.infrastructure.mapper.DictionaryItemMapper;
import io.github.loadup.modules.dictionary.infrastructure.mapper.DictionaryTypeMapper;
import io.github.loadup.modules.dictionary.infrastructure.table.Tables;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

public class DictionaryGatewayImpl extends ServiceImpl<DictionaryTypeMapper, DictionaryTypeDO>
        implements DictionaryGateway {
    private final NamedParameterJdbcTemplate jdbc;
    private final DictionaryItemMapper itemMapper;
    private final IdGenerator ids;
    private final DictionaryStorageConverter converter;
    private final Clock clock;

    public DictionaryGatewayImpl(
            NamedParameterJdbcTemplate jdbc,
            DictionaryTypeMapper mapper,
            DictionaryItemMapper itemMapper,
            IdGenerator ids,
            DictionaryStorageConverter converter,
            Clock clock) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.itemMapper = itemMapper;
        this.ids = ids;
        this.converter = converter;
        this.clock = clock;
    }

    @Override
    public Optional<DictionaryType> findTypeById(String id) {
        return getByIdOpt(id).map(converter::toDomain);
    }

    @Override
    public Optional<DictionaryType> findTypeByCode(String code) {
        return getOneOpt(QueryWrapper.create().where(Tables.DICTIONARY_TYPE.CODE.eq(code)))
                .map(converter::toDomain);
    }

    @Override
    public void insertType(DictionaryType type) {
        var row = converter.toDO(type);
        if (row.getId() == null || row.getId().isBlank()) {
            row.setId(ids.generate());
        }
        var now = LocalDateTime.now(clock);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        jdbc.update("""
            INSERT INTO dictionary_type (id, type_code, type_name, description, enabled, created_at, updated_at, deleted)
            VALUES (:id, :code, :name, :description, :enabled, :createdAt, :updatedAt, FALSE)
            """, new BeanPropertySqlParameterSource(row));
    }

    @Override
    public void updateType(DictionaryType type) {
        var row = converter.toDO(type);
        row.setUpdatedAt(LocalDateTime.now(clock));
        int changed = jdbc.update(
                "UPDATE dictionary_type SET type_name = :name, description = :description, enabled = :enabled, updated_at = :updatedAt "
                        + "WHERE id = :id AND deleted = FALSE",
                new BeanPropertySqlParameterSource(row));
        if (changed != 1) {
            throw new CommonException(DictionaryResultCodeEnum.DICTIONARY_TYPE_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void deleteType(String id) {
        lockType(id);
        if (countItems(id) != 0) {
            throw new CommonException(DictionaryResultCodeEnum.DICTIONARY_TYPE_NOT_EMPTY);
        }
        jdbc.update("DELETE FROM dictionary_type WHERE id = :id", new MapSqlParameterSource("id", id));
    }

    @Override
    public PageResult<DictionaryType> listTypes(int page, int size) {
        var result = page(new Page<>(page, size), QueryWrapper.create().orderBy(Tables.DICTIONARY_TYPE.CODE.asc()));
        return new PageResult<>(
                result.getRecords().stream().map(converter::toDomain).toList(), result.getTotalRow(), page, size);
    }

    @Override
    public Optional<DictionaryItem> findItemById(String id) {
        return Optional.ofNullable(itemMapper.selectOneById(id)).map(converter::toDomain);
    }

    @Override
    @Transactional
    public void insertItem(DictionaryItem item) {
        lockType(item.typeId());
        var row = converter.toDO(item);
        if (row.getId() == null || row.getId().isBlank()) {
            row.setId(ids.generate());
        }
        var now = LocalDateTime.now(clock);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        jdbc.update("""
            INSERT INTO dictionary_item (id, type_id, item_value, item_label, description, sort_order, enabled, created_at, updated_at, deleted)
            VALUES (:id, :typeId, :value, :label, :description, :sortOrder, :enabled, :createdAt, :updatedAt, FALSE)
            """, new BeanPropertySqlParameterSource(row));
    }

    @Override
    public void updateItem(DictionaryItem item) {
        var row = converter.toDO(item);
        row.setUpdatedAt(LocalDateTime.now(clock));
        int changed = jdbc.update(
                "UPDATE dictionary_item SET item_label = :label, description = :description, sort_order = :sortOrder, enabled = :enabled, updated_at = :updatedAt "
                        + "WHERE id = :id AND deleted = FALSE",
                new BeanPropertySqlParameterSource(row));
        if (changed != 1) {
            throw new CommonException(DictionaryResultCodeEnum.DICTIONARY_ITEM_NOT_FOUND);
        }
    }

    @Override
    public void deleteItem(String id) {
        jdbc.update("DELETE FROM dictionary_item WHERE id = :id", new MapSqlParameterSource("id", id));
    }

    private void lockType(String id) {
        var args = new MapSqlParameterSource("id", id);
        if (jdbc.queryForList(
                        "SELECT id FROM dictionary_type WHERE id = :id AND deleted = FALSE FOR UPDATE",
                        args,
                        String.class)
                .isEmpty()) {
            throw new CommonException(DictionaryResultCodeEnum.DICTIONARY_TYPE_NOT_FOUND);
        }
    }

    @Override
    public long countItems(String typeId) {
        return itemMapper.selectCountByQuery(QueryWrapper.create().where(Tables.DICTIONARY_ITEM.TYPE_ID.eq(typeId)));
    }

    @Override
    public PageResult<DictionaryItem> listItems(String typeId, int page, int size) {
        var query = QueryWrapper.create()
                .where(Tables.DICTIONARY_ITEM.TYPE_ID.eq(typeId))
                .orderBy(
                        Tables.DICTIONARY_ITEM.SORT_ORDER.asc(),
                        Tables.DICTIONARY_ITEM.LABEL.asc(),
                        Tables.DICTIONARY_ITEM.VALUE.asc());
        var result = itemMapper.paginate(new Page<>(page, size), query);
        return new PageResult<>(
                result.getRecords().stream().map(converter::toDomain).toList(), result.getTotalRow(), page, size);
    }

    @Override
    public List<DictionaryItem> listEnabledItems(String typeId) {
        var query = QueryWrapper.create()
                .where(Tables.DICTIONARY_ITEM.TYPE_ID.eq(typeId))
                .and(Tables.DICTIONARY_ITEM.ENABLED.eq(true))
                .orderBy(
                        Tables.DICTIONARY_ITEM.SORT_ORDER.asc(),
                        Tables.DICTIONARY_ITEM.LABEL.asc(),
                        Tables.DICTIONARY_ITEM.VALUE.asc());
        return itemMapper.selectListByQuery(query).stream()
                .map(converter::toDomain)
                .toList();
    }
}
