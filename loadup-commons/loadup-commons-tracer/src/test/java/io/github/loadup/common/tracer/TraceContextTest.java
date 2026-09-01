package io.github.loadup.common.tracer;

/*-
 * #%L
 * loadup-commons-tracer
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import org.junit.jupiter.api.Test;

/**
 * Test for TraceContext.
 */
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
