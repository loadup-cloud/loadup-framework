package io.github.loadup.framework.api.context;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
