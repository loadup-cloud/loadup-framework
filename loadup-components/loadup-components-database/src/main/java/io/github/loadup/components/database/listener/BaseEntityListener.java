package io.github.loadup.components.database.listener;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
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

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import io.github.loadup.commons.dataobject.BaseDO;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.tenant.TenantContextHolder;
import io.micrometer.common.util.StringUtils;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base Entity Listener
 *
 * <p>Automatically fills common fields for entities extending BaseDO:
 *
 * <ul>
 *   <li>tenantId: Set from TenantContextHolder on insert
 *   <li>createdAt: Set current time on insert
 *   <li>updatedAt: Set current time on insert and update
 *   <li>deleted: Initialize to false on insert
 * </ul>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class BaseEntityListener implements InsertListener, UpdateListener {
    private static final Logger log = LoggerFactory.getLogger(BaseEntityListener.class);

    private final DatabaseProperties databaseProperties;

    @Override
    public void onInsert(Object entity) {
        if (!(entity instanceof BaseDO baseDO)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // Set tenant ID if multi-tenant is enabled
        String tenantId = TenantContextHolder.getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            if (databaseProperties.getMultiTenant().isEnabled()) {
                throw new RuntimeException("multi-tenant is enabled but tenantId not found!");
            }
            tenantId = databaseProperties.getMultiTenant().getDefaultTenantId();
        }
        baseDO.setTenantId(tenantId);
        log.debug(
                "Auto-filled tenantId={} for entity {}",
                tenantId,
                entity.getClass().getSimpleName());

        // Set timestamps
        if (baseDO.getCreatedAt() == null) {
            baseDO.setCreatedAt(now);
        }
        if (baseDO.getUpdatedAt() == null) {
            baseDO.setUpdatedAt(now);
        }

        // Initialize deleted flag if logical delete is enabled
        baseDO.setDeleted(false);
    }

    @Override
    public void onUpdate(Object entity) {
        if (!(entity instanceof BaseDO baseDO)) {
            return;
        }

        // Update timestamp
        baseDO.setUpdatedAt(LocalDateTime.now());
    }

    public BaseEntityListener(DatabaseProperties databaseProperties) {
        this.databaseProperties = databaseProperties;
    }
}
