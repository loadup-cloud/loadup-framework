package io.github.loadup.modules.upms.client.command;

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

/**
 * User Login Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class UserLoginCommand {

    /**
     * 登录类型：PASSWORD | MOBILE | EMAIL | OAUTH
     * 如果未指定，默认为 PASSWORD
     */
    private String loginType;

    /**
     * 账号密码登录 - 用户名
     */
    private String username;

    /**
     * 账号密码登录 - 密码
     */
    private String password;

    /**
     * 手机验证码登录 - 手机号
     */
    private String mobile;

    /**
     * 手机验证码登录 - 短信验证码
     */
    private String smsCode;

    /**
     * 邮箱验证码登录 - 邮箱
     */
    private String email;

    /**
     * 邮箱验证码登录 - 邮箱验证码
     */
    private String emailCode;

    /**
     * OAuth 登录 - 提供商（wechat | github | google）
     */
    private String provider;

    /**
     * OAuth 登录 - 授���码
     */
    private String code;

    /**
     * OAuth 登录 - 状态参数（防CSRF）
     */
    private String state;

    /**
     * OAuth 登录 - 回调地址
     */
    private String redirectUri;

    /**
     * 图形验证码Key
     */
    private String captchaKey;

    /**
     * 图形验证码值
     */
    private String captchaCode;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
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

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getMobile() {
        return this.mobile;
    }

    public String getSmsCode() {
        return this.smsCode;
    }

    public String getEmail() {
        return this.email;
    }

    public String getEmailCode() {
        return this.emailCode;
    }

    public String getProvider() {
        return this.provider;
    }

    public String getCode() {
        return this.code;
    }

    public String getState() {
        return this.state;
    }

    public String getRedirectUri() {
        return this.redirectUri;
    }

    public String getCaptchaKey() {
        return this.captchaKey;
    }

    public String getCaptchaCode() {
        return this.captchaCode;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmailCode(String emailCode) {
        this.emailCode = emailCode;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public void setCaptchaKey(String captchaKey) {
        this.captchaKey = captchaKey;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
