package io.github.loadup.modules.upms.client.dto;

/*-
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

import java.time.LocalDateTime;

/**
 * Login Log DTO
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class LoginLogDTO {

    private String id;
    private String userId;
    private String username;
    private String loginType;
    private String ipAddress;
    private String location;
    private String browser;
    private String os;
    private Boolean success;
    private String message;
    private LocalDateTime loginTime;

    public LoginLogDTO(
            String id,
            String userId,
            String username,
            String loginType,
            String ipAddress,
            String location,
            String browser,
            String os,
            Boolean success,
            String message,
            LocalDateTime loginTime) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.loginType = loginType;
        this.ipAddress = ipAddress;
        this.location = location;
        this.browser = browser;
        this.os = os;
        this.success = success;
        this.message = message;
        this.loginTime = loginTime;
    }

    public LoginLogDTO() {}

    public String getId() {
        return this.id;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getUsername() {
        return this.username;
    }

    public String getLoginType() {
        return this.loginType;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public String getLocation() {
        return this.location;
    }

    public String getBrowser() {
        return this.browser;
    }

    public String getOs() {
        return this.os;
    }

    public Boolean isSuccess() {
        return this.success;
    }

    public String getMessage() {
        return this.message;
    }

    public LocalDateTime getLoginTime() {
        return this.loginTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String userId;
        private String username;
        private String loginType;
        private String ipAddress;
        private String location;
        private String browser;
        private String os;
        private Boolean success;
        private String message;
        private LocalDateTime loginTime;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder loginType(String loginType) {
            this.loginType = loginType;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder browser(String browser) {
            this.browser = browser;
            return this;
        }

        public Builder os(String os) {
            this.os = os;
            return this;
        }

        public Builder success(Boolean success) {
            this.success = success;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder loginTime(LocalDateTime loginTime) {
            this.loginTime = loginTime;
            return this;
        }

        public LoginLogDTO build() {
            return new LoginLogDTO(
                    this.id,
                    this.userId,
                    this.username,
                    this.loginType,
                    this.ipAddress,
                    this.location,
                    this.browser,
                    this.os,
                    this.success,
                    this.message,
                    this.loginTime);
        }
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
