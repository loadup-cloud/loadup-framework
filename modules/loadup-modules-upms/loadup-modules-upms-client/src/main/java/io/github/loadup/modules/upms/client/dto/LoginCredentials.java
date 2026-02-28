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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private Map<String, Object> extra = new HashMap<>();
}

