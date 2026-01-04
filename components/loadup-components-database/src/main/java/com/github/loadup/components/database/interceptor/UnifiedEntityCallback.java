package com.github.loadup.components.database.interceptor;

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

import com.github.loadup.commons.dataobject.BaseDO;
import com.github.loadup.components.database.config.DatabaseProperties;
import com.github.loadup.components.database.id.IdGenerator;
import com.github.loadup.components.database.tenant.TenantConfigService;
import com.github.loadup.components.database.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

/**
 * Unified Before Save Callback
 *
 * <p>Handles multiple concerns before saving an entity:
 *
 * <ul>
 *   <li>ID Generation: Auto-generate ID if null (when enabled)
 *   <li>Logical Delete: Initialize deleted field (when enabled, per-tenant configuration)
 *   <li>Multi-Tenant: Auto-set tenant_id from context (when enabled)
 * </ul>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnifiedEntityCallback implements BeforeConvertCallback<BaseDO> {

  private final DatabaseProperties databaseProperties;
  private final IdGenerator idGenerator;
  private final TenantConfigService tenantConfigService;

  /** Hardcoded default values for logical delete */
  private static final String LOGICAL_DELETE_COLUMN = "deleted";

  private static final Boolean DELETED_VALUE = true;
  private static final Boolean NOT_DELETED_VALUE = false;

  @Override
  public BaseDO onBeforeConvert(BaseDO entity) {
    // 1. ID Generation
    handleIdGeneration(entity);

    // 2. Logical Delete
    handleLogicalDelete(entity);

    // 3. Multi-Tenant
    handleMultiTenant(entity);

    return entity;
  }

  /**
   * Handle ID generation if enabled and ID is null
   *
   * @param entity entity to process
   */
  private void handleIdGeneration(BaseDO entity) {
    if (!databaseProperties.getIdGenerator().isEnabled()) {
      return;
    }

    if (entity.getId() == null && idGenerator != null) {
      String id = idGenerator.generate();
      entity.setId(id);
      log.debug("Generated ID for entity {}: {}", entity.getClass().getSimpleName(), id);
    }
  }

  /**
   * Handle logical delete field initialization if enabled
   *
   * <p>Logical delete is now configured per-tenant. This method queries the tenant's configuration
   * from sys_tenant table to determine whether to initialize the deleted field.
   *
   * @param entity entity to process
   */
  private void handleLogicalDelete(BaseDO entity) {
    // Multi-tenant must be enabled
    if (!databaseProperties.getMultiTenant().isEnabled()) {
      return;
    }

    // Get current tenant ID (may be null if not set yet, will be set in handleMultiTenant)
    String tenantId = entity.getTenantId();
    if (tenantId == null) {
      tenantId = TenantContextHolder.getTenantId();
    }
    if (tenantId == null) {
      tenantId = databaseProperties.getMultiTenant().getDefaultTenantId();
    }

    // Query tenant-specific logical delete configuration from database
    boolean logicalDeleteEnabled = tenantConfigService.isLogicalDeleteEnabled(tenantId);

    if (!logicalDeleteEnabled) {
      log.trace(
          "Logical delete is disabled for tenant: {}, skipping initialization for entity: {}",
          tenantId,
          entity.getClass().getSimpleName());
      return;
    }

    // Initialize deleted field if null
    if (entity.getDeleted() == null) {
      entity.setDeleted(NOT_DELETED_VALUE); // Use hardcoded default: false
      log.trace(
          "Initialized logical delete field for entity: {} (tenant: {}) with value: {}",
          entity.getClass().getSimpleName(),
          tenantId,
          entity.getDeleted());
    }
  }

  /**
   * Handle multi-tenant field setting if enabled
   *
   * @param entity entity to process
   */
  private void handleMultiTenant(BaseDO entity) {
    if (!databaseProperties.getMultiTenant().isEnabled()) {
      return;
    }

    // Only set tenant_id if it's null (don't override existing value)
    if (entity.getTenantId() == null) {
      String tenantId = TenantContextHolder.getTenantId();

      if (tenantId == null) {
        tenantId = databaseProperties.getMultiTenant().getDefaultTenantId();
        log.debug(
            "No tenant context found, using default tenant: {} for entity: {}",
            tenantId,
            entity.getClass().getSimpleName());
      }

      entity.setTenantId(tenantId);
      log.debug("Set tenant_id: {} for entity: {}", tenantId, entity.getClass().getSimpleName());
    }
  }
}
