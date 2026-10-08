/*
 * #%L
 * LoadUp Common Context
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
package io.github.loadup.commons.context;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ServiceTemplateTest {
    @Test
    void lifecycleRunsInOrderInsideBoundContext() {
        var events = new ArrayList<String>();
        var context = ExecutionContext.empty().with(ContextKeys.TENANT_ID, "tenant");
        var lifecycle = new ServiceTemplate.Lifecycle() {
            public void init() {
                assertThat(ContextHolder.current()).isSameAs(context);
                events.add("init");
            }

            public void clean() {
                assertThat(ContextHolder.current()).isSameAs(context);
                events.add("clean");
            }
        };
        String result = ServiceTemplate.execute(context, lifecycle, () -> {
            events.add("work");
            return "done";
        });
        assertThat(result).isEqualTo("done");
        assertThat(events).containsExactly("init", "work", "clean");
        assertThat(ContextHolder.isBound()).isFalse();
    }

    @Test
    void cleanupFailureIsSuppressedAndCheckedBusinessFailureIsPreserved() {
        var primary = new IOException("business");
        var cleanup = new IllegalStateException("cleanup");
        var lifecycle = new ServiceTemplate.Lifecycle() {
            public void clean() {
                throw cleanup;
            }
        };
        assertThatThrownBy(() -> ServiceTemplate.execute(ExecutionContext.empty(), lifecycle, () -> {
                    throw primary;
                }))
                .isSameAs(primary)
                .hasSuppressedException(cleanup);
        assertThat(ContextHolder.isBound()).isFalse();
    }

    @Test
    void cleansPartialInitializationWithoutRunningBusinessAndRestoresOuterContext() {
        var events = new ArrayList<String>();
        var primary = new IllegalArgumentException("init");
        var lifecycle = new ServiceTemplate.Lifecycle() {
            public void init() {
                events.add("init");
                throw primary;
            }

            public void clean() {
                events.add("clean");
            }
        };
        var outer = ExecutionContext.empty().with(ContextKeys.TENANT_ID, "outer");
        ContextHolder.runWith(outer, () -> {
            assertThatThrownBy(() -> ServiceTemplate.run(ExecutionContext.empty(), lifecycle, () -> events.add("work")))
                    .isSameAs(primary);
            assertThat(ContextHolder.current()).isSameAs(outer);
        });
        assertThat(events).isEqualTo(List.of("init", "clean"));
    }
}
