package io.github.loadup.commons.util;

/*-
 * #%L
 * LoadUp Commons Util
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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JWT Utility class for generating and parsing tokens.
 */
public class JwtUtils {
    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    /**
     * Create a new JWT Token
     *
     * @param subject   User ID or Subject
     * @param claims    Additional Claims
     * @param secret    Secret Key (must be at least 256 bits/32 chars)
     * @param ttlMillis Time to Live in milliseconds
     * @return Signed JWT String
     */
    public static String createToken(String subject, Map<String, Object> claims, String secret, long ttlMillis) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        long nowString = System.currentTimeMillis();

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(nowString))
                .expiration(new Date(nowString + ttlMillis))
                .signWith(key)
                .compact();
    }

    /**
     * Parse and validate a token
     *
     * @param token  JWT Token
     * @param secret Secret Key
     * @return Claims object
     */
    public static Claims parseToken(String token, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    /**
     * Check if token is expired
     */
    public static boolean isExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
