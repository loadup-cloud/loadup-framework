package com.github.loadup.modules.upms.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT Token Provider
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Component
public class JwtTokenProvider {

  @Value(
      "${upms.security.jwt.secret:LoadUpFrameworkSecretKeyForJWTTokenGenerationMustBeLongEnough}")
  private String secret;

  @Value("${upms.security.jwt.expiration:86400000}") // 24 hours
  private Long expiration;

  @Value("${upms.security.jwt.refresh-expiration:604800000}") // 7 days
  private Long refreshExpiration;

  /** Generate access token */
  public String generateToken(String username, Long userId) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("type", "access");

    return createToken(claims, username, expiration);
  }

  /** Generate refresh token */
  public String generateRefreshToken(String username, Long userId) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("type", "refresh");

    return createToken(claims, username, refreshExpiration);
  }

  /** Create JWT token */
  private String createToken(
      Map<String, Object> claims, String subject, Long validityInMilliseconds) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .issuedAt(now)
        .expiration(validity)
        .signWith(getSigningKey())
        .compact();
  }

  /** Get signing key */
  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  /** Extract username from token */
  public String getUsernameFromToken(String token) {
    return getClaims(token).getSubject();
  }

  /** Extract user ID from token */
  public Long getUserIdFromToken(String token) {
    Claims claims = getClaims(token);
    Object userId = claims.get("userId");
    if (userId instanceof Integer) {
      return ((Integer) userId).longValue();
    }
    return (Long) userId;
  }

  /** Get all claims from token */
  private Claims getClaims(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  /** Validate token */
  public boolean validateToken(String token) {
    try {
      Claims claims = getClaims(token);
      return !claims.getExpiration().before(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  /** Check if token is expired */
  public boolean isTokenExpired(String token) {
    try {
      return getClaims(token).getExpiration().before(new Date());
    } catch (Exception e) {
      return true;
    }
  }
}
