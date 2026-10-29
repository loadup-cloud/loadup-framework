/*
 * #%L
 * LoadUp Transfer Infrastructure
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
package io.github.loadup.modules.transfer.infrastructure.autoconfigure;

import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.modules.transfer.domain.gateway.TransferGateway;
import io.github.loadup.modules.transfer.infrastructure.repository.TransferGatewayImpl;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration(
        afterName = {
            "org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration",
            "io.github.loadup.components.database.autoconfigure.IdGeneratorAutoConfiguration"
        })
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnProperty(
        prefix = "loadup.modules.transfer",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class TransferPersistenceAutoConfiguration {
    @Bean("transferStorageClock")
    @ConditionalOnMissingBean(name = "transferStorageClock")
    public java.time.Clock storageClock() {
        return java.time.Clock.systemDefaultZone();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(TransferGateway.class)
    static class DefaultPersistence {
        @Bean
        public TransferGateway transferGateway(
                JdbcTemplate jdbc,
                IdGenerator ids,
                @org.springframework.beans.factory.annotation.Qualifier("transferStorageClock") java.time.Clock clock) {
            return new TransferGatewayImpl(jdbc, ids, clock);
        }
    }
}
