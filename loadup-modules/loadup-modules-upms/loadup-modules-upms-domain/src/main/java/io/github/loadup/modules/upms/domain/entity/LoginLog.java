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
 * Login Log Entity - User login/logout audit log
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class LoginLog {

    private String id;

    private String userId;

    private String username;

    private LocalDateTime loginTime;

    private LocalDateTime logoutTime;

    private String ipAddress;

    private String loginLocation;

    private String browser;

    private String os;

    /**
     * Login status: 1-Success, 0-Failure
     */
    private Short loginStatus;

    private String loginMessage;

    /**
     * 登录方式：PASSWORD | MOBILE | EMAIL | OAUTH
     */
    private String loginType;

    /**
     * OAuth提供商（仅OAuth登录时有值）：wechat | github | google
     */
    private String provider;

    public boolean isSuccess() {
        return loginStatus != null && loginStatus == 1;
    }

    public LoginLog(String id, String userId, String username, LocalDateTime loginTime, LocalDateTime logoutTime, String ipAddress, String loginLocation, String browser, String os, Short loginStatus, String loginMessage, String loginType, String provider) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.loginTime = loginTime;
        this.logoutTime = logoutTime;
        this.ipAddress = ipAddress;
        this.loginLocation = loginLocation;
        this.browser = browser;
        this.os = os;
        this.loginStatus = loginStatus;
        this.loginMessage = loginMessage;
        this.loginType = loginType;
        this.provider = provider;
    }

    public LoginLog() {
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

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public void setLogoutTime(LocalDateTime logoutTime) {
        this.logoutTime = logoutTime;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setLoginLocation(String loginLocation) {
        this.loginLocation = loginLocation;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public void setLoginStatus(Short loginStatus) {
        this.loginStatus = loginStatus;
    }

    public void setLoginMessage(String loginMessage) {
        this.loginMessage = loginMessage;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, userId, username, loginTime, logoutTime, ipAddress, loginLocation, browser, os, loginStatus, loginMessage, loginType, provider);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginLog other = (LoginLog) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(userId, other.userId)) return false;
        if (!java.util.Objects.equals(username, other.username)) return false;
        if (!java.util.Objects.equals(loginTime, other.loginTime)) return false;
        if (!java.util.Objects.equals(logoutTime, other.logoutTime)) return false;
        if (!java.util.Objects.equals(ipAddress, other.ipAddress)) return false;
        if (!java.util.Objects.equals(loginLocation, other.loginLocation)) return false;
        if (!java.util.Objects.equals(browser, other.browser)) return false;
        if (!java.util.Objects.equals(os, other.os)) return false;
        if (!java.util.Objects.equals(loginStatus, other.loginStatus)) return false;
        if (!java.util.Objects.equals(loginMessage, other.loginMessage)) return false;
        if (!java.util.Objects.equals(loginType, other.loginType)) return false;
        if (!java.util.Objects.equals(provider, other.provider)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "LoginLog(" + "id=" + id + ", " + "userId=" + userId + ", " + "username=" + username + ", " + "loginTime=" + loginTime + ", " + "logoutTime=" + logoutTime + ", " + "ipAddress=" + ipAddress + ", " + "loginLocation=" + loginLocation + ", " + "browser=" + browser + ", " + "os=" + os + ", " + "loginStatus=" + loginStatus + ", " + "loginMessage=" + loginMessage + ", " + "loginType=" + loginType + ", " + "provider=" + provider + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String userId;
        private String username;
        private LocalDateTime loginTime;
        private LocalDateTime logoutTime;
        private String ipAddress;
        private String loginLocation;
        private String browser;
        private String os;
        private Short loginStatus;
        private String loginMessage;
        private String loginType;
        private String provider;

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

        public Builder loginTime(LocalDateTime loginTime) {
            this.loginTime = loginTime;
            return this;
        }

        public Builder logoutTime(LocalDateTime logoutTime) {
            this.logoutTime = logoutTime;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder loginLocation(String loginLocation) {
            this.loginLocation = loginLocation;
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

        public Builder loginStatus(Short loginStatus) {
            this.loginStatus = loginStatus;
            return this;
        }

        public Builder loginMessage(String loginMessage) {
            this.loginMessage = loginMessage;
            return this;
        }

        public Builder loginType(String loginType) {
            this.loginType = loginType;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public LoginLog build() {
            return new LoginLog(this.id, this.userId, this.username, this.loginTime, this.logoutTime, this.ipAddress, this.loginLocation, this.browser, this.os, this.loginStatus, this.loginMessage, this.loginType, this.provider);
        }
    }
}
