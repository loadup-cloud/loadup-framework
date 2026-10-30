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
package io.github.loadup.modules.upms.app.strategy.oauth;

import io.github.loadup.commons.json.ToStringAsJson;
import java.io.Serial;
import java.io.Serializable;

/**
 * OAuth Token
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class OAuthToken implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String accessToken;

    private String refreshToken;

    private Long expiresIn;

    private String scope;

    public OAuthToken(String accessToken, String refreshToken, Long expiresIn, String scope) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.scope = scope;
    }

    public OAuthToken() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiresIn() {
        return this.expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getScope() {
        return this.scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }

    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private Long expiresIn;
        private String scope;

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder expiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public OAuthToken build() {
            return new OAuthToken(this.accessToken, this.refreshToken, this.expiresIn, this.scope);
        }
    }
}
