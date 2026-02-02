package io.github.loadup.modules.upms.security.config;

/*-
 * #%L
 * Loadup Modules UPMS Security Layer
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
public class UpmsSecurityProperties {

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
