package io.github.loadup.modules.upms.client.dto;

/*-
 * #%L
 * Loadup Modules UPMS Client Layer
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

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 作用域
     */
    private String scope;

    public OAuthToken(String accessToken, String refreshToken, Long expiresIn, String scope) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.scope = scope;
    }

    public OAuthToken() {
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public Long getExpiresIn() {
        return this.expiresIn;
    }

    public String getScope() {
        return this.scope;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(accessToken, refreshToken, expiresIn, scope);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuthToken other = (OAuthToken) o;
        if (!java.util.Objects.equals(accessToken, other.accessToken)) return false;
        if (!java.util.Objects.equals(refreshToken, other.refreshToken)) return false;
        if (!java.util.Objects.equals(expiresIn, other.expiresIn)) return false;
        if (!java.util.Objects.equals(scope, other.scope)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "OAuthToken(" + "accessToken=" + accessToken + ", " + "refreshToken=" + refreshToken + ", " + "expiresIn=" + expiresIn + ", " + "scope=" + scope + ")";
    }

    public static Builder builder() {
        return new Builder();
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
