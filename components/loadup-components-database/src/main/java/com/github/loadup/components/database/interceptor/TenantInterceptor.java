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

import com.github.loadup.components.database.config.DatabaseProperties;
import com.github.loadup.components.database.tenant.TenantContextHolder;
import com.mybatisflex.core.tenant.TenantManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Multi-Tenant Interceptor for MyBatis-Flex
 *
 * <p>Automatically adds tenant_id filter to SQL queries and fills tenant_id on insert/update
 * operations.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

  private final DatabaseProperties databaseProperties;
  private final Set<String> ignoreTables = new HashSet<>();

  /** Initialize tenant interceptor */
  public void init() {
    DatabaseProperties.MultiTenant config = databaseProperties.getMultiTenant();

    // Parse ignore tables
    if (config.getIgnoreTables() != null && !config.getIgnoreTables().isBlank()) {
      String[] tables = config.getIgnoreTables().split(",");
      Arrays.stream(tables).map(String::trim).forEach(ignoreTables::add);
    }

    // Configure MyBatis-Flex TenantManager
    TenantManager.setTenantFactory(
        () -> {
          String tenantId = TenantContextHolder.getTenantId();
          if (tenantId == null) {
            tenantId = config.getDefaultTenantId();
          }
          return new Object[] {tenantId};
        });

    log.info(
        "Initialized TenantInterceptor with column={}, ignoreTables={}",
        config.getColumnName(),
        ignoreTables);
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    return HandlerInterceptor.super.preHandle(request, response, handler);
  }
}
