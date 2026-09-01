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

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
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

    @Test
    @DisplayName("rejects protected routes without a bearer token")
    void secureRouteWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/demo/secure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"loadup\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("routes a bean call with a valid bearer token through the pipeline")
    void secureRouteAcceptsValidToken() throws Exception {
        mockMvc.perform(post("/api/demo/secure")
                        .header("Authorization", "Bearer " + signedToken(List.of("user:read")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"loadup\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value("echo:loadup"));
    }

    @Test
    @DisplayName("method-level @PreAuthorize sees gateway-issued authorities")
    void methodSecurityUsesGatewayAuthentication() throws Exception {
        mockMvc.perform(post("/api/demo/protected")
                        .header("Authorization", "Bearer " + signedToken(List.of("user:write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"loadup\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value("secure:loadup"));
    }

    @Test
    @DisplayName("route-level authorize accepts a token carrying the required permission")
    void authorizedRouteAcceptsPermission() throws Exception {
        mockMvc.perform(post("/api/demo/authorized")
                        .header("Authorization", "Bearer " + signedToken(List.of("user:write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"loadup\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value("SUCCESS"));
    }

    @Test
    @DisplayName("route-level authorize rejects a token without the required permission with 403")
    void authorizedRouteRejectsMissingPermission() throws Exception {
        mockMvc.perform(post("/api/demo/authorized")
                        .header("Authorization", "Bearer " + signedToken(List.of("user:read")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"loadup\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("route-level authorize without a token returns 401")
    void authorizedRouteWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/demo/authorized")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"loadup\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("route-level authorize supports full SpEL expressions")
    void authorizedRouteSupportsSpel() throws Exception {
        mockMvc.perform(post("/api/demo/authorized-role")
                        .header("Authorization", "Bearer " + signedToken(List.of("user:read"), List.of("ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"loadup\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value("SUCCESS"));
    }

    @Test
    @DisplayName("route-level SpEL rejects a token with the wrong role with 403")
    void authorizedRouteSpelRejectsWrongRole() throws Exception {
        mockMvc.perform(post("/api/demo/authorized-role")
                        .header("Authorization", "Bearer " + signedToken(List.of("user:read"), List.of("USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"loadup\"}"))
                .andExpect(status().isForbidden());
    }

    /**
     * Signs a JWT with the same HMAC secret the gateway resource server is configured with
     * (default {@code loadup.gateway.security.secret}), carrying the LoadUp claims contract.
     */
    private static String signedToken() {
        return signedToken(List.of("user:read"));
    }

    private static String signedToken(List<String> permissions) {
        return signedToken(permissions, List.of("ADMIN"));
    }

    private static String signedToken(List<String> permissions, List<String> roles) {
        String secret = "loadup-gateway-secret-key-must-be-long-enough-32bytes";
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("loadup")
                .subject("u-1")
                .claim("username", "admin")
                .claim("roles", roles)
                .claim("permissions", permissions)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
