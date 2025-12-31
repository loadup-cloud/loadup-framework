package com.github.loadup.components.tracer;

/*-
 * #%L
 * loadup-components-tracer
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import org.junit.jupiter.api.Test;

/** Test for TraceContext. */
class TraceContextTest {

  @Test
  void testPushAndPop() {
    TraceContext context = new TraceContext();
    assertThat(context.isEmpty()).isTrue();

    Span span = Span.getInvalid();
    context.push(span);

    assertThat(context.isEmpty()).isFalse();
    assertThat(context.getThreadLocalSpanSize()).isEqualTo(1);

    Span retrieved = context.pop();
    assertThat(retrieved).isEqualTo(span);
    assertThat(context.isEmpty()).isTrue();
  }

  @Test
  void testGetCurrentSpan() {
    TraceContext context = new TraceContext();

    Span nullSpan = context.getCurrentSpan();
    assertThat(nullSpan).isNull();

    Span span = Span.getInvalid();
    context.push(span);

    Span current = context.getCurrentSpan();
    assertThat(current).isEqualTo(span);
  }

  @Test
  void testClear() {
    TraceContext context = new TraceContext();
    Span span = Span.getInvalid();

    context.push(span);
    assertThat(context.isEmpty()).isFalse();

    context.clear();
    assertThat(context.isEmpty()).isTrue();
  }

  @Test
  void testPushNull() {
    TraceContext context = new TraceContext();
    context.push(null);

    // Pushing null should not change state
    assertThat(context.isEmpty()).isTrue();
  }

  @Test
  void testPopEmpty() {
    TraceContext context = new TraceContext();
    Span span = context.pop();

    assertThat(span).isNull();
  }
}
