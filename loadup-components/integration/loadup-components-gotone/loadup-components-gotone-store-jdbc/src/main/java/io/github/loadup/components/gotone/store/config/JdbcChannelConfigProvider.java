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
import io.github.loadup.components.gotone.store.dataobject.ServiceChannelDO;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * JDBC-backed {@link ChannelConfigProvider} backed by {@code gotone_service_channel}.
 */
public class JdbcChannelConfigProvider implements ChannelConfigProvider {

    private final NamedParameterJdbcTemplate jdbc;
    private final DatabaseTenant tenant;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JdbcChannelConfigProvider(NamedParameterJdbcTemplate jdbc, DatabaseTenant tenant) {
        this.jdbc = jdbc;
        this.tenant = tenant;
    }

    @Override
    public List<ChannelConfig> findEnabledByServiceCode(String serviceCode) {
        List<ServiceChannelDO> entities = jdbc.query(
                """
                SELECT id, service_code, channel, template_content, channel_config, provider, fallback_providers, enabled, priority
                FROM gotone_service_channel WHERE service_code = :code AND tenant_id IS NOT DISTINCT FROM :tenant AND enabled = TRUE AND deleted = FALSE ORDER BY priority ASC
                """,
                new MapSqlParameterSource("code", serviceCode).addValue("tenant", tenant.current()),
                BeanPropertyRowMapper.newInstance(ServiceChannelDO.class));
        if (entities == null) {
            return List.of();
        }
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
            return objectMapper.readValue(
                    json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JacksonException e) {
            LogUtil.warn(
                    JdbcChannelConfigProvider.class,
                    "Failed to parse fallbackProviders JSON: exceptionType={}",
                    e.getClass().getSimpleName());
            return List.of();
        }
    }
}
