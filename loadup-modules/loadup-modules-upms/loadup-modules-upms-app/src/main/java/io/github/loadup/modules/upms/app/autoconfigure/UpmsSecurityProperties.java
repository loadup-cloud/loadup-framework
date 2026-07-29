package io.github.loadup.modules.upms.app.autoconfigure;

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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * UPMS Security Configuration Properties
 */
@ConfigurationProperties(prefix = "loadup.upms.security")
public class UpmsSecurityProperties {

    private JwtConfig jwt = new JwtConfig();
    private LoginConfig login = new LoginConfig();
    private OAuthProvidersConfig oauth = new OAuthProvidersConfig();

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

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Long getExpiration() {
            return expiration;
        }

        public void setExpiration(Long expiration) {
            this.expiration = expiration;
        }

        public Long getRefreshExpiration() {
            return refreshExpiration;
        }

        public void setRefreshExpiration(Long refreshExpiration) {
            this.refreshExpiration = refreshExpiration;
        }
    }

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

        public Boolean getEnableFailureTracking() {
            return enableFailureTracking;
        }

        public void setEnableFailureTracking(Boolean enableFailureTracking) {
            this.enableFailureTracking = enableFailureTracking;
        }

        public Integer getMaxFailAttempts() {
            return maxFailAttempts;
        }

        public void setMaxFailAttempts(Integer maxFailAttempts) {
            this.maxFailAttempts = maxFailAttempts;
        }

        public Integer getLockDuration() {
            return lockDuration;
        }

        public void setLockDuration(Integer lockDuration) {
            this.lockDuration = lockDuration;
        }
    }

    public static class OAuthProvidersConfig {
        private OAuthConfig github = new OAuthConfig();
        private OAuthConfig wechat = new OAuthConfig();
        private OAuthConfig google = new OAuthConfig();

        public OAuthConfig getGithub() {
            return github;
        }

        public void setGithub(OAuthConfig github) {
            this.github = github;
        }

        public OAuthConfig getWechat() {
            return wechat;
        }

        public void setWechat(OAuthConfig wechat) {
            this.wechat = wechat;
        }

        public OAuthConfig getGoogle() {
            return google;
        }

        public void setGoogle(OAuthConfig google) {
            this.google = google;
        }
    }

    public static class OAuthConfig {
        /**
         * 是否启用
         */
        private Boolean enabled = false;

        /**
         * Client ID
         */
        private String clientId;

        /**
         * Client Secret
         */
        private String clientSecret;

        /**
         * 回调地址
         */
        private String redirectUri;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }
    }

    public JwtConfig getJwt() {
        return jwt;
    }

    public void setJwt(JwtConfig jwt) {
        this.jwt = jwt;
    }

    public LoginConfig getLogin() {
        return login;
    }

    public void setLogin(LoginConfig login) {
        this.login = login;
    }

    public OAuthProvidersConfig getOauth() {
        return oauth;
    }

    public void setOauth(OAuthProvidersConfig oauth) {
        this.oauth = oauth;
    }
}
