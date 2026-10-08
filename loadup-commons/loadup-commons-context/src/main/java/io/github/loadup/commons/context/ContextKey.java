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

/** A named, runtime-checked key. Use immutable values when sharing snapshots between threads. */
public record ContextKey<T>(String name, Class<T> type) {
    public ContextKey {
        Objects.requireNonNull(name, "context key name");
        Objects.requireNonNull(type, "context key type");
        if (name.isBlank() || !name.equals(name.trim()) || type.isPrimitive()) {
            throw new IllegalArgumentException("Context keys require a nonblank name and a reference type");
        }
    }
}
