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

import io.github.loadup.components.database.autoconfig.MyBatisFlexAutoConfiguration;
import io.github.loadup.components.gotone.config.ChannelConfigProvider;
import io.github.loadup.components.gotone.config.ServiceConfigProvider;
import io.github.loadup.components.gotone.record.RecordHandler;
import io.github.loadup.components.gotone.store.mapper.NotificationRecordDOMapper;
import io.github.loadup.components.gotone.store.mapper.NotificationServiceDOMapper;
import io.github.loadup.components.gotone.store.mapper.ServiceChannelDOMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the default JDBC store of the gotone component.
 *
 * <p>Each storage SPI is only registered when no custom implementation is present, so integrators
 * can swap the JDBC store for their own {@link ServiceConfigProvider} / {@link ChannelConfigProvider}
 * / {@link RecordHandler} beans without touching the engine.
 */
@AutoConfiguration(after = MyBatisFlexAutoConfiguration.class)
@ConditionalOnClass({ChannelConfigProvider.class, MyBatisFlexAutoConfiguration.class})
@MapperScan("io.github.loadup.components.gotone.store.mapper")
public class GotoneStoreJdbcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceConfigProvider serviceConfigProvider(NotificationServiceDOMapper mapper) {
        return new JdbcServiceConfigProvider(mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChannelConfigProvider channelConfigProvider(ServiceChannelDOMapper mapper) {
        return new JdbcChannelConfigProvider(mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public RecordHandler recordHandler(NotificationRecordDOMapper mapper) {
        return new JdbcRecordHandler(mapper);
    }
}
