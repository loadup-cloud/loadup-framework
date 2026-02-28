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

import static io.github.loadup.modules.config.infrastructure.dataobject.table.Tables.CONFIG_HISTORY_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.modules.config.domain.enums.ChangeType;
import io.github.loadup.modules.config.domain.gateway.ConfigHistoryGateway;
import io.github.loadup.modules.config.domain.model.ConfigHistory;
import io.github.loadup.modules.config.infrastructure.dataobject.ConfigHistoryDO;
import io.github.loadup.modules.config.infrastructure.mapper.ConfigHistoryDOMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
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
}
