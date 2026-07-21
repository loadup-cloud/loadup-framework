package io.github.loadup.commons.util;

/*-
 * #%L
 * LoadUp Commons Util
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT Utility class for generating and parsing tokens.
 */
@Slf4j
public class JwtUtils {

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
