package com.github.loadup.modules.upms.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Security Configuration Properties
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "upms.security")
public class SecurityProperties {

  /** JWT configuration */
  private JwtConfig jwt = new JwtConfig();

  /** Login configuration */
  private LoginConfig login = new LoginConfig();

  /** Captcha configuration */
  private CaptchaConfig captcha = new CaptchaConfig();

  /** Whitelist paths (no authentication required) */
  private String[] whitelist = new String[] {};

  @Data
  public static class JwtConfig {
    /** JWT secret key */
    private String secret = "LoadUpFrameworkSecretKeyForJWTTokenGenerationMustBeLongEnough";

    /** Access token expiration time (milliseconds) */
    private Long expiration = 86400000L; // 24 hours

    /** Refresh token expiration time (milliseconds) */
    private Long refreshExpiration = 604800000L; // 7 days
  }

  @Data
  public static class LoginConfig {
    /** Maximum login failure attempts before lock */
    private Integer maxFailAttempts = 5;

    /** Account lock duration (minutes) */
    private Integer lockDuration = 30;

    /** Enable login failure tracking */
    private Boolean enableFailureTracking = true;
  }

  @Data
  public static class CaptchaConfig {
    /** Enable captcha verification */
    private Boolean enabled = true;

    /** Captcha type: image/sms */
    private String type = "image";

    /** Captcha expiration time (seconds) */
    private Integer expiration = 300; // 5 minutes
  }
}
