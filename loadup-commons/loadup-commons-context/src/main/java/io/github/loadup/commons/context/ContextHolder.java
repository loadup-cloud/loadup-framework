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
import java.util.concurrent.Callable;

/** Read-only execution metadata bound for the dynamic extent of a callback. */
public final class ContextHolder {
    private static final ScopedValue<ExecutionContext> CURRENT = ScopedValue.newInstance();

    private ContextHolder() {}

    public static ExecutionContext current() {
        return CURRENT.orElse(ExecutionContext.empty());
    }

    public static <T> T get(ContextKey<T> key) {
        return current().get(key);
    }

    public static boolean isBound() {
        return CURRENT.isBound();
    }

    public static boolean isEmpty() {
        return current().isEmpty();
    }

    public static void runWith(ExecutionContext context, Runnable action) {
        Objects.requireNonNull(context, "execution context");
        Objects.requireNonNull(action, "action");
        ScopedValue.where(CURRENT, context).run(action);
    }

    public static <T, X extends Throwable> T callWith(ExecutionContext context, ScopedValue.CallableOp<T, X> action)
            throws X {
        Objects.requireNonNull(context, "execution context");
        Objects.requireNonNull(action, "action");
        return ScopedValue.where(CURRENT, context).call(action);
    }

    /** Captures the immutable bindings now; ordinary executors do not inherit ScopedValue bindings. */
    public static Runnable wrap(Runnable action) {
        Objects.requireNonNull(action, "action");
        ExecutionContext captured = current();
        return () -> runWith(captured, action);
    }

    public static <T> Callable<T> wrapCallable(Callable<T> action) {
        Objects.requireNonNull(action, "action");
        ExecutionContext captured = current();
        return () -> callWith(captured, action::call);
    }
}
