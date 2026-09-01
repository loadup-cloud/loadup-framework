package io.github.loadup.components.authserver.app;

/*-
 * #%L
 * LoadUp Components AuthServer Test
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end test: the embedded Spring Authorization Server exposes the standard token endpoint
 * and returns a LoadUp JWT that can be verified with the configured JWK source.
 */
@SpringBootTest(classes = AuthServerTestApplication.class)
@AutoConfigureMockMvc
@DisplayName("SAS binder end-to-end")
class SasAuthServerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("/oauth2/token issues a verifiable access token for client credentials")
    void tokenEndpointIssuesVerifiableToken() throws Exception {
        String authorization =
                "Basic " + Base64.getEncoder().encodeToString("loadup-app:change-me".getBytes(StandardCharsets.UTF_8));

        String responseBody = mockMvc.perform(post("/oauth2/token")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials")
                        .param("scope", "openid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = JsonPath.read(responseBody, "$.access_token");

        // Verify signature with the decoder assembled from the authorization server JWK source.
        Jwt jwt = jwtDecoder.decode(accessToken);
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("http://localhost:8080");
        assertThat(jwt.getSubject()).isEqualTo("loadup-app");
        assertThat(jwt.getClaimAsStringList("scope")).contains("openid");
    }
}
