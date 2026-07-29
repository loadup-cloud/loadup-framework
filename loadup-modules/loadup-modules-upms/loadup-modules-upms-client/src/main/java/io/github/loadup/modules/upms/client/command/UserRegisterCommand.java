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

/**
 * User Register Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class UserRegisterCommand {

    private String username;

    private String password;

    private String nickname;

    private String email;

    private String mobile;

    private String captchaKey;

    private String captchaCode;

    private String smsCode;

    public UserRegisterCommand(
            String username,
            String password,
            String nickname,
            String email,
            String mobile,
            String captchaKey,
            String captchaCode,
            String smsCode) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.mobile = mobile;
        this.captchaKey = captchaKey;
        this.captchaCode = captchaCode;
        this.smsCode = smsCode;
    }

    public UserRegisterCommand() {}

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getEmail() {
        return this.email;
    }

    public String getMobile() {
        return this.mobile;
    }

    public String getCaptchaKey() {
        return this.captchaKey;
    }

    public String getCaptchaCode() {
        return this.captchaCode;
    }

    public String getSmsCode() {
        return this.smsCode;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setCaptchaKey(String captchaKey) {
        this.captchaKey = captchaKey;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
