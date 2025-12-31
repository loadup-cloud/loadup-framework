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

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

@SpringBootTest(classes = TestConfiguration.class)
@TestPropertySource(
    properties = {"spring.application.name=test-service", "loadup.tracer.enabled=true"})
class TraceUtilTest {

  @Autowired private Tracer tracer;

  @BeforeEach
  void setUp() {
    // Clean up any existing trace context before each test
    TraceUtil.getTraceContext().clear();
  }

  @AfterEach
  void tearDown() {
    // Clean up trace context after each test to avoid interference
    TraceUtil.getTraceContext().clear();
  }

  @Test
  void testGetTracer() {
    Tracer tracer = TraceUtil.getTracer();
    assertThat(tracer).isNotNull();
  }

  @Test
  void testGetApplicationName() {
    String appName = TraceUtil.getApplicationName();
    assertThat(appName).isEqualTo("test-service");
  }

  @Test
  void testCreateSpan() {
    Span span = TraceUtil.createSpan("test-operation");
    assertThat(span).isNotNull();
    assertThat(span.getSpanContext().isValid()).isTrue();

    // Verify span is stored in context
    Span currentSpan = TraceUtil.getSpan();
    assertThat(currentSpan).isEqualTo(span);

    span.end();
  }

  @Test
  void testGetTracerId() {
    Span span = TraceUtil.createSpan("test-trace-id");
    try {
      String traceId = TraceUtil.getTracerId();
      assertThat(traceId).isNotNull();
      assertThat(traceId).hasSize(32); // TraceId should be 32 hex characters
    } finally {
      span.end();
    }
  }

  @Test
  void testTraceContext() {
    // Get the trace context and ensure it's clean
    TraceContext context = TraceUtil.getTraceContext();
    assertThat(context).isNotNull();

    // Verify context is empty at the start (should be cleaned by setUp)
    assertThat(context.isEmpty()).isTrue();
    assertThat(context.getThreadLocalSpanSize()).isEqualTo(0);

    // Create a span and verify it's stored in context
    Span span = TraceUtil.createSpan("context-test");
    assertThat(span).isNotNull();
    assertThat(context.isEmpty()).isFalse();
    assertThat(context.getThreadLocalSpanSize()).isEqualTo(1);

    // Retrieve and verify the span from context
    Span retrievedSpan = context.getCurrentSpan();
    assertThat(retrievedSpan).isNotNull();
    assertThat(retrievedSpan).isEqualTo(span);

    // End span before clearing context
    span.end();

    // Clear context and verify it's empty
    context.clear();
    assertThat(context.isEmpty()).isTrue();
    assertThat(context.getThreadLocalSpanSize()).isEqualTo(0);
  }

  @Test
  void testLogTraceId() {
    Span span = TraceUtil.createSpan("log-test");
    try {
      // Should not throw exception
      TraceUtil.logTraceId(span);
      TraceUtil.logTraceId("custom-trace-id");
      TraceUtil.clearTraceId();
    } finally {
      span.end();
    }
  }
}
