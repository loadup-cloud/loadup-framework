package io.github.loadup.components.tracer;

/*-
 * #%L
 * loadup-components-tracer
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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
