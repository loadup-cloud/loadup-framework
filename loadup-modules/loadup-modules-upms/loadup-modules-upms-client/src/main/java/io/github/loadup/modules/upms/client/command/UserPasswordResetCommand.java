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
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
