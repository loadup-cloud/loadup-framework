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
package io.github.loadup.modules.upms.client.command;

import io.github.loadup.commons.json.ToStringAsJson;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * User Login Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class UserLoginCommand {

    @Schema(description = "Login type")
    private String loginType;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Password, supplied only in requests", accessMode = Schema.AccessMode.WRITE_ONLY)
    @com.fasterxml.jackson.annotation.JsonProperty(
            access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Schema(description = "Mobile")
    private String mobile;

    @Schema(description = "SMS verification code", accessMode = Schema.AccessMode.WRITE_ONLY)
    @com.fasterxml.jackson.annotation.JsonProperty(
            access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String smsCode;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Email verification code", accessMode = Schema.AccessMode.WRITE_ONLY)
    @com.fasterxml.jackson.annotation.JsonProperty(
            access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String emailCode;

    @Schema(description = "Provider")
    private String provider;

    @Schema(description = "Business code")
    private String code;

    @Schema(description = "State")
    private String state;

    @Schema(description = "Redirect uri")
    private String redirectUri;

    @Schema(description = "Captcha key")
    private String captchaKey;

    @Schema(description = "Captcha answer", accessMode = Schema.AccessMode.WRITE_ONLY)
    @com.fasterxml.jackson.annotation.JsonProperty(
            access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String captchaCode;

    @Schema(description = "Ip address")
    private String ipAddress;

    @Schema(description = "User agent")
    private String userAgent;

    public UserLoginCommand(
            String loginType,
            String username,
            String password,
            String mobile,
            String smsCode,
            String email,
            String emailCode,
            String provider,
            String code,
            String state,
            String redirectUri,
            String captchaKey,
            String captchaCode,
            String ipAddress,
            String userAgent) {
        this.loginType = loginType;
        this.username = username;
        this.password = password;
        this.mobile = mobile;
        this.smsCode = smsCode;
        this.email = email;
        this.emailCode = emailCode;
        this.provider = provider;
        this.code = code;
        this.state = state;
        this.redirectUri = redirectUri;
        this.captchaKey = captchaKey;
        this.captchaCode = captchaCode;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public UserLoginCommand() {}

    public String getLoginType() {
        return this.loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMobile() {
        return this.mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getSmsCode() {
        return this.smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailCode() {
        return this.emailCode;
    }

    public void setEmailCode(String emailCode) {
        this.emailCode = emailCode;
    }

    public String getProvider() {
        return this.provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRedirectUri() {
        return this.redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getCaptchaKey() {
        return this.captchaKey;
    }

    public void setCaptchaKey(String captchaKey) {
        this.captchaKey = captchaKey;
    }

    public String getCaptchaCode() {
        return this.captchaCode;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
