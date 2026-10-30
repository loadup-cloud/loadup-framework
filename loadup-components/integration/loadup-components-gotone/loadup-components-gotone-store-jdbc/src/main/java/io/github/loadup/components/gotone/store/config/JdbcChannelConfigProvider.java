/*
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

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.database.tenant.DatabaseTenant;
import io.github.loadup.components.gotone.config.ChannelConfigProvider;
import io.github.loadup.components.gotone.store.dataobject.ServiceChannelRow;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * JDBC-backed {@link ChannelConfigProvider} backed by {@code gotone_service_channel}.
 */
public class JdbcChannelConfigProvider implements ChannelConfigProvider {

    private final JdbcAggregateTemplate aggregates;
    private final DatabaseTenant tenant;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JdbcChannelConfigProvider(JdbcAggregateTemplate aggregates, DatabaseTenant tenant) {
        this.aggregates = aggregates;
        this.tenant = tenant;
    }

    @Override
    public List<ChannelConfig> findEnabledByServiceCode(String serviceCode) {
        String tenantId = tenant.current();
        if (serviceCode == null) return List.of();
        var criteria = Criteria.where("serviceCode").is(serviceCode).and("enabled").is(true)
                .and("deleted").is(false).and("tenantId");
        var query = Query.query(tenantId == null ? criteria.isNull() : criteria.is(tenantId))
                .sort(Sort.by("priority"));
        List<ServiceChannelRow> entities = aggregates.findAll(query, ServiceChannelRow.class);
        return entities.stream().map(this::toConfig).toList();
    }

    private ChannelConfig toConfig(ServiceChannelRow entity) {
        return new ChannelConfig(
                entity.channel(),
                entity.provider(),
                parseJsonList(entity.fallbackProviders()),
                entity.templateContent(),
                parseJsonMap(entity.channelConfig()));
    }

    private Map<String, Object> parseJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            Map<String, Object> result = objectMapper.readValue(
                    json, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
            return result == null ? Map.of() : result;
        } catch (JacksonException e) {
            LogUtil.warn(
                    JdbcChannelConfigProvider.class,
                    "Failed to parse channelConfig JSON: exceptionType={}",
                    e.getClass().getSimpleName());
            return Map.of();
        }
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<String> result = objectMapper.readValue(
                    json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            return result == null ? List.of() : result;
        } catch (JacksonException e) {
            LogUtil.warn(
                    JdbcChannelConfigProvider.class,
                    "Failed to parse fallbackProviders JSON: exceptionType={}",
                    e.getClass().getSimpleName());
            return List.of();
        }
    }
}
