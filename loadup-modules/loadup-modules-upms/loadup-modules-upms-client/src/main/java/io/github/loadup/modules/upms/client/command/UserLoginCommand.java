package io.github.loadup.modules.upms.client.command;

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

import lombok.Data;

/**
 * User Login Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
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
}
