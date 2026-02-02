package io.github.loadup.components.database.listener;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import io.github.loadup.commons.dataobject.BaseDO;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.tenant.TenantContextHolder;
import io.micrometer.common.util.StringUtils;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
@RequiredArgsConstructor
public class BaseEntityListener implements InsertListener, UpdateListener {
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
    log.debug("Auto-filled tenantId={} for entity {}", tenantId, entity.getClass().getSimpleName());

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
}
