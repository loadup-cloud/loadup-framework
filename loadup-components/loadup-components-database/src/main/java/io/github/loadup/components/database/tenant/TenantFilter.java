/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2026 LoadUp Cloud
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

package io.github.loadup.components.database.tenant;

import io.github.loadup.components.database.config.DatabaseProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/** Binds the configured request tenant to {@link TenantContextHolder}. */
public class TenantFilter extends OncePerRequestFilter {
    private final DatabaseProperties.Request requestProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String previousTenantId = TenantContextHolder.getTenantId();
        String tenantId = resolveTenantId(request);
        try {
            if (tenantId != null) {
                TenantContextHolder.setTenantId(tenantId);
            }
            filterChain.doFilter(request, response);
        } finally {
            if (previousTenantId == null) {
                TenantContextHolder.clear();
            } else {
                TenantContextHolder.setTenantId(previousTenantId);
            }
        }
    }

    private String resolveTenantId(HttpServletRequest request) {
        String tenantId = StringUtils.hasText(requestProperties.getHeaderName())
                ? readValue(request.getHeader(requestProperties.getHeaderName()))
                : null;
        if (tenantId != null) {
            return tenantId;
        }
        if (StringUtils.hasText(requestProperties.getParameterName())) {
            tenantId = readValue(request.getParameter(requestProperties.getParameterName()));
            if (tenantId != null) {
                return tenantId;
            }
        }
        if (!requestProperties.isSubdomainEnabled()) {
            return null;
        }
        String serverName = request.getServerName();
        if (!StringUtils.hasText(serverName) || !serverName.contains(".")) {
            return null;
        }
        String subdomain = serverName.substring(0, serverName.indexOf('.'));
        boolean excluded =
                requestProperties.getExcludedSubdomains().stream().anyMatch(value -> value.equalsIgnoreCase(subdomain));
        return excluded ? null : readValue(subdomain);
    }

    private String readValue(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    public TenantFilter(DatabaseProperties.Request requestProperties) {
        this.requestProperties = requestProperties;
    }
}
