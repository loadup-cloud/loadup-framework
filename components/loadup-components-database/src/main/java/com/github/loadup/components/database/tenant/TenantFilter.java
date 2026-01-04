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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Tenant Filter
 *
 * <p>Extracts tenant ID from HTTP request and sets it in TenantContextHolder. Tenant ID can be
 * provided via:
 *
 * <ul>
 *   <li>Header: X-Tenant-Id
 *   <li>Query parameter: tenantId
 *   <li>Subdomain: {tenant}.example.com
 * </ul>
 *
 * <p>Also validates if the tenant exists in database.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

  private static final String HEADER_TENANT_ID = "X-Tenant-Id";
  private static final String PARAM_TENANT_ID = "tenantId";

  private final TenantConfigService tenantConfigService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String tenantId = extractTenantId(request);

      if (tenantId != null) {
        // Validate tenant exists in database
        if (!tenantConfigService.tenantExists(tenantId)) {
          log.warn("Invalid tenant ID in request: {}", tenantId);
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          response.setContentType("application/json;charset=UTF-8");
          response
              .getWriter()
              .write(
                  String.format(
                      "{\"error\":\"Invalid tenant\",\"message\":\"Tenant '%s' does not exist\"}",
                      tenantId));
          return;
        }

        TenantContextHolder.setTenantId(tenantId);
        log.debug("Set tenant context from request: {}", tenantId);
      }

      filterChain.doFilter(request, response);

    } finally {
      TenantContextHolder.clear();
    }
  }

  /**
   * Extract tenant ID from request
   *
   * <p>Priority: Header > Query Parameter > Subdomain
   *
   * @param request HTTP request
   * @return tenant ID or null
   */
  private String extractTenantId(HttpServletRequest request) {
    // 1. Try header
    String tenantId = request.getHeader(HEADER_TENANT_ID);
    if (tenantId != null && !tenantId.isBlank()) {
      return tenantId.trim();
    }

    // 2. Try query parameter
    tenantId = request.getParameter(PARAM_TENANT_ID);
    if (tenantId != null && !tenantId.isBlank()) {
      return tenantId.trim();
    }

    // 3. Try subdomain (e.g., tenant1.example.com -> tenant1)
    String serverName = request.getServerName();
    if (serverName != null && serverName.contains(".")) {
      String subdomain = serverName.substring(0, serverName.indexOf('.'));
      if (!subdomain.equals("www") && !subdomain.equals("api")) {
        return subdomain;
      }
    }

    return null;
  }
}
