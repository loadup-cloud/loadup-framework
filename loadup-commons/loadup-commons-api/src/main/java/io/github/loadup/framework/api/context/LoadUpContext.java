package io.github.loadup.framework.api.context;

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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LoadUpContext {
    private static ThreadLocal<LoadUpContext> threadLocal = new ThreadLocal<>();
    private static List<Tenant> tenantList = new ArrayList<>();

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public static void setTenantList(List<Tenant> tenantList) {
        LoadUpContext.tenantList = tenantList;
    }

    public static ThreadLocal<LoadUpContext> getThreadLocal() {
        return threadLocal;
    }

    public static void setThreadLocal(ThreadLocal<LoadUpContext> threadLocal) {
        LoadUpContext.threadLocal = threadLocal;
    }

    private Map<String, Object> attributes = new ConcurrentHashMap<>();

    public static LoadUpContext get() {
        LoadUpContext loadUpContext = threadLocal.get();
        if (Objects.isNull(loadUpContext)) {
            loadUpContext = new LoadUpContext();
            threadLocal.set(loadUpContext);
        }
        return loadUpContext;
    }

    public static void set(LoadUpContext loadUpContext) {
        threadLocal.set(loadUpContext);
    }

    public static void clear() {
        threadLocal.remove();
    }

    public static void removeAttr(String key) {
        LoadUpContext loadUpContext = threadLocal.get();
        if (Objects.nonNull(loadUpContext)) {
            loadUpContext.getAttributes().remove(key);
        }
    }

    public static List<Tenant> getTenantList() {
        return tenantList;
    }

    protected static void addTenant(Tenant tenant) {
        LoadUpContext.tenantList.add(tenant);
    }

    public LoadUpContext create() {
        LoadUpContext loadUpContext = new LoadUpContext();
        attributes.forEach((k, v) -> loadUpContext.getAttributes().put(k, v));
        return loadUpContext;
    }
}
