/*
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
package io.github.loadup.modules.upms.app.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * UPMS Security Configuration Properties
 */
@ConfigurationProperties(prefix = "loadup.modules.upms.security")
public class UpmsSecurityProperties {

    private LoginConfig login = new LoginConfig();
    private OAuthProvidersConfig oauth = new OAuthProvidersConfig();

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
         * Whether this provider is enabled
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
         * OAuth redirect URI
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
