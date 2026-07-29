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
    public int hashCode() {
        return java.util.Objects.hash(userId, username, password, nickname, status, permissions, roles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthUserDTO other = (AuthUserDTO) o;
        if (!java.util.Objects.equals(userId, other.userId)) return false;
        if (!java.util.Objects.equals(username, other.username)) return false;
        if (!java.util.Objects.equals(password, other.password)) return false;
        if (!java.util.Objects.equals(nickname, other.nickname)) return false;
        if (!java.util.Objects.equals(status, other.status)) return false;
        if (!java.util.Objects.equals(permissions, other.permissions)) return false;
        if (!java.util.Objects.equals(roles, other.roles)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "AuthUserDTO(" + "userId=" + userId + ", " + "username=" + username + ", " + "password=" + password
                + ", " + "nickname=" + nickname + ", " + "status=" + status + ", " + "permissions=" + permissions + ", "
                + "roles=" + roles + ")";
    }
}
