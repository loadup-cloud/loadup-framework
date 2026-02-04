package io.github.loadup.components.security.util;

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

import io.github.loadup.components.security.core.LoadUpUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helper class to access current authenticated user.
 *
 * <p>Note: SecurityContext should be populated by Gateway before business logic execution.</p>
 */
public class SecurityHelper {

    /**
     * Get current username
     */
    public static String getCurUserName() {
        LoadUpUser user = getCurUser();
        return null != user ? user.getUsername() : null;
    }

    /**
     * Get current LoadUpUser from SecurityContext
     */
    public static LoadUpUser getCurUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoadUpUser) {
            return (LoadUpUser) principal;
        }
        return null;
    }

    /**
     * Get current user ID
     */
    public static String getCurUserId() {
        LoadUpUser user = getCurUser();
        return null != user ? user.getUserId() : null;
    }
}
