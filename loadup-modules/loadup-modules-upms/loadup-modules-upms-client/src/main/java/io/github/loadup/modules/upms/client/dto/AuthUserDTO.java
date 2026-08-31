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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Set;

/**
 * 安全上下文中的用户信息契约
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthUserDTO implements Serializable {
    private String userId;
    private String username;

    @JsonIgnore
    private String password;

    private String nickname;
    private Integer status; // 0-正常, 1-锁定

    /**
     * 权限标识集合 (如: sys:user:add)
     */
    private Set<String> permissions;

    /**
     * 角色标识集合 (如: ROLE_ADMIN)
     */
    private Set<String> roles;

    public boolean actived() {
        return status == 0;
    }

    public AuthUserDTO(
            String userId,
            String username,
            String password,
            String nickname,
            Integer status,
            Set<String> permissions,
            Set<String> roles) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.status = status;
        this.permissions = permissions;
        this.roles = roles;
    }

    public AuthUserDTO() {}

    public String getUserId() {
        return this.userId;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getNickname() {
        return this.nickname;
    }

    public Integer getStatus() {
        return this.status;
    }

    public Set<String> getPermissions() {
        return this.permissions;
    }

    public Set<String> getRoles() {
        return this.roles;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
