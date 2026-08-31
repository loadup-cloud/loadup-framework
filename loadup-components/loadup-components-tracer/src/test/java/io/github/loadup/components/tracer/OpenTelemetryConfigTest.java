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

import io.github.loadup.components.tracer.config.TracerProperties;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for OpenTelemetryConfig.
 */
@SpringBootTest(classes = TestConfiguration.class)
@TestPropertySource(
        properties = {
            "spring.application.name=otel-test-service",
            "loadup.tracer.enabled=true",
            "loadup.tracer.enable-web-tracing=true",
            "loadup.tracer.enable-async-tracing=true",
            "loadup.tracer.include-headers=true",
            "loadup.tracer.include-parameters=true",
            "loadup.tracer.exclude-patterns=/test/**,/health"
        })
class OpenTelemetryConfigTest {

    @Autowired
    private OpenTelemetry openTelemetry;

    @Autowired
    private Tracer tracer;

    @Autowired
    private TracerProperties tracerProperties;

    @Test
    void testOpenTelemetryBeanCreated() {
        assertThat(openTelemetry).isNotNull();
    }

    @Test
    void testTracerBeanCreated() {
        assertThat(tracer).isNotNull();
    }

    @Test
    void testTracerProperties() {
        assertThat(tracerProperties).isNotNull();
        assertThat(tracerProperties.isEnabled()).isTrue();
        assertThat(tracerProperties.isEnableWebTracing()).isTrue();
        assertThat(tracerProperties.isEnableAsyncTracing()).isTrue();
        assertThat(tracerProperties.isIncludeHeaders()).isTrue();
        assertThat(tracerProperties.isIncludeParameters()).isTrue();
        assertThat(tracerProperties.getExcludePatterns()).isEqualTo("/test/**,/health");
    }

    @Test
    void testTracerCanCreateSpan() {
        var span = tracer.spanBuilder("test-span").startSpan();
        assertThat(span).isNotNull();
        assertThat(span.getSpanContext().isValid()).isTrue();
        span.end();
    }
}
