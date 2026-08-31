package io.github.loadup.components.database.interceptor;

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

import com.mybatisflex.core.tenant.TenantManager;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.tenant.TenantContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class TenantInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(TenantInterceptor.class);

    private final DatabaseProperties databaseProperties;
    private final Set<String> ignoreTables;

    /**
     * Initialize tenant interceptor
     */
    public void init() {
        DatabaseProperties.MultiTenant config = databaseProperties.getMultiTenant();

        // Parse ignore tables
        if (config.getIgnoreTables() != null && !config.getIgnoreTables().isBlank()) {
            String[] tables = config.getIgnoreTables().split(",");
            Arrays.stream(tables).map(String::trim).forEach(ignoreTables::add);
        }

        // Configure MyBatis-Flex TenantManager
        TenantManager.setTenantFactory(() -> {
            String tenantId = TenantContextHolder.getTenantId();
            if (tenantId == null) {
                tenantId = config.getDefaultTenantId();
            }
            return new Object[] {tenantId};
        });

        log.info("Initialized TenantInterceptor with column={}, ignoreTables={}", config.getColumnName(), ignoreTables);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    public TenantInterceptor(DatabaseProperties databaseProperties, Set<String> ignoreTables) {
        this.databaseProperties = databaseProperties;
        this.ignoreTables = ignoreTables;
    }
}
