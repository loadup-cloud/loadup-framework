package io.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-api
 * %%
 * Copyright (C) 2025 LoadUp Cloud
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

import io.github.loadup.framework.api.context.LoadUpContext;
import io.github.loadup.framework.api.context.Tenant;
import java.util.*;

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
