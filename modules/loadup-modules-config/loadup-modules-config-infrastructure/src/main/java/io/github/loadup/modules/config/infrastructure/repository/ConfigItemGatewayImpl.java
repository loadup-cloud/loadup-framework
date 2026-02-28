package io.github.loadup.modules.config.infrastructure.repository;

import static io.github.loadup.modules.config.infrastructure.dataobject.table.Tables.CONFIG_ITEM_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.modules.config.domain.gateway.ConfigItemGateway;
import io.github.loadup.modules.config.domain.model.ConfigItem;
import io.github.loadup.modules.config.infrastructure.cache.ConfigLocalCache;
import io.github.loadup.modules.config.infrastructure.converter.ConfigItemConverter;
import io.github.loadup.modules.config.infrastructure.dataobject.ConfigItemDO;
import io.github.loadup.modules.config.infrastructure.mapper.ConfigItemDOMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

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

/**
 * Gateway implementation for config items backed by MySQL via MyBatis-Flex.
 * Converts between domain model {@link ConfigItem} and persistence entity {@link ConfigItemDO}.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ConfigItemGatewayImpl implements ConfigItemGateway {

    private final ConfigItemDOMapper mapper;
    private final ConfigLocalCache localCache;
    private final ConfigItemConverter converter;

    @Override
    public Optional<ConfigItem> findByKey(String configKey) {
        return localCache.getConfig(configKey).or(() -> {
            ConfigItemDO entity =
                    mapper.selectOneByQuery(QueryWrapper.create().where(CONFIG_ITEM_DO.CONFIG_KEY.eq(configKey)));
            if (entity != null) {
                ConfigItem item = converter.toModel(entity);
                localCache.putConfig(item);
                return Optional.of(item);
            }
            return Optional.empty();
        });
    }

    @Override
    public List<ConfigItem> findByCategory(String category) {
        return mapper
                .selectListByQuery(QueryWrapper.create()
                        .where(CONFIG_ITEM_DO.CATEGORY.eq(category))
                        .orderBy(CONFIG_ITEM_DO.SORT_ORDER.asc()))
                .stream()
                .map(converter::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfigItem> findAll() {
        return mapper
                .selectListByQuery(
                        QueryWrapper.create().orderBy(CONFIG_ITEM_DO.CATEGORY.asc(), CONFIG_ITEM_DO.SORT_ORDER.asc()))
                .stream()
                .map(converter::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public void save(ConfigItem item) {
        mapper.insert(converter.toEntity(item));
        localCache.putConfig(item);
    }

    @Override
    public void update(ConfigItem item) {
        mapper.update(converter.toEntity(item));
        localCache.evictConfig(item.getConfigKey());
    }

    @Override
    public void deleteByKey(String configKey) {
        mapper.deleteByQuery(QueryWrapper.create().where(CONFIG_ITEM_DO.CONFIG_KEY.eq(configKey)));
        localCache.evictConfig(configKey);
    }

    @Override
    public boolean existsByKey(String configKey) {
        return mapper.selectCountByQuery(QueryWrapper.create().where(CONFIG_ITEM_DO.CONFIG_KEY.eq(configKey))) > 0;
    }
}
