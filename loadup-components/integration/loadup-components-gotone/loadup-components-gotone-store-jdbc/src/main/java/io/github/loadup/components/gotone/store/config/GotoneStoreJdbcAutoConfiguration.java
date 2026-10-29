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
import io.github.loadup.components.gotone.config.ChannelConfigProvider;
import io.github.loadup.components.gotone.config.ServiceConfigProvider;
import io.github.loadup.components.gotone.record.RecordHandler;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Auto-configuration for the default JDBC store of the gotone component.
 *
 * <p>Each storage SPI is only registered when no custom implementation is present, so integrators
 * can swap the JDBC store for their own {@link ServiceConfigProvider} / {@link ChannelConfigProvider}
 * / {@link RecordHandler} beans without touching the engine.
 */
@AutoConfiguration(afterName = {
        "io.github.loadup.components.database.autoconfigure.DatabaseCoreAutoConfiguration",
        "org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration"})
@ConditionalOnClass({ChannelConfigProvider.class, NamedParameterJdbcTemplate.class})
public class GotoneStoreJdbcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceConfigProvider serviceConfigProvider(NamedParameterJdbcTemplate jdbc, DatabaseTenant tenant) {
        return new JdbcServiceConfigProvider(jdbc, tenant);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChannelConfigProvider channelConfigProvider(NamedParameterJdbcTemplate jdbc, DatabaseTenant tenant) {
        return new JdbcChannelConfigProvider(jdbc, tenant);
    }

    @Bean
    @ConditionalOnMissingBean
    public RecordHandler recordHandler(
            NamedParameterJdbcTemplate jdbc, DatabaseTenant tenant, @Qualifier("databaseClock") Clock clock) {
        return new JdbcRecordHandler(jdbc, tenant, clock);
    }
}
