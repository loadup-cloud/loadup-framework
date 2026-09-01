package io.github.loadup.modules.config.infrastructure.repository;

/*-
 * #%L
 * Loadup Modules Config Infrastructure
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

import static io.github.loadup.modules.config.infrastructure.dataobject.table.Tables.DICT_ITEM_DO;
import static io.github.loadup.modules.config.infrastructure.dataobject.table.Tables.DICT_TYPE_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.modules.config.domain.gateway.DictGateway;
import io.github.loadup.modules.config.domain.model.DictItem;
import io.github.loadup.modules.config.domain.model.DictType;
import io.github.loadup.modules.config.infrastructure.cache.ConfigLocalCache;
import io.github.loadup.modules.config.infrastructure.converter.DictConverter;
import io.github.loadup.modules.config.infrastructure.dataobject.DictItemDO;
import io.github.loadup.modules.config.infrastructure.dataobject.DictTypeDO;
import io.github.loadup.modules.config.infrastructure.mapper.DictItemDOMapper;
import io.github.loadup.modules.config.infrastructure.mapper.DictTypeDOMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class DictGatewayImpl implements DictGateway {
    private static final Logger log = LoggerFactory.getLogger(DictGatewayImpl.class);

    private final DictTypeDOMapper typeMapper;
    private final DictItemDOMapper itemMapper;
    private final ConfigLocalCache localCache;
    private final DictConverter converter;

    /* ---- DictType ---- */

    @Override
    public List<DictType> findAllTypes() {
        List<DictTypeDO> entities =
                typeMapper.selectListByQuery(QueryWrapper.create().orderBy(DICT_TYPE_DO.SORT_ORDER.asc()));
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(converter::toModel).collect(Collectors.toList());
    }

    @Override
    public Optional<DictType> findTypeByCode(String dictCode) {
        return Optional.ofNullable(
                        typeMapper.selectOneByQuery(QueryWrapper.create().where(DICT_TYPE_DO.DICT_CODE.eq(dictCode))))
                .map(converter::toModel);
    }

    @Override
    public void saveType(DictType type) {
        typeMapper.insert(converter.toEntity(type));
    }

    @Override
    public void updateType(DictType type) {
        typeMapper.update(converter.toEntity(type));
        localCache.evictDict(type.getDictCode());
    }

    @Override
    public void deleteTypeByCode(String dictCode) {
        typeMapper.deleteByQuery(QueryWrapper.create().where(DICT_TYPE_DO.DICT_CODE.eq(dictCode)));
        localCache.evictDict(dictCode);
    }

    @Override
    public boolean existsTypeByCode(String dictCode) {
        return typeMapper.selectCountByQuery(QueryWrapper.create().where(DICT_TYPE_DO.DICT_CODE.eq(dictCode))) > 0;
    }

    /* ---- DictItem ---- */

    @Override
    public List<DictItem> findItemsByCode(String dictCode) {
        return localCache.getDictItems(dictCode).orElseGet(() -> {
            List<DictItemDO> entities = itemMapper.selectListByQuery(QueryWrapper.create()
                    .where(DICT_ITEM_DO.DICT_CODE.eq(dictCode))
                    .and(DICT_ITEM_DO.ENABLED.eq(true))
                    .orderBy(DICT_ITEM_DO.SORT_ORDER.asc()));
            List<DictItem> items = entities == null
                    ? List.of()
                    : entities.stream().map(converter::toModel).collect(Collectors.toList());
            localCache.putDictItems(dictCode, items);
            return items;
        });
    }

    @Override
    public List<DictItem> findItemsByCodeAndParent(String dictCode, String parentValue) {
        List<DictItemDO> entities = itemMapper.selectListByQuery(QueryWrapper.create()
                .where(DICT_ITEM_DO.DICT_CODE.eq(dictCode))
                .and(DICT_ITEM_DO.PARENT_VALUE.eq(parentValue))
                .and(DICT_ITEM_DO.ENABLED.eq(true))
                .orderBy(DICT_ITEM_DO.SORT_ORDER.asc()));
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(converter::toModel).collect(Collectors.toList());
    }

    @Override
    public void saveItem(DictItem item) {
        itemMapper.insert(converter.toEntity(item));
        localCache.evictDict(item.getDictCode());
    }

    @Override
    public void updateItem(DictItem item) {
        itemMapper.update(converter.toEntity(item));
        localCache.evictDict(item.getDictCode());
    }

    @Override
    public void deleteItemById(String id) {
        DictItemDO entity = itemMapper.selectOneById(id);
        itemMapper.deleteById(id);
        if (entity != null) {
            localCache.evictDict(entity.getDictCode());
        }
    }

    @Override
    public void deleteItemsByCode(String dictCode) {
        itemMapper.deleteByQuery(QueryWrapper.create().where(DICT_ITEM_DO.DICT_CODE.eq(dictCode)));
        localCache.evictDict(dictCode);
    }

    public DictGatewayImpl(
            DictTypeDOMapper typeMapper,
            DictItemDOMapper itemMapper,
            ConfigLocalCache localCache,
            DictConverter converter) {
        this.typeMapper = typeMapper;
        this.itemMapper = itemMapper;
        this.localCache = localCache;
        this.converter = converter;
    }
}
