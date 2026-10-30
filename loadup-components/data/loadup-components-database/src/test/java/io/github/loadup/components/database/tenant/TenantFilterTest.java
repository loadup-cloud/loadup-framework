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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.commons.context.ContextHolder;
import io.github.loadup.commons.context.ContextKey;
import io.github.loadup.commons.context.ExecutionContext;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.database.config.DatabaseProperties;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TenantFilterTest {
    @Test
    void bindsDefaultTenantAndPreservesRequestMetadataAcrossRedispatch() throws Exception {
        var properties = new DatabaseProperties.MultiTenant();
        properties.setDefaultTenantId("single-tenant");
        var filter = new TenantFilter(properties);
        var request = new MockHttpServletRequest();
        var key = new ContextKey<>("orders.id", String.class);
        request.setAttribute(
                ExecutionContext.class.getName(), ExecutionContext.empty().with(key, "order"));
        filter.doFilter(request, new MockHttpServletResponse(), (r, response) -> {
            assertThat(TenantUtil.getTenantId()).isEqualTo("single-tenant");
            assertThat(ContextHolder.get(key)).isEqualTo("order");
        });
        assertThat(ContextHolder.isBound()).isFalse();
        request.setDispatcherType(DispatcherType.ASYNC);
        filter.doFilter(
                request,
                new MockHttpServletResponse(),
                (r, response) -> assertThat(TenantUtil.getTenantId()).isEqualTo("single-tenant"));
        assertThat(ContextHolder.isBound()).isFalse();
    }

    @Test
    void failureRestoresOuterBinding() {
        var properties = new DatabaseProperties.MultiTenant();
        properties.setDefaultTenantId("single-tenant");
        var request = new MockHttpServletRequest();
        TenantUtil.runWithTenant("outer", () -> {
            assertThatThrownBy(() -> new TenantFilter(properties)
                            .doFilter(request, new MockHttpServletResponse(), (r, response) -> {
                                throw new ServletException("failed");
                            }))
                    .isInstanceOf(ServletException.class);
            assertThat(TenantUtil.getTenantId()).isEqualTo("outer");
        });
        assertThat(ContextHolder.isBound()).isFalse();
    }

    @Test
    void requestHeadersCannotOverrideTheConfiguredTenant() throws Exception {
        var properties = new DatabaseProperties.MultiTenant();
        properties.setDefaultTenantId("single-tenant");
        var request = new MockHttpServletRequest();
        request.addHeader("X-Tenant-Id", "other-tenant");

        new TenantFilter(properties)
                .doFilter(
                        request,
                        new MockHttpServletResponse(),
                        (r, response) -> assertThat(TenantUtil.getTenantId()).isEqualTo("single-tenant"));

        assertThat(ContextHolder.isBound()).isFalse();
    }

    @Test
    void errorRedispatchRetainsSavedExecutionMetadata() throws Exception {
        var properties = new DatabaseProperties.MultiTenant();
        properties.setDefaultTenantId("single-tenant");
        var request = new MockHttpServletRequest();
        request.setDispatcherType(DispatcherType.ERROR);
        request.setAttribute(org.springframework.web.util.WebUtils.ERROR_REQUEST_URI_ATTRIBUTE, "/api/orders");
        var key = new ContextKey<>("errors.path", String.class);
        request.setAttribute(
                ExecutionContext.class.getName(), ExecutionContext.empty().with(key, "/api/orders"));
        new TenantFilter(properties).doFilter(request, new MockHttpServletResponse(), (r, response) -> {
            assertThat(TenantUtil.getTenantId()).isEqualTo("single-tenant");
            assertThat(ContextHolder.get(key)).isEqualTo("/api/orders");
        });
        assertThat(ContextHolder.isBound()).isFalse();
    }

    @Test
    void ioAndRuntimeFailuresPropagateWithoutLeakingContext() {
        var properties = new DatabaseProperties.MultiTenant();
        var filter = new TenantFilter(properties);
        var io = new java.io.IOException("synthetic IO failure");
        assertThatThrownBy(
                        () -> filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), (r, s) -> {
                            throw io;
                        }))
                .isSameAs(io);
        var runtime = new IllegalStateException("synthetic runtime failure");
        assertThatThrownBy(
                        () -> filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), (r, s) -> {
                            throw runtime;
                        }))
                .isSameAs(runtime);
        assertThat(ContextHolder.isBound()).isFalse();
    }
}
