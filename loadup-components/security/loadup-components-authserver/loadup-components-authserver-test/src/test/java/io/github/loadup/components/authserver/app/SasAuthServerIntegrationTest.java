package io.github.loadup.components.authserver.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
    @DisplayName("authorization endpoint redirects browser requests to login when an API chain also exists")
    void authorizationEndpointRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/oauth2/authorize?response_type=code&client_id=loadup-app"
                                + "&redirect_uri=http://127.0.0.1:8080/login/oauth2/code/loadup"
                                + "&scope=openid&state=upms-http"
                                + "&code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"
                                + "&code_challenge_method=S256")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

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
        assertThat(jwt.getAudience()).contains("loadup-api");
        assertThat(jwt.getClaimAsString("token_use")).isEqualTo("access");
        assertThat(jwt.getClaimAsStringList("scope")).contains("openid");
    }
}
