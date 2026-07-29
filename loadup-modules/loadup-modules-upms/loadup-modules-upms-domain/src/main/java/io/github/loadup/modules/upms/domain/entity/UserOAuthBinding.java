package io.github.loadup.modules.upms.domain.entity;

/*-
 * #%L
 * Loadup Modules UPMS Domain Layer
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

import java.time.LocalDateTime;

/**
 * 用户OAuth第三方账号绑定实体
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class UserOAuthBinding {

    /**
     * 主键ID
     */
    private String id;

    /**
     * 本地用户ID
     */
    private String userId;

    /**
     * OAuth提供商：wechat | github | google
     */
    private String provider;

    /**
     * 第三方平台用户唯一ID
     */
    private String openId;

    /**
     * 联合ID（如微信UnionID）
     */
    private String unionId;

    /**
     * 第三方昵称
     */
    private String nickname;

    /**
     * 第三方头像
     */
    private String avatar;

    /**
     * 访问令牌（加密存储）
     */
    private String accessToken;

    /**
     * 刷新令牌（加密存储）
     */
    private String refreshToken;

    /**
     * 令牌过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 绑定时间
     */
    private LocalDateTime boundAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    public UserOAuthBinding(String id, String userId, String provider, String openId, String unionId, String nickname, String avatar, String accessToken, String refreshToken, LocalDateTime expiresAt, LocalDateTime boundAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.openId = openId;
        this.unionId = unionId;
        this.nickname = nickname;
        this.avatar = avatar;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.boundAt = boundAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UserOAuthBinding() {
    }

    public String getId() {
        return this.id;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getProvider() {
        return this.provider;
    }

    public String getOpenId() {
        return this.openId;
    }

    public String getUnionId() {
        return this.unionId;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public LocalDateTime getExpiresAt() {
        return this.expiresAt;
    }

    public LocalDateTime getBoundAt() {
        return this.boundAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setBoundAt(LocalDateTime boundAt) {
        this.boundAt = boundAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, userId, provider, openId, unionId, nickname, avatar, accessToken, refreshToken, expiresAt, boundAt, createdAt, updatedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserOAuthBinding other = (UserOAuthBinding) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(userId, other.userId)) return false;
        if (!java.util.Objects.equals(provider, other.provider)) return false;
        if (!java.util.Objects.equals(openId, other.openId)) return false;
        if (!java.util.Objects.equals(unionId, other.unionId)) return false;
        if (!java.util.Objects.equals(nickname, other.nickname)) return false;
        if (!java.util.Objects.equals(avatar, other.avatar)) return false;
        if (!java.util.Objects.equals(accessToken, other.accessToken)) return false;
        if (!java.util.Objects.equals(refreshToken, other.refreshToken)) return false;
        if (!java.util.Objects.equals(expiresAt, other.expiresAt)) return false;
        if (!java.util.Objects.equals(boundAt, other.boundAt)) return false;
        if (!java.util.Objects.equals(createdAt, other.createdAt)) return false;
        if (!java.util.Objects.equals(updatedAt, other.updatedAt)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "UserOAuthBinding(" + "id=" + id + ", " + "userId=" + userId + ", " + "provider=" + provider + ", " + "openId=" + openId + ", " + "unionId=" + unionId + ", " + "nickname=" + nickname + ", " + "avatar=" + avatar + ", " + "accessToken=" + accessToken + ", " + "refreshToken=" + refreshToken + ", " + "expiresAt=" + expiresAt + ", " + "boundAt=" + boundAt + ", " + "createdAt=" + createdAt + ", " + "updatedAt=" + updatedAt + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String userId;
        private String provider;
        private String openId;
        private String unionId;
        private String nickname;
        private String avatar;
        private String accessToken;
        private String refreshToken;
        private LocalDateTime expiresAt;
        private LocalDateTime boundAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder openId(String openId) {
            this.openId = openId;
            return this;
        }

        public Builder unionId(String unionId) {
            this.unionId = unionId;
            return this;
        }

        public Builder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder boundAt(LocalDateTime boundAt) {
            this.boundAt = boundAt;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserOAuthBinding build() {
            return new UserOAuthBinding(this.id, this.userId, this.provider, this.openId, this.unionId, this.nickname, this.avatar, this.accessToken, this.refreshToken, this.expiresAt, this.boundAt, this.createdAt, this.updatedAt);
        }
    }
}
