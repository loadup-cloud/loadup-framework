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

import static io.github.loadup.modules.config.infrastructure.dataobject.table.Tables.CONFIG_HISTORY_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.modules.config.domain.enums.ChangeType;
import io.github.loadup.modules.config.domain.gateway.ConfigHistoryGateway;
import io.github.loadup.modules.config.domain.model.ConfigHistory;
import io.github.loadup.modules.config.infrastructure.dataobject.ConfigHistoryDO;
import io.github.loadup.modules.config.infrastructure.mapper.ConfigHistoryDOMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ConfigHistoryGatewayImpl implements ConfigHistoryGateway {

    private final ConfigHistoryDOMapper mapper;

    @Override
    public void save(ConfigHistory history) {
        ConfigHistoryDO entity = toEntity(history);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        mapper.insert(entity);
    }

    @Override
    public List<ConfigHistory> findByKey(String configKey) {
        return mapper
                .selectListByQuery(QueryWrapper.create()
                        .where(CONFIG_HISTORY_DO.CONFIG_KEY.eq(configKey))
                        .orderBy(CONFIG_HISTORY_DO.CREATED_AT.desc()))
                .stream()
                .map(this::toModel)
                .toList();
    }

    private ConfigHistoryDO toEntity(ConfigHistory h) {
        ConfigHistoryDO e = new ConfigHistoryDO();
        e.setId(h.getId());
        e.setConfigKey(h.getConfigKey());
        e.setOldValue(h.getOldValue());
        e.setNewValue(h.getNewValue());
        e.setChangeType(h.getChangeType() == null ? null : h.getChangeType().name());
        e.setOperator(h.getOperator());
        e.setRemark(h.getRemark());
        e.setCreatedAt(h.getCreatedAt());
        return e;
    }

    private ConfigHistory toModel(ConfigHistoryDO e) {
        return ConfigHistory.builder()
                .id(e.getId())
                .configKey(e.getConfigKey())
                .oldValue(e.getOldValue())
                .newValue(e.getNewValue())
                .changeType(e.getChangeType() == null ? null : ChangeType.valueOf(e.getChangeType()))
                .operator(e.getOperator())
                .remark(e.getRemark())
                .createdAt(e.getCreatedAt())
                .build();
    }

    public ConfigHistoryGatewayImpl(ConfigHistoryDOMapper mapper) {
        this.mapper = mapper;
    }
}
