package io.github.loadup.modules.upms.app.config;

/*-
 * #%L
 * Loadup Modules UPMS App Layer
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
import org.springframework.stereotype.Component;

/**
 * UPMS Security Configuration Properties
 */
@Data
@Component
@ConfigurationProperties(prefix = "loadup.upms.security")
public class UpmsSecurityProperties {

    private JwtConfig jwt = new JwtConfig();
    private LoginConfig login = new LoginConfig();

    @Data
    public static class JwtConfig {
        /**
         * JWT secret key
         */
        private String secret = "loadup-secret-key-change-in-production";

        /**
         * Token expiration time in milliseconds (default: 24 hours)
         */
        private Long expiration = 86400000L;

        /**
         * Refresh token expiration time in milliseconds (default: 7 days)
         */
        private Long refreshExpiration = 604800000L;
    }

    @Data
    public static class LoginConfig {
        /**
         * Enable login failure tracking
         */
        private Boolean enableFailureTracking = true;

        /**
         * Maximum failed login attempts before locking
         */
        private Integer maxFailAttempts = 5;

        /**
         * Account lock duration in minutes
         */
        private Integer lockDuration = 30;
    }
}
