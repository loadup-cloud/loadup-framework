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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * @since 1.0.0
 * @author LoadUp Framework
 *     <p>User Update Command
 */
public class UserUpdateCommand {

    private String updatedBy;

    private String remark;

    private List<String> roleIds;

    private Short status; // 1-Normal, 0-Disabled

    private LocalDate birthday;

    private Short gender; // 0-Unknown, 1-Male, 2-Female

    private String avatar;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String deptId;

    @Size(max = 50, message = "真实姓名长度不能超过50")
    private String realName;

    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    @NotNull(message = "用户ID不能为空")
    private String id;

    public UserUpdateCommand(
            String updatedBy,
            String remark,
            List<String> roleIds,
            Short status,
            LocalDate birthday,
            Short gender,
            String avatar,
            String mobile,
            String email,
            String deptId,
            String realName,
            String nickname,
            String id) {
        this.updatedBy = updatedBy;
        this.remark = remark;
        this.roleIds = roleIds;
        this.status = status;
        this.birthday = birthday;
        this.gender = gender;
        this.avatar = avatar;
        this.mobile = mobile;
        this.email = email;
        this.deptId = deptId;
        this.realName = realName;
        this.nickname = nickname;
        this.id = id;
    }

    public UserUpdateCommand() {}

    public String getUpdatedBy() {
        return this.updatedBy;
    }

    public String getRemark() {
        return this.remark;
    }

    public List<String> getRoleIds() {
        return this.roleIds;
    }

    public Short getStatus() {
        return this.status;
    }

    public LocalDate getBirthday() {
        return this.birthday;
    }

    public Short getGender() {
        return this.gender;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public String getMobile() {
        return this.mobile;
    }

    public String getEmail() {
        return this.email;
    }

    public String getDeptId() {
        return this.deptId;
    }

    public String getRealName() {
        return this.realName;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getId() {
        return this.id;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public void setGender(Short gender) {
        this.gender = gender;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
