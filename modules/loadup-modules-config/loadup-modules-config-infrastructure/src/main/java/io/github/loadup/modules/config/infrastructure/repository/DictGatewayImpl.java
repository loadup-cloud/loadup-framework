package io.github.loadup.modules.config.infrastructure.repository;

/*-
 * #%L
 * Loadup Modules Config Infrastructure
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

import static io.github.loadup.modules.config.infrastructure.dataobject.table.Tables.DICT_ITEM_DO;
import static io.github.loadup.modules.config.infrastructure.dataobject.table.Tables.DICT_TYPE_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.modules.config.domain.gateway.DictGateway;
import io.github.loadup.modules.config.domain.model.DictItem;
import io.github.loadup.modules.config.domain.model.DictType;
import io.github.loadup.modules.config.infrastructure.cache.ConfigLocalCache;
import io.github.loadup.modules.config.infrastructure.converter.DictConverter;
import io.github.loadup.modules.config.infrastructure.dataobject.DictItemDO;
import io.github.loadup.modules.config.infrastructure.mapper.DictItemDOMapper;
import io.github.loadup.modules.config.infrastructure.mapper.DictTypeDOMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DictGatewayImpl implements DictGateway {

    private final DictTypeDOMapper typeMapper;
    private final DictItemDOMapper itemMapper;
    private final ConfigLocalCache localCache;
    private final DictConverter converter;

    /* ---- DictType ---- */

    @Override
    public List<DictType> findAllTypes() {
        return typeMapper.selectListByQuery(QueryWrapper.create().orderBy(DICT_TYPE_DO.SORT_ORDER.asc())).stream()
                .map(converter::toModel)
                .collect(Collectors.toList());
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
            List<DictItem> items = itemMapper
                    .selectListByQuery(QueryWrapper.create()
                            .where(DICT_ITEM_DO.DICT_CODE.eq(dictCode))
                            .and(DICT_ITEM_DO.ENABLED.eq(true))
                            .orderBy(DICT_ITEM_DO.SORT_ORDER.asc()))
                    .stream()
                    .map(converter::toModel)
                    .collect(Collectors.toList());
            localCache.putDictItems(dictCode, items);
            return items;
        });
    }

    @Override
    public List<DictItem> findItemsByCodeAndParent(String dictCode, String parentValue) {
        return itemMapper
                .selectListByQuery(QueryWrapper.create()
                        .where(DICT_ITEM_DO.DICT_CODE.eq(dictCode))
                        .and(DICT_ITEM_DO.PARENT_VALUE.eq(parentValue))
                        .and(DICT_ITEM_DO.ENABLED.eq(true))
                        .orderBy(DICT_ITEM_DO.SORT_ORDER.asc()))
                .stream()
                .map(converter::toModel)
                .collect(Collectors.toList());
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
}
