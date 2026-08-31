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

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户详情返回对象")
public class UserDetailDTO implements Serializable {

    @Schema(description = "用户ID")
    private String id;

    @Schema(description = "账号")
    private String account;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "性别：0-未知，1-男，2-女")
    private Integer gender;

    @Schema(description = "状态：0-正常，1-停用")
    private Integer status;

    @Schema(description = "所属部门ID")
    private String deptId;

    @Schema(description = "所属部门名称")
    private String deptName;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    private List<String> roles;
    private List<String> permissions;

    public UserDetailDTO(
            String id,
            String account,
            String nickname,
            String realName,
            String avatar,
            String mobile,
            String email,
            Integer gender,
            Integer status,
            String deptId,
            String deptName,
            LocalDateTime lastLoginTime,
            LocalDateTime createdAt,
            List<String> roles,
            List<String> permissions) {
        this.id = id;
        this.account = account;
        this.nickname = nickname;
        this.realName = realName;
        this.avatar = avatar;
        this.mobile = mobile;
        this.email = email;
        this.gender = gender;
        this.status = status;
        this.deptId = deptId;
        this.deptName = deptName;
        this.lastLoginTime = lastLoginTime;
        this.createdAt = createdAt;
        this.roles = roles;
        this.permissions = permissions;
    }

    public UserDetailDTO() {}

    public String getId() {
        return this.id;
    }

    public String getAccount() {
        return this.account;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getRealName() {
        return this.realName;
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

    public Integer getGender() {
        return this.gender;
    }

    public Integer getStatus() {
        return this.status;
    }

    public String getDeptId() {
        return this.deptId;
    }

    public String getDeptName() {
        return this.deptName;
    }

    public LocalDateTime getLastLoginTime() {
        return this.lastLoginTime;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public List<String> getRoles() {
        return this.roles;
    }

    public List<String> getPermissions() {
        return this.permissions;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setRealName(String realName) {
        this.realName = realName;
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

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
