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

import com.alibaba.ttl.TransmittableThreadLocal;
import io.opentelemetry.api.trace.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.Setter;

public class LoadUpContext {
    private static ThreadLocal<LoadUpContext> threadLocal = new TransmittableThreadLocal<>();
    private static List<Tenant> tenantList = new ArrayList<>();
    @Getter
    @Setter
    private Map<String, Object> attributes = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private Span span;

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

    protected static List<Tenant> getTenantList() {
        return tenantList;
    }

    protected static void addTenant(Tenant tenant) {
        LoadUpContext.tenantList.add(tenant);
    }

    public LoadUpContext create() {
        LoadUpContext loadUpContext = new LoadUpContext();
        attributes.forEach((k, v) -> loadUpContext.getAttributes().put(k, v));
        loadUpContext.setSpan(null);
        return loadUpContext;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

}
