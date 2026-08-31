/*-
 * #%L
 * Loadup Gotone Store JDBC
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
package io.github.loadup.components.gotone.store.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.components.gotone.config.ChannelConfigProvider;
import io.github.loadup.components.gotone.store.dataobject.ServiceChannelDO;
import io.github.loadup.components.gotone.store.mapper.ServiceChannelDOMapper;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC-backed {@link ChannelConfigProvider} backed by {@code gotone_service_channel}.
 */
public class JdbcChannelConfigProvider implements ChannelConfigProvider {

    private static final Logger log = LoggerFactory.getLogger(JdbcChannelConfigProvider.class);

    private final ServiceChannelDOMapper mapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JdbcChannelConfigProvider(ServiceChannelDOMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ChannelConfig> findEnabledByServiceCode(String serviceCode) {
        List<ServiceChannelDO> entities = mapper.selectListByQuery(QueryWrapper.create()
                .where(ServiceChannelDO::getServiceCode)
                .eq(serviceCode)
                .and(ServiceChannelDO::isEnabled)
                .eq(true)
                .orderBy(ServiceChannelDO::getPriority)
                .asc());
        return entities.stream().map(this::toConfig).toList();
    }

    private ChannelConfig toConfig(ServiceChannelDO entity) {
        return new ChannelConfig(
                entity.getChannel(),
                entity.getProvider(),
                parseJsonList(entity.getFallbackProviders()),
                entity.getTemplateContent(),
                parseJsonMap(entity.getChannelConfig()));
    }

    private Map<String, Object> parseJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(
                    json, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse channelConfig JSON, using empty config", e);
            return Map.of();
        }
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse fallbackProviders JSON, using empty list", e);
            return List.of();
        }
    }
}
