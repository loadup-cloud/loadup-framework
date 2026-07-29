package io.github.loadup.modules.upms.infrastructure.dataobject;

/*-
 * #%L
 * Loadup Modules UPMS Infrastructure Layer
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

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import java.time.LocalDateTime;

/**
 * LoginLog Data Object
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Table("upms_login_log")
public class LoginLogDO extends BaseDO {

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

    /**
     * 登录方式：PASSWORD | MOBILE | EMAIL | OAUTH
     */
    private String loginType;

    /**
     * OAuth提供商（仅OAuth登录时有值）
     */
    private String provider;

    public LoginLogDO(String userId, String username, LocalDateTime loginTime, LocalDateTime logoutTime, String ipAddress, String loginLocation, String browser, String os, Short loginStatus, String loginMessage, String loginType, String provider) {
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

    public LoginLogDO() {
    }

    public String getUserId() {
        return this.userId;
    }

    public String getUsername() {
        return this.username;
    }

    public LocalDateTime getLoginTime() {
        return this.loginTime;
    }

    public LocalDateTime getLogoutTime() {
        return this.logoutTime;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public String getLoginLocation() {
        return this.loginLocation;
    }

    public String getBrowser() {
        return this.browser;
    }

    public String getOs() {
        return this.os;
    }

    public Short getLoginStatus() {
        return this.loginStatus;
    }

    public String getLoginMessage() {
        return this.loginMessage;
    }

    public String getLoginType() {
        return this.loginType;
    }

    public String getProvider() {
        return this.provider;
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
        return java.util.Objects.hash(super.hashCode(), userId, username, loginTime, logoutTime, ipAddress, loginLocation, browser, os, loginStatus, loginMessage, loginType, provider);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LoginLogDO other = (LoginLogDO) o;
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
        return "LoginLogDO(" + "super=" + super.toString() + ", " + "userId=" + userId + ", " + "username=" + username + ", " + "loginTime=" + loginTime + ", " + "logoutTime=" + logoutTime + ", " + "ipAddress=" + ipAddress + ", " + "loginLocation=" + loginLocation + ", " + "browser=" + browser + ", " + "os=" + os + ", " + "loginStatus=" + loginStatus + ", " + "loginMessage=" + loginMessage + ", " + "loginType=" + loginType + ", " + "provider=" + provider + ")";
    }
}
