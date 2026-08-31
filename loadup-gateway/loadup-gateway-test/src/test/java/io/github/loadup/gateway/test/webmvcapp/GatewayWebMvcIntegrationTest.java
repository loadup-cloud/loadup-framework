/*-
 * #%L
 * LoadUp Gateway Test
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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
package io.github.loadup.gateway.test.webmvcapp;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end test for the gateway MVC engine: a YAML route compiled into a
 * {@code RouterFunction} dispatches a {@code bean://} call and wraps the response.
 */
@SpringBootTest(classes = GatewayWebMvcTestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
            "loadup.gateway.storage.file.base-path=classpath:gateway-routes.yml",
            "loadup.gateway.response.wrap=true"
        })
@DisplayName("Gateway WebMVC engine")
class GatewayWebMvcIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("routes a bean:// call through the RouterFunction pipeline and wraps the response")
    void routesBeanCall() throws Exception {
        mockMvc.perform(post("/api/demo/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"loadup\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value("echo:loadup"))
                .andExpect(jsonPath("$.meta.requestId").exists());
    }

    @Test
    @DisplayName("returns 404 for unmapped routes")
    void unmappedRouteIsNotFound() throws Exception {
        mockMvc.perform(post("/api/unknown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }
}
