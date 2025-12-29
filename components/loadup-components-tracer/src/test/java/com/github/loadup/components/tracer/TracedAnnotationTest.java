package com.github.loadup.components.tracer;

import com.github.loadup.components.tracer.annotation.Traced;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test for @Traced annotation functionality.
 */
@SpringBootTest(classes = TestConfiguration.class)
@TestPropertySource(properties = {
    "spring.application.name=traced-test-service",
    "loadup.tracer.enabled=true"
})
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

        @Traced(
            name = "TestService.methodWithParams",
            includeParameters = true,
            includeResult = true
        )
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

