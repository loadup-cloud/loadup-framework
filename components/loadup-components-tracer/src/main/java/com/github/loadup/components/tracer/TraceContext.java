package com.github.loadup.components.tracer;

import io.opentelemetry.api.trace.Span;

import java.util.EmptyStackException;
import java.util.Objects;

public class TraceContext {
    private final ThreadLocal<Span> threadLocal = new ThreadLocal();

    public TraceContext() {
    }

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
