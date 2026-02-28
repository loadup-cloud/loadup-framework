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
import lombok.Data;

/** 安全上下文中的用户信息契约 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthUserDTO implements Serializable {
    private String userId;
    private String username;

    @JsonIgnore
    private String password;

    private String nickname;
    private Integer status; // 0-正常, 1-锁定

    /** 权限标识集合 (如: sys:user:add) */
    private Set<String> permissions;

    /** 角色标识集合 (如: ROLE_ADMIN) */
    private Set<String> roles;

    public boolean actived() {
        return status == 0;
    }
}
