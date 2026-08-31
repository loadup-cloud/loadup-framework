package io.github.loadup.modules.upms.infrastructure.dataobject;

/*-
 * #%L
 * Loadup Modules UPMS Infrastructure Layer
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

    public LoginLogDO(
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

    public LoginLogDO() {}

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
}
