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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * User Password Reset Command (with verification code)
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class UserPasswordResetCommand {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "验证码不能为空")
    private String verificationCode;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20之间")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /**
     * Verification type: EMAIL or SMS
     */
    private String verificationType;

    public UserPasswordResetCommand(
            String username,
            String verificationCode,
            String newPassword,
            String confirmPassword,
            String verificationType) {
        this.username = username;
        this.verificationCode = verificationCode;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
        this.verificationType = verificationType;
    }

    public UserPasswordResetCommand() {}

    public String getUsername() {
        return this.username;
    }

    public String getVerificationCode() {
        return this.verificationCode;
    }

    public String getNewPassword() {
        return this.newPassword;
    }

    public String getConfirmPassword() {
        return this.confirmPassword;
    }

    public String getVerificationType() {
        return this.verificationType;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void setVerificationType(String verificationType) {
        this.verificationType = verificationType;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(username, verificationCode, newPassword, confirmPassword, verificationType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPasswordResetCommand other = (UserPasswordResetCommand) o;
        if (!java.util.Objects.equals(username, other.username)) return false;
        if (!java.util.Objects.equals(verificationCode, other.verificationCode)) return false;
        if (!java.util.Objects.equals(newPassword, other.newPassword)) return false;
        if (!java.util.Objects.equals(confirmPassword, other.confirmPassword)) return false;
        if (!java.util.Objects.equals(verificationType, other.verificationType)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "UserPasswordResetCommand(" + "username=" + username + ", " + "verificationCode=" + verificationCode
                + ", " + "newPassword=" + newPassword + ", " + "confirmPassword=" + confirmPassword + ", "
                + "verificationType=" + verificationType + ")";
    }
}
