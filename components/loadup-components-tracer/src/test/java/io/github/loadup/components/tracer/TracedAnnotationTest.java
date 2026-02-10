package io.github.loadup.components.tracer;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.components.tracer.annotation.Traced;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;

/** Test for @Traced annotation functionality. */
@SpringBootTest(classes = TestConfiguration.class)
@TestPropertySource(properties = {"spring.application.name=traced-test-service", "loadup.tracer.enabled=true"})
class TracedAnnotationTest {

    @Autowired
    private TestService testService;

    @BeforeEach
    void setUp() {
        TraceUtil.getTraceContext().clear();
    }

    @Test
    void testSimpleTracedMethod() {
        String result = testService.simpleMethod("test");
        assertThat(result).isEqualTo("Processed: test");

        // Verify span was created (though it's already ended)
        // In a real test, you'd use a SpanExporter to capture spans
    }

    @Test
    void testTracedMethodWithParameters() {
        String result = testService.methodWithParams("param1", 42);
        assertThat(result).isEqualTo("param1-42");
    }

    @Test
    void testTracedMethodWithException() {
        assertThatThrownBy(() -> testService.methodThatThrows())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test exception");
    }

    @Test
    void testNestedTracedMethods() {
        String result = testService.outerMethod("test");
        assertThat(result).contains("outer").contains("inner");
    }

    @Test
    void testClassLevelTraced() {
        String result = testService.classLevelMethod();
        assertThat(result).isEqualTo("class-level");
    }

    @Service
    static class TestService {

        @Traced(name = "TestService.simpleMethod")
        public String simpleMethod(String input) {
            return "Processed: " + input;
        }

        @Traced(name = "TestService.methodWithParams", includeParameters = true, includeResult = true)
        public String methodWithParams(String param1, int param2) {
            return param1 + "-" + param2;
        }

        @Traced(name = "TestService.methodThatThrows")
        public void methodThatThrows() {
            throw new RuntimeException("Test exception");
        }

        @Traced(name = "TestService.outerMethod")
        public String outerMethod(String input) {
            return "outer-" + innerMethod(input);
        }

        @Traced(name = "TestService.innerMethod")
        private String innerMethod(String input) {
            return "inner-" + input;
        }

        @Traced
        public String classLevelMethod() {
            return "class-level";
        }
    }
}
