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
import java.util.HashMap;
import java.util.Map;

/**
 * 认证成功后的用户信息
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class AuthenticatedUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 是否新用户（用于首次 OAuth 登录后引导绑定）
     */
    private boolean newUser = false;

    /**
     * 扩展信息
     */
    private Map<String, Object> attributes = new HashMap<>();

    public AuthenticatedUser(String userId, String username, String nickname, String avatar, String email, String mobile, boolean newUser, Map<String, Object> attributes) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
        this.email = email;
        this.mobile = mobile;
        this.newUser = newUser;
        this.attributes = attributes;
    }

    public AuthenticatedUser() {
    }

    public String getUserId() {
        return this.userId;
    }

    public String getUsername() {
        return this.username;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public String getEmail() {
        return this.email;
    }

    public String getMobile() {
        return this.mobile;
    }

    public boolean isNewUser() {
        return this.newUser;
    }

    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(userId, username, nickname, avatar, email, mobile, newUser, attributes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthenticatedUser other = (AuthenticatedUser) o;
        if (!java.util.Objects.equals(userId, other.userId)) return false;
        if (!java.util.Objects.equals(username, other.username)) return false;
        if (!java.util.Objects.equals(nickname, other.nickname)) return false;
        if (!java.util.Objects.equals(avatar, other.avatar)) return false;
        if (!java.util.Objects.equals(email, other.email)) return false;
        if (!java.util.Objects.equals(mobile, other.mobile)) return false;
        if (!java.util.Objects.equals(newUser, other.newUser)) return false;
        if (!java.util.Objects.equals(attributes, other.attributes)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "AuthenticatedUser(" + "userId=" + userId + ", " + "username=" + username + ", " + "nickname=" + nickname + ", " + "avatar=" + avatar + ", " + "email=" + email + ", " + "mobile=" + mobile + ", " + "newUser=" + newUser + ", " + "attributes=" + attributes + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userId;
        private String username;
        private String nickname;
        private String avatar;
        private String email;
        private String mobile;
        private boolean newUser = false;
        private Map<String, Object> attributes = new HashMap<>();

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
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

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder mobile(String mobile) {
            this.mobile = mobile;
            return this;
        }

        public Builder newUser(boolean newUser) {
            this.newUser = newUser;
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        public AuthenticatedUser build() {
            return new AuthenticatedUser(this.userId, this.username, this.nickname, this.avatar, this.email, this.mobile, this.newUser, this.attributes);
        }
    }
}
