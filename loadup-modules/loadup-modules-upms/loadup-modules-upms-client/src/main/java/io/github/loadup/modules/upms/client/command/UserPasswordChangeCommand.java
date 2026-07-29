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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * User Password Change Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class UserPasswordChangeCommand {

    @NotNull(message = "用户ID不能为空")
    private String userId;

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20之间")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    public UserPasswordChangeCommand(String userId, String oldPassword, String newPassword, String confirmPassword) {
        this.userId = userId;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public UserPasswordChangeCommand() {}

    public String getUserId() {
        return this.userId;
    }

    public String getOldPassword() {
        return this.oldPassword;
    }

    public String getNewPassword() {
        return this.newPassword;
    }

    public String getConfirmPassword() {
        return this.confirmPassword;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(userId, oldPassword, newPassword, confirmPassword);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPasswordChangeCommand other = (UserPasswordChangeCommand) o;
        if (!java.util.Objects.equals(userId, other.userId)) return false;
        if (!java.util.Objects.equals(oldPassword, other.oldPassword)) return false;
        if (!java.util.Objects.equals(newPassword, other.newPassword)) return false;
        if (!java.util.Objects.equals(confirmPassword, other.confirmPassword)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "UserPasswordChangeCommand(" + "userId=" + userId + ", " + "oldPassword=" + oldPassword + ", "
                + "newPassword=" + newPassword + ", " + "confirmPassword=" + confirmPassword + ")";
    }
}
