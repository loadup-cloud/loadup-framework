/*-
 * #%L
 * Loadup Components Database
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
package io.github.loadup.components.database.listener;

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import io.github.loadup.commons.dataobject.BaseDO;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.id.IdGenerator;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.util.StringUtils;

/** Populates common fields before MyBatis-Flex insert and update operations. */
public class BaseEntityListener implements InsertListener, UpdateListener {
    private final DatabaseProperties databaseProperties;
    private final IdGenerator idGenerator;
    private final Clock clock;

    @Override
    public void onInsert(Object entity) {
        if (!(entity instanceof BaseDO baseDO)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now(clock);

        if (!StringUtils.hasText(baseDO.getId())) {
            baseDO.setId(idGenerator.generate());
        }

        if (baseDO.getCreatedAt() == null) {
            baseDO.setCreatedAt(now);
        }
        baseDO.setUpdatedAt(now);

        DatabaseProperties.MultiTenant tenant = databaseProperties.getMultiTenant();
        if (!tenant.isEnabled()) {
            baseDO.setTenantId(tenant.getDefaultTenantId());
        } else {
            String tenantId = TenantUtil.getTenantId();
            if (!StringUtils.hasText(tenantId) && tenant.isRequired()) {
                throw new TenantContextMissingException(entity.getClass());
            }
            if (StringUtils.hasText(tenantId)) {
                baseDO.setTenantId(tenantId);
            }
        }

        baseDO.setDeleted(0);
    }

    @Override
    public void onUpdate(Object entity) {
        if (!(entity instanceof BaseDO baseDO)) {
            return;
        }

        baseDO.setUpdatedAt(LocalDateTime.now(clock));
    }

    public BaseEntityListener(DatabaseProperties databaseProperties, IdGenerator idGenerator, Clock clock) {
        this.databaseProperties = databaseProperties;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }
}
