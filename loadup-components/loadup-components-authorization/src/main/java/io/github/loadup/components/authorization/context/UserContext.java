package io.github.loadup.components.authorization.context;

/*-
 * #%L
 * LoadUp Components Authorization
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

import io.github.loadup.components.authorization.model.LoadUpUser;

/**
 * Thread-local holder for current user context.
 *
 * <p>This class provides static methods to access the current authenticated user
 * within the same thread context.</p>
 *
 * <p><b>Important:</b> Always call {@link #clear()} after request processing to avoid
 * memory leaks, especially when using thread pools.</p>
 */
public final class UserContext {

    private static final ThreadLocal<LoadUpUser> USER_HOLDER = new ThreadLocal<>();

    private UserContext() {
        // Utility class
    }

    /**
     * Set the current user in thread-local context
     *
     * @param user the user to set
     */
    public static void set(LoadUpUser user) {
        USER_HOLDER.set(user);
    }

    /**
     * Get the current user from thread-local context
     *
     * @return current user, or null if not set
     */
    public static LoadUpUser get() {
        return USER_HOLDER.get();
    }

    /**
     * Get current user ID
     *
     * @return user ID, or null if no user is set
     */
    public static String getUserId() {
        LoadUpUser user = get();
        return user != null ? user.getUserId() : null;
    }

    /**
     * Get current username
     *
     * @return username, or null if no user is set
     */
    public static String getUsername() {
        LoadUpUser user = get();
        return user != null ? user.getUsername() : null;
    }

    /**
     * Clear the current user from thread-local context.
     *
     * <p><b>Important:</b> This must be called to prevent memory leaks.</p>
     */
    public static void clear() {
        USER_HOLDER.remove();
    }

    /**
     * Check if a user is currently set in context
     *
     * @return true if user is present, false otherwise
     */
    public static boolean isPresent() {
        return USER_HOLDER.get() != null;
    }
}
