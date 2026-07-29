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

    public LoginLog(
            String id,
            String userId,
            String username,
            LocalDateTime loginTime,
            LocalDateTime logoutTime,
            String ipAddress,
            String loginLocation,
            String browser,
            String os,
            Short loginStatus,
            String loginMessage,
            String loginType,
            String provider) {
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

    public LoginLog() {}

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

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public LocalDateTime getLogoutTime() {
        return logoutTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getLoginLocation() {
        return loginLocation;
    }

    public String getBrowser() {
        return browser;
    }

    public String getOs() {
        return os;
    }

    public Short getLoginStatus() {
        return loginStatus;
    }

    public String getLoginMessage() {
        return loginMessage;
    }

    public String getLoginType() {
        return loginType;
    }

    public String getProvider() {
        return provider;
    }
}
