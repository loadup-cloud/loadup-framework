/*
 * #%L
 * LoadUp Web MVC
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
package io.github.loadup.components.webmvc;

import static org.assertj.core.api.Assertions.*;

import io.github.loadup.commons.context.ContextHolder;
import io.github.loadup.commons.context.ContextKeys;
import io.github.loadup.commons.context.ExecutionContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ExecutionContextFilterTest {
    @Test
    void isolatesRequestAndRestoresOuterContextEvenOnFailure() {
        var filter = new ExecutionContextFilter();
        var outer = ExecutionContext.empty().with(ContextKeys.TENANT_ID, "outer");
        ContextHolder.runWith(outer, () -> {
            assertThatThrownBy(() -> filter.doFilter(
                            new MockHttpServletRequest(), new MockHttpServletResponse(), (request, response) -> {
                                assertThat(ContextHolder.isEmpty()).isTrue();
                                assertThat(ContextHolder.isBound()).isTrue();
                                throw new ServletException("failed");
                            }))
                    .isInstanceOf(ServletException.class);
            assertThat(ContextHolder.current()).isSameAs(outer);
        });
        assertThat(ContextHolder.isBound()).isFalse();
    }

    @Test
    void redispatchUsesExplicitImmutableRequestMetadataAndOtherRequestsAreEmpty() throws Exception {
        var filter = new ExecutionContextFilter();
        var request = new MockHttpServletRequest();
        request.setAttribute(
                ExecutionContext.class.getName(), ExecutionContext.empty().with(ContextKeys.TENANT_ID, "tenant"));
        for (var dispatch : new DispatcherType[] {DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR}) {
            request.setDispatcherType(dispatch);
            filter.doFilter(request, new MockHttpServletResponse(), (r, response) -> {
                assertThat(ContextHolder.get(ContextKeys.TENANT_ID)).isEqualTo("tenant");
                ContextHolder.runWith(
                        ContextHolder.current().with(ContextKeys.TENANT_ID, "nested"),
                        () -> assertThat(ContextHolder.get(ContextKeys.TENANT_ID))
                                .isEqualTo("nested"));
            });
            assertThat(ContextHolder.isBound()).isFalse();
        }
        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (r, response) -> assertThat(ContextHolder.isEmpty()).isTrue());
    }
}
