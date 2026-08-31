package io.github.loadup.components.tracer;

/*-
 * #%L
 * loadup-components-tracer
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
import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = TestConfiguration.class)
@TestPropertySource(properties = {"spring.application.name=test-service", "loadup.tracer.enabled=true"})
class TraceUtilTest {

    @Autowired
    private Tracer tracer;

    @BeforeEach
    void setUp() {
        // Clean up any existing trace context before each test
        TraceUtil.clearContext();
    }

    @AfterEach
    void tearDown() {
        // Clean up trace context after each test to avoid interference
        TraceUtil.clearContext();
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
        // Verify context is empty at the start (should be cleaned by setUp)
        assertThat(TraceUtil.getSpan()).isNull();

        // Create a span and verify it's stored in context
        Span span = TraceUtil.createSpan("context-test");
        assertThat(span).isNotNull();
        assertThat(TraceUtil.getSpan()).isEqualTo(span);

        // Retrieve and verify the span from context
        assertThat(TraceUtil.getSpan()).isEqualTo(span);

        // End span before clearing context
        span.end();

        // Clear context and verify it's empty
        TraceUtil.clearContext();
        assertThat(TraceUtil.getSpan()).isNull();
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
