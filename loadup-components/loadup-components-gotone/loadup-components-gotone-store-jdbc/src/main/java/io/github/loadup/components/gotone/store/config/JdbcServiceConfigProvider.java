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

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.components.gotone.config.ServiceConfigProvider;
import io.github.loadup.components.gotone.store.dataobject.NotificationServiceDO;
import io.github.loadup.components.gotone.store.mapper.NotificationServiceDOMapper;
import java.util.Optional;

/**
 * JDBC-backed {@link ServiceConfigProvider} backed by {@code gotone_notification_service}.
 */
public class JdbcServiceConfigProvider implements ServiceConfigProvider {

    private final NotificationServiceDOMapper mapper;

    public JdbcServiceConfigProvider(NotificationServiceDOMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<ServiceConfig> findByServiceCode(String serviceCode) {
        NotificationServiceDO entity = mapper.selectOneByQuery(QueryWrapper.create()
                .where(NotificationServiceDO::getServiceCode)
                .eq(serviceCode)
                .and(NotificationServiceDO::isEnabled)
                .eq(true));
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(new ServiceConfig(entity.getServiceCode(), entity.getServiceName(), entity.isEnabled()));
    }
}
