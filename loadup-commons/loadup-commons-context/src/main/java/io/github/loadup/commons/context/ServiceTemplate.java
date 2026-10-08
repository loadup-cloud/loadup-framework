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

import java.util.Objects;

/** Optional application-entry lifecycle; transaction, authorization and observation remain external. */
public final class ServiceTemplate {
    private static final Lifecycle NOOP = new Lifecycle() {};

    private ServiceTemplate() {}

    public interface Lifecycle {
        default void init() {}

        default void clean() {}
    }

    public static <T, X extends Throwable> T execute(ScopedValue.CallableOp<T, X> action) throws X {
        return execute(ContextHolder.current(), NOOP, action);
    }

    public static <T, X extends Throwable> T execute(ExecutionContext context, ScopedValue.CallableOp<T, X> action)
            throws X {
        return execute(context, NOOP, action);
    }

    /** Cleanup also runs on partial initialization failure and cannot hide a primary exception. */
    public static <T, X extends Throwable> T execute(
            ExecutionContext context, Lifecycle lifecycle, ScopedValue.CallableOp<T, X> action) throws X {
        Objects.requireNonNull(lifecycle, "lifecycle");
        Objects.requireNonNull(action, "action");
        return ContextHolder.callWith(context, () -> {
            try (Cleanup cleanup = new Cleanup(lifecycle)) {
                lifecycle.init();
                return action.call();
            }
        });
    }

    public static void run(Runnable action) {
        run(ContextHolder.current(), NOOP, action);
    }

    public static void run(ExecutionContext context, Runnable action) {
        run(context, NOOP, action);
    }

    public static void run(ExecutionContext context, Lifecycle lifecycle, Runnable action) {
        Objects.requireNonNull(action, "action");
        execute(context, lifecycle, () -> {
            action.run();
            return null;
        });
    }

    private record Cleanup(Lifecycle lifecycle) implements AutoCloseable {
        @Override
        public void close() {
            lifecycle.clean();
        }
    }
}
