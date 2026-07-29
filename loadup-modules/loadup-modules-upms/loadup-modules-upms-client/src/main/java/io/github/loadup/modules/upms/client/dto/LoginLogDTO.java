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

    public LoginLogDTO(String id, String userId, String username, String loginType, String ipAddress, String location, String browser, String os, Boolean success, String message, LocalDateTime loginTime) {
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

    public LoginLogDTO() {
    }

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

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, userId, username, loginType, ipAddress, location, browser, os, success, message, loginTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginLogDTO other = (LoginLogDTO) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(userId, other.userId)) return false;
        if (!java.util.Objects.equals(username, other.username)) return false;
        if (!java.util.Objects.equals(loginType, other.loginType)) return false;
        if (!java.util.Objects.equals(ipAddress, other.ipAddress)) return false;
        if (!java.util.Objects.equals(location, other.location)) return false;
        if (!java.util.Objects.equals(browser, other.browser)) return false;
        if (!java.util.Objects.equals(os, other.os)) return false;
        if (!java.util.Objects.equals(success, other.success)) return false;
        if (!java.util.Objects.equals(message, other.message)) return false;
        if (!java.util.Objects.equals(loginTime, other.loginTime)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "LoginLogDTO(" + "id=" + id + ", " + "userId=" + userId + ", " + "username=" + username + ", " + "loginType=" + loginType + ", " + "ipAddress=" + ipAddress + ", " + "location=" + location + ", " + "browser=" + browser + ", " + "os=" + os + ", " + "success=" + success + ", " + "message=" + message + ", " + "loginTime=" + loginTime + ")";
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
            return new LoginLogDTO(this.id, this.userId, this.username, this.loginType, this.ipAddress, this.location, this.browser, this.os, this.success, this.message, this.loginTime);
        }
    }
}
