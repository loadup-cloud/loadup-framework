package io.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-api
 * %%
 * Copyright (C) 2025 LoadUp Cloud
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

import io.github.loadup.framework.api.context.LoadUpContext;
import io.github.loadup.framework.api.context.Tenant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TenantUtil {
    public static final String TENANT = "tenant";
    public static final String TENANT_ID = "tenantId";

    public static Tenant getTenant() {
        return null;
    }

    public static void setTenant(Tenant tenant) {
        LoadUpContext antfinContext = LoadUpContext.get();
        antfinContext.getAttributes().put(TENANT, (tenant));
        MDCUtils.logTenantId(tenant.getTenantId());
    }

    public static String getTenantId() {
        Tenant tenant = getTenant();
        if (Objects.isNull(tenant)) {
            return null;
        }
        return tenant.getTenantId();
    }

    public static void setTenantId(String tenantId) {
        Tenant tenant = getTenant();
        if (Objects.isNull(tenant)) {
            tenant = new Tenant();
        }
        tenant.setTenantId(tenantId);
    }

    public static void putTenantAttribute(String key, String value) {
        Tenant tenant = getTenant();
        if (tenant == null) {
            tenant = new Tenant();
        }
        tenant.getAttributes().put(key, value);
        LoadUpContext antfinContext = LoadUpContext.get();
        antfinContext.getAttributes().put(TENANT, (tenant));
    }

    public static List<Tenant> getAllTenants() {
        return Collections.unmodifiableList(LoadUpContext.getTenantList());
    }

    public static String getCurrentTenantId() {
        return null;
    }

    public static String getClientIdByTenantId(String tenantId) {
        return null;
    }
}
