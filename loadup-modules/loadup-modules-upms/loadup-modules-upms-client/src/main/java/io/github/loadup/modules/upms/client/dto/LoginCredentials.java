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

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一登录凭证对象
 * 不同策略从中提取所需字段
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class LoginCredentials implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 登录类型：PASSWORD | MOBILE | EMAIL | OAUTH
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
     * OAuth 登录 - 授权码
     */
    private String code;

    /**
     * OAuth 登录 - ��态参数（防CSRF）
     */
    private String state;

    /**
     * OAuth 登录 - 回调地址
     */
    private String redirectUri;

    /**
     * 通用字段 - IP地址
     */
    private String ipAddress;

    /**
     * 通用字段 - 用户代理
     */
    private String userAgent;

    /**
     * 通用字段 - 图形验证码Key
     */
    private String captchaKey;

    /**
     * 通用字段 - 图形验证码值
     */
    private String captchaCode;

    /**
     * 扩展字段
     */
    private Map<String, Object> extra = new HashMap<>();

    public LoginCredentials(
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
            String ipAddress,
            String userAgent,
            String captchaKey,
            String captchaCode,
            Map<String, Object> extra) {
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
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.captchaKey = captchaKey;
        this.captchaCode = captchaCode;
        this.extra = extra;
    }

    public LoginCredentials() {}

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

    public String getIpAddress() {
        return this.ipAddress;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public String getCaptchaKey() {
        return this.captchaKey;
    }

    public String getCaptchaCode() {
        return this.captchaCode;
    }

    public Map<String, Object> getExtra() {
        return this.extra;
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

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setCaptchaKey(String captchaKey) {
        this.captchaKey = captchaKey;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String loginType;
        private String username;
        private String password;
        private String mobile;
        private String smsCode;
        private String email;
        private String emailCode;
        private String provider;
        private String code;
        private String state;
        private String redirectUri;
        private String ipAddress;
        private String userAgent;
        private String captchaKey;
        private String captchaCode;
        private Map<String, Object> extra = new HashMap<>();

        public Builder loginType(String loginType) {
            this.loginType = loginType;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder mobile(String mobile) {
            this.mobile = mobile;
            return this;
        }

        public Builder smsCode(String smsCode) {
            this.smsCode = smsCode;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder emailCode(String emailCode) {
            this.emailCode = emailCode;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder state(String state) {
            this.state = state;
            return this;
        }

        public Builder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder captchaKey(String captchaKey) {
            this.captchaKey = captchaKey;
            return this;
        }

        public Builder captchaCode(String captchaCode) {
            this.captchaCode = captchaCode;
            return this;
        }

        public Builder extra(Map<String, Object> extra) {
            this.extra = extra;
            return this;
        }

        public LoginCredentials build() {
            return new LoginCredentials(
                    this.loginType,
                    this.username,
                    this.password,
                    this.mobile,
                    this.smsCode,
                    this.email,
                    this.emailCode,
                    this.provider,
                    this.code,
                    this.state,
                    this.redirectUri,
                    this.ipAddress,
                    this.userAgent,
                    this.captchaKey,
                    this.captchaCode,
                    this.extra);
        }
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
