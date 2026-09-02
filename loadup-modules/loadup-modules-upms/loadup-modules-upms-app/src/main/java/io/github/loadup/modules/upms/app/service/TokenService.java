/*-
 * #%L
 * Loadup Modules UPMS App Layer
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
package io.github.loadup.modules.upms.app.service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.util.security.JwtSecretValidator;
import io.github.loadup.modules.upms.app.autoconfigure.UpmsSecurityProperties;
import io.github.loadup.modules.upms.client.constant.UpmsResultCode;
import java.time.Instant;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Standard Nimbus (spring-security-oauth2-jose) JWT token service.
 *
 * <p>Issues HMAC-SHA256 signed access/refresh tokens and validates refresh tokens with the
 * standard {@link JwtDecoder}. Claims follow the LoadUp contract ({@code sub}, {@code username},
 * {@code roles}, {@code permissions}) so tokens are interoperable with any standard OAuth2
 * resource server, including the LoadUp gateway.
 */
public class TokenService {

    private static final MacAlgorithm ALGORITHM = MacAlgorithm.HS256;

    private final UpmsSecurityProperties.JwtConfig jwtConfig;
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    /**
     * Creates a token service from the UPMS security properties. The secret is validated here so
     * the constructor itself never throws.
     */
    public static TokenService create(UpmsSecurityProperties securityProperties) {
        UpmsSecurityProperties.JwtConfig config = securityProperties.getJwt();
        return new TokenService(
                config,
                JwtSecretValidator.requireStrong("loadup.upms.security.jwt.secret", config.getSecret()));
    }

    private TokenService(UpmsSecurityProperties.JwtConfig jwtConfig, SecretKey secretKey) {
        this.jwtConfig = jwtConfig;
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
        this.decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(ALGORITHM)
                .build();
    }

    public String issueAccessToken(String userId, Map<String, Object> claims) {
        return issue(userId, claims, jwtConfig.getExpiration());
    }

    public String issueRefreshToken(String userId, Map<String, Object> claims) {
        long refreshTtl = jwtConfig.getRefreshExpiration() != null
                ? jwtConfig.getRefreshExpiration()
                : jwtConfig.getExpiration() * 7;
        return issue(userId, claims, refreshTtl);
    }

    /**
     * Validates a refresh token (signature + expiry) and returns its subject.
     *
     * @throws CommonException with {@link UpmsResultCode#UNAUTHORIZED} when the token is invalid
     *     or expired
     */
    public String parseRefreshToken(String refreshToken) {
        Jwt jwt;
        try {
            jwt = decoder.decode(refreshToken);
        } catch (Exception e) {
            throw new CommonException(UpmsResultCode.UNAUTHORIZED);
        }
        if (jwt == null) {
            throw new CommonException(UpmsResultCode.UNAUTHORIZED);
        }
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) {
            throw new CommonException(UpmsResultCode.UNAUTHORIZED);
        }
        if (expiresAt.isBefore(Instant.now())) {
            throw new CommonException(UpmsResultCode.UNAUTHORIZED);
        }
        return jwt.getSubject();
    }

    private String issue(String userId, Map<String, Object> claims, long ttlMillis) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claimsBuilder =
                JwtClaimsSet.builder().subject(userId).issuedAt(now).expiresAt(now.plusMillis(ttlMillis));
        claims.forEach(claimsBuilder::claim);

        JwsHeader header = JwsHeader.with(ALGORITHM).build();
        return encoder.encode(JwtEncoderParameters.from(header, claimsBuilder.build()))
                .getTokenValue();
    }

}
