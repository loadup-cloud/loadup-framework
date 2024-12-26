package com.github.loadup.framework.api.context;

/*-
 * #%L
 * loadup-api
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.framework.api.util.MDCUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TenantUtil {
    public static final String TENANT    = "tenant";
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
