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

import io.github.loadup.components.database.tenant.DatabaseTenant;
import io.github.loadup.components.gotone.config.ServiceConfigProvider;
import io.github.loadup.components.gotone.store.dataobject.NotificationServiceDO;
import java.util.Optional;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * JDBC-backed {@link ServiceConfigProvider} backed by {@code gotone_notification_service}.
 */
public class JdbcServiceConfigProvider implements ServiceConfigProvider {

    private final NamedParameterJdbcTemplate jdbc;
    private final DatabaseTenant tenant;

    public JdbcServiceConfigProvider(NamedParameterJdbcTemplate jdbc, DatabaseTenant tenant) {
        this.jdbc = jdbc;
        this.tenant = tenant;
    }

    @Override
    public Optional<ServiceConfig> findByServiceCode(String serviceCode) {
        NotificationServiceDO entity = jdbc
                .query(
                        "SELECT id, service_code, service_name, enabled FROM gotone_notification_service "
                                + "WHERE service_code = :code AND tenant_id IS NOT DISTINCT FROM :tenant AND enabled = TRUE AND deleted = FALSE",
                        new MapSqlParameterSource("code", serviceCode).addValue("tenant", tenant.current()),
                        BeanPropertyRowMapper.newInstance(NotificationServiceDO.class))
                .stream()
                .findFirst()
                .orElse(null);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(new ServiceConfig(entity.getServiceCode(), entity.getServiceName(), entity.getEnabled()));
    }
}
