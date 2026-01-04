package com.github.loadup.components.database.tenant;

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

import lombok.extern.slf4j.Slf4j;

/**
 * Tenant Context Holder
 *
 * <p>Thread-local storage for current tenant ID. Used by multi-tenant feature to automatically
 * filter queries and set tenant_id on insert/update operations.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
public class TenantContextHolder {

  private static final ThreadLocal<String> TENANT_ID = new InheritableThreadLocal<>();

  /**
   * Set current tenant ID
   *
   * @param tenantId tenant ID
   */
  public static void setTenantId(String tenantId) {
    if (tenantId == null) {
      log.warn("Attempting to set null tenant ID, clearing context instead");
      clear();
      return;
    }
    TENANT_ID.set(tenantId);
    log.debug("Set tenant context: {}", tenantId);
  }

  /**
   * Get current tenant ID
   *
   * @return tenant ID, or null if not set
   */
  public static String getTenantId() {
    return TENANT_ID.get();
  }

  /**
   * Check if tenant context is set
   *
   * @return true if tenant context exists
   */
  public static boolean hasTenantId() {
    return TENANT_ID.get() != null;
  }

  /** Clear tenant context */
  public static void clear() {
    TENANT_ID.remove();
    log.debug("Cleared tenant context");
  }

  /**
   * Execute code with specific tenant context
   *
   * @param tenantId tenant ID
   * @param runnable code to execute
   */
  public static void runWithTenant(String tenantId, Runnable runnable) {
    String previousTenantId = getTenantId();
    try {
      setTenantId(tenantId);
      runnable.run();
    } finally {
      if (previousTenantId != null) {
        setTenantId(previousTenantId);
      } else {
        clear();
      }
    }
  }
}
