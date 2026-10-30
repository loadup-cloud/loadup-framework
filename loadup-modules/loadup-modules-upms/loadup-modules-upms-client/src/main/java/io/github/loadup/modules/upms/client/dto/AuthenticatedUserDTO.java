/*
 * #%L
 * Loadup Modules UPMS Client Layer
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
package io.github.loadup.modules.upms.client.dto;

import io.github.loadup.commons.json.ToStringAsJson;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class AuthenticatedUserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "User id")
    private String userId;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Nickname")
    private String nickname;

    @Schema(description = "Avatar")
    private String avatar;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Mobile")
    private String mobile;

    private boolean newUser = false;

    private Map<String, Object> attributes = new HashMap<>();

    public AuthenticatedUserDTO(
            String userId,
            String username,
            String nickname,
            String avatar,
            String email,
            String mobile,
            boolean newUser,
            Map<String, Object> attributes) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
        this.email = email;
        this.mobile = mobile;
        this.newUser = newUser;
        this.attributes = attributes;
    }

    public AuthenticatedUserDTO() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return this.mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public boolean isNewUser() {
        return this.newUser;
    }

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }

    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
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

        public AuthenticatedUserDTO build() {
            return new AuthenticatedUserDTO(
                    this.userId,
                    this.username,
                    this.nickname,
                    this.avatar,
                    this.email,
                    this.mobile,
                    this.newUser,
                    this.attributes);
        }
    }
}
