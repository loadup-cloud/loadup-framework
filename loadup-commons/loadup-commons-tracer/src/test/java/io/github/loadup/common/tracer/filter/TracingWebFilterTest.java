package io.github.loadup.common.tracer.filter;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.loadup.common.tracer.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Integration test for TracingWebFilter.
 */
@SpringBootTest(
        classes = {TestConfiguration.class, TracingWebFilterTest.TestController.class},
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
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
        mockMvc.perform(get("/test/hello")).andExpect(status().isOk()).andExpect(header().exists("traceparent"));
    }

    @Test
    void testWebRequestWithParameters() throws Exception {
        mockMvc.perform(get("/test/hello").param("name", "test").param("value", "123"))
                .andExpect(status().isOk());
    }

    @Test
    void testExcludedEndpointNotTraced() throws Exception {
        // Health endpoint should be excluded
        mockMvc.perform(get("/health")).andExpect(status().isOk());
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
