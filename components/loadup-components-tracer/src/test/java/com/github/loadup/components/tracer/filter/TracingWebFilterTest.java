package com.github.loadup.components.tracer.filter;

import com.github.loadup.components.tracer.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for TracingWebFilter.
 */
@SpringBootTest(
    classes = {TestConfiguration.class, TracingWebFilterTest.TestController.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.application.name=web-filter-test",
    "loadup.tracer.enabled=true",
    "loadup.tracer.enable-web-tracing=true",
    "loadup.tracer.include-parameters=true",
    "loadup.tracer.exclude-patterns=/actuator/**,/health"
})
class TracingWebFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testWebRequestIsTraced() throws Exception {
        mockMvc.perform(get("/test/hello"))
            .andExpect(status().isOk())
            .andExpect(header().exists("traceparent"));
    }

    @Test
    void testWebRequestWithParameters() throws Exception {
        mockMvc.perform(get("/test/hello")
                .param("name", "test")
                .param("value", "123"))
            .andExpect(status().isOk());
    }

    @Test
    void testExcludedEndpointNotTraced() throws Exception {
        // Health endpoint should be excluded
        mockMvc.perform(get("/health"))
            .andExpect(status().isOk());
    }

    @Test
    void testTraceContextPropagation() throws Exception {
        // Send request with traceparent header
        mockMvc.perform(get("/test/hello")
                .header("traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01"))
            .andExpect(status().isOk())
            .andExpect(header().exists("traceparent"));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/hello")
        public String hello() {
            return "Hello";
        }

        @GetMapping("/health")
        public String health() {
            return "OK";
        }
    }
}

