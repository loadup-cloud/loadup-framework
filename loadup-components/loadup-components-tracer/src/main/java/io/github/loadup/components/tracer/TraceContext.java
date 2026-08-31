package io.github.loadup.components.tracer;

/*-
 * #%L
 * Loadup Components Tracer
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

import io.opentelemetry.api.trace.Span;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Thread-local span stack that tracks the active span chain for the current thread.
 *
 * <p>Each thread maintains an independent stack. Spans are pushed on creation and
 * popped on completion, so nested spans work correctly. The stack is bounded only by
 * call depth – callers must always balance push/pop to avoid leaks.
 */
public class TraceContext {

    private final ThreadLocal<Deque<Span>> spanStack = ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * Pushes a span onto the stack. No-op when {@code span} is null.
     *
     * @param span the span to push
     */
    public void push(Span span) {
        if (span != null) {
            spanStack.get().push(span);
        }
    }

    /**
     * Pops and returns the top span from the stack.
     *
     * @return the top span, or {@code null} if the stack is empty
     */
    public Span pop() {
        Deque<Span> stack = spanStack.get();
        if (stack.isEmpty()) {
            return null;
        }
        Span span = stack.pop();
        // Clean up the ThreadLocal when the stack becomes empty to prevent leaks.
        if (stack.isEmpty()) {
            spanStack.remove();
        }
        return span;
    }

    /**
     * Returns the top span without removing it.
     *
     * @return the current span, or {@code null} if the stack is empty
     */
    public Span getCurrentSpan() {
        Deque<Span> stack = spanStack.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    /**
     * Returns {@code true} when no spans are on the stack.
     */
    public boolean isEmpty() {
        return spanStack.get().isEmpty();
    }

    /**
     * Returns the number of spans currently on the stack for the current thread.
     */
    public int getThreadLocalSpanSize() {
        return spanStack.get().size();
    }

    /**
     * Clears all spans and removes the ThreadLocal entry.
     * Typically called in test teardown or at the end of a request lifecycle.
     */
    public void clear() {
        spanStack.get().clear();
        spanStack.remove();
    }
}
