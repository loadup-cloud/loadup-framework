/*
 * #%L
 * LoadUp Lock
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
package io.github.loadup.components.lock;

/** Synchronous critical sections. Acquire, callback and release all run on the caller thread. */
public interface LockTemplate {
    <T, X extends Throwable> T execute(LockKey key, ScopedValue.CallableOp<T, X> action) throws X, InterruptedException;

    <T, X extends Throwable> T execute(LockKey key, LockOptions options, ScopedValue.CallableOp<T, X> action)
            throws X, InterruptedException;

    default void run(LockKey key, Runnable action) throws InterruptedException {
        java.util.Objects.requireNonNull(action, "action");
        execute(key, () -> {
            action.run();
            return null;
        });
    }

    default void run(LockKey key, LockOptions options, Runnable action) throws InterruptedException {
        java.util.Objects.requireNonNull(action, "action");
        execute(key, options, () -> {
            action.run();
            return null;
        });
    }
}
