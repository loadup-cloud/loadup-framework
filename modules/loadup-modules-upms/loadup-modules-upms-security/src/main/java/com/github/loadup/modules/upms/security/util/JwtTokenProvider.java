package com.github.loadup.modules.upms.security.util;

import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.modules.upms.security.core.SecurityUser;
import com.github.loadup.upms.api.dto.AuthUserDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 令牌提供者 负责 Token 的生成、解析、验证及双令牌逻辑
 *
 * @author LoadUp Framework
 */
@Slf4j
@Component
public class JwtTokenProvider {

  private static final String CLAIM_USER_JSON = "user_json";
  private static final String CLAIM_AUTHORITIES = "authorities";
  private static final String CLAIM_USER_ID = "userId";
  private static final String CLAIM_TYPE = "type";

  private static final String TYPE_ACCESS = "access";
  private static final String TYPE_REFRESH = "refresh";

  @Value(
      "${upms.security.jwt.secret:LoadUpFrameworkSecretKeyForJWTTokenGenerationMustBeLongEnough}")
  private String secret;

  @Value("${upms.security.jwt.expiration:3600000}") // 默认 1 小时
  private Long expiration;

  @Value("${upms.security.jwt.refresh-expiration:604800000}") // 默认 7 天
  private Long refreshExpiration;

  private SecretKey signingKey;

  @PostConstruct
  protected void init() {
    // 初始化加密密钥
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  /** 生成 Access Token (包含完整用户信息和权限) */
  public String createToken(SecurityUser securityUser) {
    Map<String, Object> claims = new HashMap<>();
    // 使用 JsonUtil 序列化 DTO，确保日期等格式符合 loadup-commons 规范
    claims.put(CLAIM_USER_JSON, JsonUtil.toJsonString(securityUser.getUser()));
    claims.put(CLAIM_AUTHORITIES, securityUser.getPermissions());
    claims.put(CLAIM_TYPE, TYPE_ACCESS);

    return buildToken(claims, securityUser.getUsername(), expiration);
  }

  /** 生成 Refresh Token (轻量级，仅含识别 ID) */
  public String createRefreshToken(SecurityUser securityUser) {
    Map<String, Object> claims = new HashMap<>();
    claims.put(CLAIM_USER_ID, securityUser.getUserId());
    claims.put(CLAIM_TYPE, TYPE_REFRESH);

    return buildToken(claims, securityUser.getUsername(), refreshExpiration);
  }

  /** 构建 JWT 字符串 */
  private String buildToken(Map<String, Object> claims, String subject, Long validity) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + validity);

    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(signingKey)
        .compact();
  }

  /** 解析 Access Token 并还原为 SecurityUser 对象 核心：实现请求链路的 Zero-DB-IO */
  public SecurityUser parseToken(String token) {
    try {
      Claims claims = getClaims(token);

      // 1. 解析类型校验
      if (!TYPE_ACCESS.equals(claims.get(CLAIM_TYPE))) {
        return null;
      }

      // 2. 还原 AuthUserDTO
      String userJson = claims.get(CLAIM_USER_JSON, String.class);
      AuthUserDTO authUserDTO = JsonUtil.parseObject(userJson, AuthUserDTO.class);

      // 3. 还原权限列表
      List<String> rawAuths = claims.get(CLAIM_AUTHORITIES, List.class);
      Set<String> permissions =
          new HashSet<>(rawAuths != null ? rawAuths : Collections.emptyList());

      return new SecurityUser(authUserDTO, permissions);
    } catch (Exception e) {
      log.error("JWT 解析失败: {}", e.getMessage());
      return null;
    }
  }

  /** 从 Refresh Token 中解析用户 ID */
  public String getUserIdFromToken(String token) {
    try {
      Claims claims = getClaims(token);
      if (!TYPE_REFRESH.equals(claims.get(CLAIM_TYPE))) {
        return null;
      }
      Object userId = claims.get(CLAIM_USER_ID);
      return userId != null ? userId.toString() : null;
    } catch (Exception e) {
      return null;
    }
  }

  /** 验证令牌合法性 */
  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
      return true;
    } catch (ExpiredJwtException e) {
      log.warn("JWT 令牌已过期");
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("JWT 令牌非法或签名错误: {}", e.getMessage());
    }
    return false;
  }

  /** 获取 Claims 载荷 */
  private Claims getClaims(String token) {
    return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
  }
}
