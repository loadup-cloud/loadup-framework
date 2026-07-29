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

/**
 * Login Result DTO - Contains access token and user information
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class AccessTokenDTO {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserDetailDTO userInfo;

    public AccessTokenDTO(
            String accessToken, String refreshToken, String tokenType, Long expiresIn, UserDetailDTO userInfo) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.userInfo = userInfo;
    }

    public AccessTokenDTO() {}

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public String getTokenType() {
        return this.tokenType;
    }

    public Long getExpiresIn() {
        return this.expiresIn;
    }

    public UserDetailDTO getUserInfo() {
        return this.userInfo;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public void setUserInfo(UserDetailDTO userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(accessToken, refreshToken, tokenType, expiresIn, userInfo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessTokenDTO other = (AccessTokenDTO) o;
        if (!java.util.Objects.equals(accessToken, other.accessToken)) return false;
        if (!java.util.Objects.equals(refreshToken, other.refreshToken)) return false;
        if (!java.util.Objects.equals(tokenType, other.tokenType)) return false;
        if (!java.util.Objects.equals(expiresIn, other.expiresIn)) return false;
        if (!java.util.Objects.equals(userInfo, other.userInfo)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "AccessTokenDTO(" + "accessToken=" + accessToken + ", " + "refreshToken=" + refreshToken + ", "
                + "tokenType=" + tokenType + ", " + "expiresIn=" + expiresIn + ", " + "userInfo=" + userInfo + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
        private UserDetailDTO userInfo;

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder expiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public Builder userInfo(UserDetailDTO userInfo) {
            this.userInfo = userInfo;
            return this;
        }

        public AccessTokenDTO build() {
            return new AccessTokenDTO(
                    this.accessToken, this.refreshToken, this.tokenType, this.expiresIn, this.userInfo);
        }
    }
}
