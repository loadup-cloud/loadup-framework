/*
 * #%L
 * Loadup Components Extension
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
package io.github.loadup.components.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.loadup.components.extension.api.BizIdentity;
import io.github.loadup.components.extension.context.BizContextHolder;
import io.github.loadup.components.extension.core.BizScenario;
import io.github.loadup.components.extension.interceptor.BizScenarioInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class BizScenarioInterceptorTest {
    private final BizScenarioInterceptor interceptor = new BizScenarioInterceptor();

    @AfterEach
    void clearContext() {
        BizContextHolder.clear();
    }

    @Test
    void explicitAnnotationOverridesIdentityOnlyForInvocationScope() throws Throwable {
        BizScenario expected = BizScenario.valueOf("retail", "checkout", "mobile");
        var call = invocation("explicit", new Object[] {(BizIdentity) () -> "ignored"});
        when(call.proceed()).thenAnswer(ignored -> {
            assertThat(BizContextHolder.get()).isEqualTo(expected);
            return "result";
        });
        assertThat(interceptor.invoke(call)).isEqualTo("result");
        assertThat(BizContextHolder.get()).isNull();
    }

    @Test
    void identityIsResolvedAfterUnrelatedAndNullArguments() throws Throwable {
        BizIdentity identity = new BizIdentity() {
            public String getBizCode() {
                return "trade";
            }

            public String getUseCase() {
                return "refund";
            }

            public String getScenario() {
                return "online";
            }
        };
        var call = invocation("dynamic", new Object[] {null, "unrelated", identity});
        when(call.proceed()).thenAnswer(ignored -> BizContextHolder.get());
        assertThat(interceptor.invoke(call)).isEqualTo(BizScenario.valueOf("trade", "refund", "online"));
        assertThat(BizContextHolder.get()).isNull();
    }

    @Test
    void unannotatedAndUnresolvedCallsPreserveExistingContext() throws Throwable {
        var outer = BizScenario.valueOf("outer");
        BizContextHolder.set(outer);
        for (String method : new String[] {"plain", "dynamic"}) {
            for (Object[] args : new Object[][] {null, new Object[0], new Object[] {null, "unrelated"}}) {
                var call = invocation(method, args);
                when(call.proceed()).thenAnswer(ignored -> BizContextHolder.get());
                assertThat(interceptor.invoke(call)).isSameAs(outer);
                assertThat(BizContextHolder.get()).isSameAs(outer);
            }
        }
    }

    @Test
    void nestedInvocationRestoresOuterScopeOnSuccessAndFailure() throws Throwable {
        var outer = BizScenario.valueOf("outer");
        BizContextHolder.set(outer);
        var inner = invocation("explicit", new Object[0]);
        when(inner.proceed()).thenAnswer(ignored -> BizContextHolder.get());
        assertThat(interceptor.invoke(inner)).isEqualTo(BizScenario.valueOf("retail", "checkout", "mobile"));
        assertThat(BizContextHolder.get()).isSameAs(outer);
        var failure = new IllegalStateException("synthetic failure");
        when(inner.proceed()).thenThrow(failure);
        assertThatThrownBy(() -> interceptor.invoke(inner)).isSameAs(failure);
        assertThat(BizContextHolder.get()).isSameAs(outer);
    }

    @Test
    void entryFailureDoesNotLeakContextAndDefaultsRemainExplicit() throws Throwable {
        var call = invocation("dynamic", new Object[] {(BizIdentity) () -> "trade"});
        var failure = new IllegalStateException("synthetic failure");
        when(call.proceed()).thenAnswer(ignored -> {
            assertThat(BizContextHolder.get()).isEqualTo(BizScenario.valueOf("trade", "default", "default"));
            throw failure;
        });
        assertThatThrownBy(() -> interceptor.invoke(call)).isSameAs(failure);
        assertThat(BizContextHolder.get()).isNull();
        var fallback = BizScenario.valueOf("fallback", "checkout");
        assertThat(BizContextHolder.getOrDefault(fallback)).isSameAs(fallback);
        BizContextHolder.set(BizScenario.valueOf("trade"));
        assertThat(BizContextHolder.getOrDefault(fallback).getUniqueIdentity())
                .isEqualTo("trade.#defaultUseCase#.#defaultScenario#");
    }

    private static MethodInvocation invocation(String method, Object[] args) throws Exception {
        var call = mock(MethodInvocation.class);
        when(call.getMethod()).thenReturn(Endpoints.class.getMethod(method));
        when(call.getArguments()).thenReturn(args);
        return call;
    }

    public static class Endpoints {
        @io.github.loadup.components.extension.annotation.BizScenario(
                bizCode = "retail",
                useCase = "checkout",
                scenario = "mobile")
        public void explicit() {}

        @io.github.loadup.components.extension.annotation.BizScenario
        public void dynamic() {}

        public void plain() {}
    }
}
