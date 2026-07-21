package io.github.loadup.components.tracer;

/*-
 * #%L
 * Loadup Components Tracer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
