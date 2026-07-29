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
import org.springframework.stereotype.Component;

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
    }

    public static class OAuthProvidersConfig {
        private OAuthConfig github = new OAuthConfig();
        private OAuthConfig wechat = new OAuthConfig();
        private OAuthConfig google = new OAuthConfig();
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
    }

    public UpmsSecurityProperties(JwtConfig jwt, LoginConfig login, OAuthProvidersConfig oauth, String secret, Long expiration, Long refreshExpiration, Boolean enableFailureTracking, Integer maxFailAttempts, Integer lockDuration, OAuthConfig github, OAuthConfig wechat, OAuthConfig google, Boolean enabled, String clientId, String clientSecret, String redirectUri) {
        this.jwt = jwt;
        this.login = login;
        this.oauth = oauth;
        this.secret = secret;
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
        this.enableFailureTracking = enableFailureTracking;
        this.maxFailAttempts = maxFailAttempts;
        this.lockDuration = lockDuration;
        this.github = github;
        this.wechat = wechat;
        this.google = google;
        this.enabled = enabled;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public UpmsSecurityProperties() {
    }

    public JwtConfig getJwt() {
        return this.jwt;
    }

    public LoginConfig getLogin() {
        return this.login;
    }

    public OAuthProvidersConfig getOauth() {
        return this.oauth;
    }

    public String getSecret() {
        return this.secret;
    }

    public Long getExpiration() {
        return this.expiration;
    }

    public Long getRefreshExpiration() {
        return this.refreshExpiration;
    }

    public Boolean isEnableFailureTracking() {
        return this.enableFailureTracking;
    }

    public Integer getMaxFailAttempts() {
        return this.maxFailAttempts;
    }

    public Integer getLockDuration() {
        return this.lockDuration;
    }

    public OAuthConfig getGithub() {
        return this.github;
    }

    public OAuthConfig getWechat() {
        return this.wechat;
    }

    public OAuthConfig getGoogle() {
        return this.google;
    }

    public Boolean isEnabled() {
        return this.enabled;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public String getRedirectUri() {
        return this.redirectUri;
    }

    public void setJwt(JwtConfig jwt) {
        this.jwt = jwt;
    }

    public void setLogin(LoginConfig login) {
        this.login = login;
    }

    public void setOauth(OAuthProvidersConfig oauth) {
        this.oauth = oauth;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    public void setRefreshExpiration(Long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }

    public void setEnableFailureTracking(Boolean enableFailureTracking) {
        this.enableFailureTracking = enableFailureTracking;
    }

    public void setMaxFailAttempts(Integer maxFailAttempts) {
        this.maxFailAttempts = maxFailAttempts;
    }

    public void setLockDuration(Integer lockDuration) {
        this.lockDuration = lockDuration;
    }

    public void setGithub(OAuthConfig github) {
        this.github = github;
    }

    public void setWechat(OAuthConfig wechat) {
        this.wechat = wechat;
    }

    public void setGoogle(OAuthConfig google) {
        this.google = google;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(jwt, login, oauth, secret, expiration, refreshExpiration, enableFailureTracking, maxFailAttempts, lockDuration, github, wechat, google, enabled, clientId, clientSecret, redirectUri);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpmsSecurityProperties other = (UpmsSecurityProperties) o;
        if (!java.util.Objects.equals(jwt, other.jwt)) return false;
        if (!java.util.Objects.equals(login, other.login)) return false;
        if (!java.util.Objects.equals(oauth, other.oauth)) return false;
        if (!java.util.Objects.equals(secret, other.secret)) return false;
        if (!java.util.Objects.equals(expiration, other.expiration)) return false;
        if (!java.util.Objects.equals(refreshExpiration, other.refreshExpiration)) return false;
        if (!java.util.Objects.equals(enableFailureTracking, other.enableFailureTracking)) return false;
        if (!java.util.Objects.equals(maxFailAttempts, other.maxFailAttempts)) return false;
        if (!java.util.Objects.equals(lockDuration, other.lockDuration)) return false;
        if (!java.util.Objects.equals(github, other.github)) return false;
        if (!java.util.Objects.equals(wechat, other.wechat)) return false;
        if (!java.util.Objects.equals(google, other.google)) return false;
        if (!java.util.Objects.equals(enabled, other.enabled)) return false;
        if (!java.util.Objects.equals(clientId, other.clientId)) return false;
        if (!java.util.Objects.equals(clientSecret, other.clientSecret)) return false;
        if (!java.util.Objects.equals(redirectUri, other.redirectUri)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "UpmsSecurityProperties(" + "jwt=" + jwt + ", " + "login=" + login + ", " + "oauth=" + oauth + ", " + "secret=" + secret + ", " + "expiration=" + expiration + ", " + "refreshExpiration=" + refreshExpiration + ", " + "enableFailureTracking=" + enableFailureTracking + ", " + "maxFailAttempts=" + maxFailAttempts + ", " + "lockDuration=" + lockDuration + ", " + "github=" + github + ", " + "wechat=" + wechat + ", " + "google=" + google + ", " + "enabled=" + enabled + ", " + "clientId=" + clientId + ", " + "clientSecret=" + clientSecret + ", " + "redirectUri=" + redirectUri + ")";
    }
}
