package io.github.loadup.modules.upms.domain.entity;

/*-
 * #%L
 * Loadup Modules UPMS Domain Layer
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

import io.github.loadup.commons.domain.BaseEntity;
import java.time.LocalDateTime;

/**
 * Login Log Entity - User login/logout audit log
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class LoginLog extends BaseEntity {

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
        setId(id);
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
