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
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class ContextHolderTest {
    private static final ContextKey<String> ORDER = new ContextKey<>("payments.orderId", String.class);

    @Test
    void derivesImmutableBindingsAndReadsTypedKeys() {
        var original = ExecutionContext.empty().with(ORDER, "order-1");
        var derived = original.with(ORDER, "order-2");
        assertThat(original.get(ORDER)).isEqualTo("order-1");
        assertThat(derived.get(ORDER)).isEqualTo("order-2");
        assertThat(derived.with(ORDER, null).isEmpty()).isTrue();
        ContextHolder.runWith(original, () -> {
            assertThat(ContextHolder.isBound()).isTrue();
            assertThat(ContextHolder.get(new ContextKey<>("payments.orderId", String.class)))
                    .isEqualTo("order-1");
        });
        assertThat(ContextHolder.isBound()).isFalse();
        assertThat(original.toString()).doesNotContain("order-1");
        assertThatIllegalArgumentException().isThrownBy(() -> new ContextKey<>(" ", String.class));
        assertThatIllegalArgumentException().isThrownBy(() -> new ContextKey<>("n", int.class));
    }

    @Test
    void restoresOuterBindingAfterCheckedFailureAndEmptyScope() {
        var outer = ExecutionContext.empty().with(ORDER, "outer");
        ContextHolder.runWith(outer, () -> {
            assertThatThrownBy(() -> ContextHolder.callWith(outer.with(ORDER, "inner"), () -> {
                        assertThat(ContextHolder.get(ORDER)).isEqualTo("inner");
                        ContextHolder.runWith(
                                ExecutionContext.empty(),
                                () -> assertThat(ContextHolder.isEmpty()).isTrue());
                        assertThat(ContextHolder.get(ORDER)).isEqualTo("inner");
                        throw new IOException("failed");
                    }))
                    .isInstanceOf(IOException.class);
            assertThat(ContextHolder.current()).isSameAs(outer);
        });
        assertThat(ContextHolder.isBound()).isFalse();
    }

    @Test
    void capturesAtDecorationTimeAndRestoresAnExistingWorkerBinding() throws Exception {
        var caller = ExecutionContext.empty().with(ORDER, "caller");
        var task = ContextHolder.callWith(
                caller,
                () -> ContextHolder.wrap(() -> {
                    assertThat(ContextHolder.get(ORDER)).isEqualTo("caller");
                    throw new IllegalStateException("failed");
                }));
        var worker = ExecutionContext.empty().with(ORDER, "worker");
        try (var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> ContextHolder.runWith(worker, () -> {
                        assertThatThrownBy(task::run).isInstanceOf(IllegalStateException.class);
                        assertThat(ContextHolder.current()).isSameAs(worker);
                    }))
                    .get();
            var unbound = executor.submit(ContextHolder::isBound);
            assertThat(unbound.get()).isFalse();
            Runnable emptyTask =
                    ContextHolder.wrap(() -> assertThat(ContextHolder.isEmpty()).isTrue());
            executor.submit(() -> ContextHolder.runWith(worker, emptyTask)).get();
        }
    }

    @Test
    void isolatesVirtualThreadsAndPropagatesOnlyWrappedTasks() throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            ContextHolder.callWith(ExecutionContext.empty().with(ORDER, "caller"), () -> {
                var plain = executor.submit(() -> ContextHolder.get(ORDER));
                assertThat(plain.get()).isNull();
                var wrapped = executor.submit(ContextHolder.wrapCallable(() -> {
                    assertThat(Thread.currentThread().isVirtual()).isTrue();
                    return ContextHolder.get(ORDER);
                }));
                assertThat(wrapped.get()).isEqualTo("caller");
                var tasks = new java.util.ArrayList<java.util.concurrent.Future<String>>();
                for (int i = 0; i < 100; i++) {
                    String id = "order-" + i;
                    tasks.add(executor.submit(() ->
                            ContextHolder.callWith(ExecutionContext.empty().with(ORDER, id), () -> {
                                Thread.sleep(1);
                                return ContextHolder.get(ORDER);
                            })));
                }
                for (int i = 0; i < tasks.size(); i++)
                    assertThat(tasks.get(i).get()).isEqualTo("order-" + i);
                assertThat(ContextHolder.get(ORDER)).isEqualTo("caller");
                return null;
            });
        }
        assertThat(ContextHolder.isBound()).isFalse();
    }
}
