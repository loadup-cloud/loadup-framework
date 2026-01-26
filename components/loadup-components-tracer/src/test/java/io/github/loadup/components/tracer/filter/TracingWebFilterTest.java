package io.github.loadup.components.tracer.filter;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.loadup.components.tracer.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Integration test for TracingWebFilter. */
@SpringBootTest(
    classes = {TestConfiguration.class, TracingWebFilterTest.TestController.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

  @Autowired private MockMvc mockMvc;

  @Test
  void testWebRequestIsTraced() throws Exception {
    mockMvc
        .perform(get("/test/hello"))
        .andExpect(status().isOk())
        .andExpect(header().exists("traceparent"));
  }

  @Test
  void testWebRequestWithParameters() throws Exception {
    mockMvc
        .perform(get("/test/hello").param("name", "test").param("value", "123"))
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
    mockMvc
        .perform(
            get("/test/hello")
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
