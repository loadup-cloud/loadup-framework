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
import io.github.loadup.components.gotone.store.dataobject.NotificationServiceRow;
import java.util.Optional;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;

/**
 * JDBC-backed {@link ServiceConfigProvider} backed by {@code gotone_notification_service}.
 */
public class JdbcServiceConfigProvider implements ServiceConfigProvider {

    private final JdbcAggregateTemplate aggregates;
    private final DatabaseTenant tenant;

    public JdbcServiceConfigProvider(JdbcAggregateTemplate aggregates, DatabaseTenant tenant) {
        this.aggregates = aggregates;
        this.tenant = tenant;
    }

    @Override
    public Optional<ServiceConfig> findByServiceCode(String serviceCode) {
        String tenantId = tenant.current();
        if (serviceCode == null) return Optional.empty();
        var criteria = Criteria.where("serviceCode").is(serviceCode).and("enabled").is(true)
                .and("deleted").is(false).and("tenantId");
        var query = Query.query(tenantId == null ? criteria.isNull() : criteria.is(tenantId));
        return aggregates.findOne(query, NotificationServiceRow.class)
                .map(row -> new ServiceConfig(row.serviceCode(), row.serviceName(), row.enabled()));
    }
}
