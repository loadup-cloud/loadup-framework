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

import com.github.loadup.components.database.config.DatabaseProperties;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Tenant Configuration Service
 *
 * <p>Provides access to tenant-specific configuration from database. Uses in-memory cache to avoid
 * frequent database queries.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantConfigService {

  private final JdbcTemplate jdbcTemplate;
  private final DatabaseProperties databaseProperties;

  // In-memory cache for tenant configurations
  private final ConcurrentHashMap<String, Boolean> logicalDeleteCache = new ConcurrentHashMap<>();

  /** Hardcoded default value for logical delete when tenant not found or multi-tenant disabled */
  private static final boolean DEFAULT_LOGICAL_DELETE_ENABLED = false;

  /**
   * Check if logical delete is enabled for the given tenant
   *
   * <p>This method queries the sys_tenant table and caches the result. Cache is thread-safe and
   * shared across all requests.
   *
   * @param tenantId tenant ID
   * @return true if logical delete is enabled for this tenant
   */
  public boolean isLogicalDeleteEnabled(String tenantId) {
    if (tenantId == null) {
      // Use default when no tenant context
      return DEFAULT_LOGICAL_DELETE_ENABLED;
    }

    // Try cache first
    Boolean cached = logicalDeleteCache.get(tenantId);
    if (cached != null) {
      return cached;
    }

    // Query from database
    try {
      String sql = "SELECT logical_delete_enabled FROM sys_tenant WHERE id = ?";
      Boolean enabled = jdbcTemplate.queryForObject(sql, Boolean.class, tenantId);

      if (enabled == null) {
        enabled = DEFAULT_LOGICAL_DELETE_ENABLED;
      }

      // Cache the result
      logicalDeleteCache.put(tenantId, enabled);
      log.debug("Loaded logical delete config for tenant {}: {}", tenantId, enabled);

      return enabled;

    } catch (EmptyResultDataAccessException e) {
      // Tenant not found, use default
      log.warn("Tenant {} not found in database, using default logical delete config", tenantId);
      logicalDeleteCache.put(tenantId, DEFAULT_LOGICAL_DELETE_ENABLED);
      return DEFAULT_LOGICAL_DELETE_ENABLED;

    } catch (Exception e) {
      log.error("Error querying logical delete config for tenant {}: {}", tenantId, e.getMessage());
      // On error, use default
      return DEFAULT_LOGICAL_DELETE_ENABLED;
    }
  }

  /**
   * Clear cache for specific tenant
   *
   * <p>Call this method when tenant configuration is updated
   *
   * @param tenantId tenant ID
   */
  public void evictCache(String tenantId) {
    logicalDeleteCache.remove(tenantId);
    log.debug("Evicted logical delete cache for tenant {}", tenantId);
  }

  /** Clear all cache */
  public void evictAllCache() {
    logicalDeleteCache.clear();
    log.debug("Evicted all logical delete cache");
  }

  /**
   * Update logical delete configuration for tenant
   *
   * @param tenantId tenant ID
   * @param enabled whether to enable logical delete
   */
  public void updateLogicalDeleteConfig(String tenantId, boolean enabled) {
    String sql = "UPDATE sys_tenant SET logical_delete_enabled = ? WHERE id = ?";
    int updated = jdbcTemplate.update(sql, enabled, tenantId);

    if (updated > 0) {
      evictCache(tenantId);
      log.info("Updated logical delete config for tenant {}: {}", tenantId, enabled);
    } else {
      log.warn("Failed to update logical delete config for tenant {}: tenant not found", tenantId);
    }
  }

  /**
   * Check if tenant exists in database
   *
   * @param tenantId tenant ID
   * @return true if tenant exists
   */
  public boolean tenantExists(String tenantId) {
    if (tenantId == null) {
      return false;
    }

    try {
      String sql = "SELECT COUNT(*) FROM sys_tenant WHERE id = ?";
      Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tenantId);
      return count != null && count > 0;
    } catch (Exception e) {
      log.error("Error checking if tenant {} exists: {}", tenantId, e.getMessage());
      return false;
    }
  }
}
