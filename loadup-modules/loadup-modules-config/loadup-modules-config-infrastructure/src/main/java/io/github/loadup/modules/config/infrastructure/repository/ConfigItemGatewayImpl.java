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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

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

/**
 * Gateway implementation for config items backed by MySQL via MyBatis-Flex.
 * Converts between domain model {@link ConfigItem} and persistence entity {@link ConfigItemDO}.
 */
@Repository
public class ConfigItemGatewayImpl implements ConfigItemGateway {
    private static final Logger log = LoggerFactory.getLogger(ConfigItemGatewayImpl.class);

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
        List<ConfigItemDO> entities = mapper.selectListByQuery(QueryWrapper.create()
                .where(CONFIG_ITEM_DO.CATEGORY.eq(category))
                .orderBy(CONFIG_ITEM_DO.SORT_ORDER.asc()));
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(converter::toModel).collect(Collectors.toList());
    }

    @Override
    public List<ConfigItem> findAll() {
        List<ConfigItemDO> entities = mapper.selectListByQuery(
                QueryWrapper.create().orderBy(CONFIG_ITEM_DO.CATEGORY.asc(), CONFIG_ITEM_DO.SORT_ORDER.asc()));
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(converter::toModel).collect(Collectors.toList());
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

    public ConfigItemGatewayImpl(
            ConfigItemDOMapper mapper, ConfigLocalCache localCache, ConfigItemConverter converter) {
        this.mapper = mapper;
        this.localCache = localCache;
        this.converter = converter;
    }
}
