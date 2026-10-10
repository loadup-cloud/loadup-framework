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

import static io.github.loadup.modules.dictionary.infrastructure.dataobject.table.Tables.*;

import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import io.github.loadup.modules.dictionary.domain.gateway.DictionaryGateway;
import io.github.loadup.modules.dictionary.domain.model.*;
import io.github.loadup.modules.dictionary.infrastructure.converter.DictionaryStorageConverter;
import io.github.loadup.modules.dictionary.infrastructure.dataobject.*;
import io.github.loadup.modules.dictionary.infrastructure.mapper.*;
import java.util.*;

public class DictionaryGatewayImpl implements DictionaryGateway {

    private final DictionaryTypeDOMapper types;
    private final DictionaryItemDOMapper items;
    private final DictionaryStorageConverter converter;

    public DictionaryGatewayImpl(
            DictionaryTypeDOMapper types, DictionaryItemDOMapper items, DictionaryStorageConverter converter) {
        this.types = types;
        this.items = items;
        this.converter = converter;
    }

    private QueryWrapper typeScope(String tenant) {
        return QueryWrapper.create().from(DICTIONARY_TYPE_DO).where(DICTIONARY_TYPE_DO.TENANT_ID.eq(tenant));
    }

    private QueryWrapper itemScope(String tenant) {
        return QueryWrapper.create().from(DICTIONARY_ITEM_DO).where(DICTIONARY_ITEM_DO.TENANT_ID.eq(tenant));
    }

    @Override
    public Optional<DictionaryType> findTypeById(String tenant, String id) {
        return Optional.ofNullable(types.selectOneByQuery(typeScope(tenant).and(DICTIONARY_TYPE_DO.ID.eq(id))))
                .map(converter::toDomain);
    }

    @Override
    public Optional<DictionaryType> findTypeByCode(String tenant, String code) {
        return Optional.ofNullable(types.selectOneByQuery(typeScope(tenant).and(DICTIONARY_TYPE_DO.CODE.eq(code))))
                .map(converter::toDomain);
    }

    @Override
    public void insertType(DictionaryType type) {
        types.insert(converter.toDO(type));
    }

    @Override
    public void updateType(DictionaryType type) {
        var update = UpdateEntity.of(DictionaryTypeDO.class);
        update.setName(type.name());
        update.setDescription(type.description());
        update.setEnabled(type.enabled());
        types.updateByQuery(update, typeScope(type.tenantId()).and(DICTIONARY_TYPE_DO.ID.eq(type.id())));
    }

    @Override
    public void deleteType(String tenant, String id) {
        LogicDeleteManager.execWithoutLogicDelete(
                () -> types.deleteByQuery(typeScope(tenant).and(DICTIONARY_TYPE_DO.ID.eq(id))));
    }

    @Override
    public DictionaryPage<DictionaryType> listTypes(String tenant, int page, int size) {
        var q = typeScope(tenant);
        long total = types.selectCountByQuery(q);
        q.orderBy(DICTIONARY_TYPE_DO.CODE.asc()).limit(size).offset((long) (page - 1) * size);
        return new DictionaryPage<>(
                types.selectListByQuery(q).stream().map(converter::toDomain).toList(), total, page, size);
    }

    @Override
    public Optional<DictionaryItem> findItemById(String tenant, String id) {
        return Optional.ofNullable(items.selectOneByQuery(itemScope(tenant).and(DICTIONARY_ITEM_DO.ID.eq(id))))
                .map(converter::toDomain);
    }

    @Override
    public void insertItem(DictionaryItem item) {
        items.insert(converter.toDO(item));
    }

    @Override
    public void updateItem(DictionaryItem item) {
        var update = UpdateEntity.of(DictionaryItemDO.class);
        update.setLabel(item.label());
        update.setDescription(item.description());
        update.setSortOrder(item.sortOrder());
        update.setEnabled(item.enabled());
        items.updateByQuery(update, itemScope(item.tenantId()).and(DICTIONARY_ITEM_DO.ID.eq(item.id())));
    }

    @Override
    public void deleteItem(String tenant, String id) {
        LogicDeleteManager.execWithoutLogicDelete(
                () -> items.deleteByQuery(itemScope(tenant).and(DICTIONARY_ITEM_DO.ID.eq(id))));
    }

    @Override
    public long countItems(String tenant, String type) {
        return items.selectCountByQuery(itemScope(tenant).and(DICTIONARY_ITEM_DO.TYPE_ID.eq(type)));
    }

    private QueryWrapper orderedItems(String tenant, String type) {
        return itemScope(tenant)
                .and(DICTIONARY_ITEM_DO.TYPE_ID.eq(type))
                .orderBy(
                        DICTIONARY_ITEM_DO.SORT_ORDER.asc(),
                        DICTIONARY_ITEM_DO.LABEL.asc(),
                        DICTIONARY_ITEM_DO.VALUE.asc());
    }

    @Override
    public DictionaryPage<DictionaryItem> listItems(String tenant, String type, int page, int size) {
        long total = countItems(tenant, type);
        var q = orderedItems(tenant, type).limit(size).offset((long) (page - 1) * size);
        return new DictionaryPage<>(
                items.selectListByQuery(q).stream().map(converter::toDomain).toList(), total, page, size);
    }

    @Override
    public List<DictionaryItem> listEnabledItems(String tenant, String type) {
        return items.selectListByQuery(orderedItems(tenant, type).and(DICTIONARY_ITEM_DO.ENABLED.eq(true))).stream()
                .map(converter::toDomain)
                .toList();
    }
}
