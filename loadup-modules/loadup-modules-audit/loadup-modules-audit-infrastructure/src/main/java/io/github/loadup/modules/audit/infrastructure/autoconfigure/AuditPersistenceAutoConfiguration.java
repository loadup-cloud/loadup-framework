/*
 * #%L
 * LoadUp Audit Infrastructure
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
package io.github.loadup.modules.audit.infrastructure.autoconfigure;

import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.modules.audit.domain.gateway.AuditGateway;
import io.github.loadup.modules.audit.infrastructure.converter.AuditStorageConverter;
import io.github.loadup.modules.audit.infrastructure.converter.AuditStorageConverterImpl;
import io.github.loadup.modules.audit.infrastructure.repository.AuditGatewayImpl;
import java.time.Clock;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@AutoConfiguration
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnProperty(prefix = "loadup.modules.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditPersistenceAutoConfiguration {
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(AuditGateway.class)
    @Import(AuditStorageConverterImpl.class)
    static class DefaultPersistence {
        @Bean
        public AuditGateway auditGateway(
                NamedParameterJdbcTemplate jdbc,
                JdbcAggregateTemplate aggregates,
                IdGenerator ids,
                AuditStorageConverter converter,
                @Qualifier("databaseClock") Clock clock) {
            return new AuditGatewayImpl(jdbc, aggregates, ids, converter, clock);
        }
    }
}
