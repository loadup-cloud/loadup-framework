package io.github.loadup.components.security.example;

/*-
 * #%L
 * LoadUp Components Security
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

import io.github.loadup.components.security.util.SecurityHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * 使用示例：基于角色的权限控制
 *
 * <p>本示例展示如何在业务 Service 中使用 @PreAuthorize 注解
 */
@Service
public class UserServiceExample {

    /**
     * 只有 ADMIN 角色可以删除用户
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        // 删除用户逻辑
        System.out.println("Deleting user: " + userId);
    }

    /**
     * ADMIN 或 USER 角色都可以查看用户信息
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String getUserInfo(String userId) {
        // 获取用户信息
        return "User info for: " + userId;
    }

    /**
     * ADMIN 可以修改任何用户，普通用户只能修改自己
     */
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    public void updateUser(String userId, String newName) {
        // 更新用户逻辑
        System.out.println("Updating user " + userId + " to: " + newName);
    }

    /**
     * 公开方法，但可以获取当前登录用户信息
     */
    public void publicMethod() {
        String currentUserId = SecurityHelper.getCurUserId();
        String currentUserName = SecurityHelper.getCurUserName();

        System.out.println("Current user: " + currentUserName + " (ID: " + currentUserId + ")");
    }
}
