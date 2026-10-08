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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Immutable bindings; values must themselves be immutable when shared between threads. */
public final class ExecutionContext {
    private static final ExecutionContext EMPTY = new ExecutionContext(Map.of());
    private final Map<ContextKey<?>, Object> values;

    private ExecutionContext(Map<ContextKey<?>, Object> values) {
        this.values = Map.copyOf(values);
    }

    public static ExecutionContext empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public <T> T get(ContextKey<T> key) {
        Objects.requireNonNull(key, "context key");
        return key.type().cast(values.get(key));
    }

    /** Creates new bindings without changing this context. A null value removes the key in the copy. */
    public <T> ExecutionContext with(ContextKey<T> key, T value) {
        Objects.requireNonNull(key, "context key");
        var copy = new HashMap<>(values);
        if (value == null) copy.remove(key);
        else copy.put(key, key.type().cast(value));
        return copy.isEmpty() ? EMPTY : new ExecutionContext(copy);
    }

    @Override
    public String toString() {
        return "ExecutionContext[size=" + values.size() + "]";
    }
}
