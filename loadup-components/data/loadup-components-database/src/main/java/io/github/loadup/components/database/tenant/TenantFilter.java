/*
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
package io.github.loadup.components.database.tenant;

import io.github.loadup.commons.context.ContextHolder;
import io.github.loadup.commons.context.ContextKeys;
import io.github.loadup.commons.context.ExecutionContext;
import io.github.loadup.components.database.config.DatabaseProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Binds the configured default tenant to immutable execution metadata.
 *
 * <p>Request-based tenant resolution is intentionally absent while the application serves a single
 * default tenant. When dynamic schema routing is introduced, the resolver replaces this binding at
 * the same entry point.
 */
public class TenantFilter extends OncePerRequestFilter {
    private final DatabaseProperties.MultiTenant tenantProperties;

    public TenantFilter(DatabaseProperties.MultiTenant tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String attribute = ExecutionContext.class.getName();
        ExecutionContext context =
                request.getAttribute(attribute) instanceof ExecutionContext saved ? saved : ContextHolder.current();
        context = context.with(ContextKeys.TENANT_ID, tenantProperties.getDefaultTenantId());
        request.setAttribute(attribute, context);
        try {
            ContextHolder.callWith(context, () -> {
                filterChain.doFilter(request, response);
                return null;
            });
        } catch (IOException | ServletException | RuntimeException failure) {
            throw failure;
        } catch (Exception failure) {
            throw new ServletException(failure);
        }
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}
