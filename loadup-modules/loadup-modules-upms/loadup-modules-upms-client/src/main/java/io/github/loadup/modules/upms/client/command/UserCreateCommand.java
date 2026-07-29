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

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * User Create Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class UserCreateCommand {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 30, message = "用户名长度必须在3-30之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    @Size(max = 50, message = "真实姓名长度不能超过50")
    private String realName;

    private String deptId;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    private String avatar;

    private Short gender; // 0-Unknown, 1-Male, 2-Female

    private LocalDate birthday;

    private Short status; // 1-Normal, 0-Disabled

    private List<String> roleIds;

    private String remark;

    private String createdBy;

    public UserCreateCommand(
            String username,
            String password,
            String nickname,
            String realName,
            String deptId,
            String email,
            String mobile,
            String avatar,
            Short gender,
            LocalDate birthday,
            Short status,
            List<String> roleIds,
            String remark,
            String createdBy) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.realName = realName;
        this.deptId = deptId;
        this.email = email;
        this.mobile = mobile;
        this.avatar = avatar;
        this.gender = gender;
        this.birthday = birthday;
        this.status = status;
        this.roleIds = roleIds;
        this.remark = remark;
        this.createdBy = createdBy;
    }

    public UserCreateCommand() {}

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getRealName() {
        return this.realName;
    }

    public String getDeptId() {
        return this.deptId;
    }

    public String getEmail() {
        return this.email;
    }

    public String getMobile() {
        return this.mobile;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public Short getGender() {
        return this.gender;
    }

    public LocalDate getBirthday() {
        return this.birthday;
    }

    public Short getStatus() {
        return this.status;
    }

    public List<String> getRoleIds() {
        return this.roleIds;
    }

    public String getRemark() {
        return this.remark;
    }

    public String getCreatedBy() {
        return this.createdBy;
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

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setGender(Short gender) {
        this.gender = gender;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
