package io.github.loadup.modules.upms.client.command;

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
    @Size(min = 8, max = 72, message = "新密码长度必须在8-72之间")
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
    public String toString() {
        return "UserPasswordChangeCommand{userId='" + userId + "', passwords=[REDACTED]}";
    }
}
