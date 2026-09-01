/*-
 * #%L
 * Loadup UPMS Test
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
package io.github.loadup.modules.upms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.upms.app.autoconfigure.UpmsSecurityProperties;
import io.github.loadup.modules.upms.app.service.TokenService;
import io.github.loadup.modules.upms.client.constant.UpmsResultCode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@DisplayName("TokenService")
class TokenServiceTest {

    private static final String SECRET = "loadup-secret-key-change-in-production";

    private final UpmsSecurityProperties properties = new UpmsSecurityProperties();

    private final TokenService tokenService = TokenService.create(properties);

    @Test
    @DisplayName("issues an access token carrying the LoadUp claims contract")
    void issuesAccessTokenWithClaimsContract() {
        Map<String, Object> claims = Map.of(
                "username", "admin",
                "roles", List.of("ADMIN", "USER"),
                "permissions", List.of("user:read", "user:write"));

        String token = tokenService.issueAccessToken("u-1", claims);

        Jwt jwt = decode(token);
        assertThat(jwt.getSubject()).isEqualTo("u-1");
        assertThat(jwt.getClaimAsString("username")).isEqualTo("admin");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ADMIN", "USER");
        assertThat(jwt.getClaimAsStringList("permissions")).containsExactly("user:read", "user:write");
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("parses a valid refresh token and returns its subject")
    void parsesValidRefreshToken() {
        String refreshToken = tokenService.issueRefreshToken("u-2", Map.of("username", "alice"));

        assertThat(tokenService.parseRefreshToken(refreshToken)).isEqualTo("u-2");
    }

    @Test
    @DisplayName("rejects an expired refresh token")
    void rejectsExpiredRefreshToken() throws Exception {
        String expired = expiredToken("u-3");

        assertThatThrownBy(() -> tokenService.parseRefreshToken(expired))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getResultCode())
                .isEqualTo(UpmsResultCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("rejects a token with a tampered signature")
    void rejectsTamperedToken() {
        String refreshToken = tokenService.issueRefreshToken("u-4", Map.of());
        String tampered =
                refreshToken.substring(0, refreshToken.length() - 2) + (refreshToken.endsWith("==") ? "AA" : "ab");

        assertThatThrownBy(() -> tokenService.parseRefreshToken(tampered))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getResultCode())
                .isEqualTo(UpmsResultCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("rejects a secret shorter than 32 bytes")
    void rejectsShortSecret() {
        UpmsSecurityProperties shortSecret = new UpmsSecurityProperties();
        shortSecret.getJwt().setSecret("too-short");

        assertThatThrownBy(() -> TokenService.create(shortSecret))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    private Jwt decode(String token) {
        SecretKey key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build()
                .decode(token);
    }

    /**
     * Builds a signature-valid HS256 token whose {@code exp} lies in the past, which the standard
     * Nimbus encoder refuses to produce.
     */
    private static String expiredToken(String subject) throws Exception {
        long past = Instant.now().minusSeconds(60).getEpochSecond();
        JWSObject jws = new JWSObject(
                new JWSHeader(JWSAlgorithm.HS256), new Payload("{\"sub\":\"" + subject + "\",\"exp\":" + past + "}"));
        jws.sign(new MACSigner(SECRET.getBytes(StandardCharsets.UTF_8)));
        return jws.serialize();
    }
}
