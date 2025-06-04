/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.tracer;

/*-
 * #%L
 * loadup-components-tracer
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import io.opentelemetry.api.trace.Span;
import java.util.EmptyStackException;
import java.util.Objects;

public class TraceContext {
    private final ThreadLocal<Span> threadLocal = new ThreadLocal();

    public TraceContext() {}

    public void push(Span span) {
        if (span != null) {
            this.threadLocal.set(span);
        }
    }

    public Span getCurrentSpan() throws EmptyStackException {
        return this.isEmpty() ? null : this.threadLocal.get();
    }

    public Span pop() throws EmptyStackException {
        if (this.isEmpty()) {
            return null;
        } else {
            Span span = this.threadLocal.get();
            this.clear();
            return span;
        }
    }

    public int getThreadLocalSpanSize() {
        return isEmpty() ? 0 : 1;
    }

    public boolean isEmpty() {
        return Objects.isNull(this.threadLocal.get());
    }

    public void clear() {
        this.threadLocal.remove();
    }
}
