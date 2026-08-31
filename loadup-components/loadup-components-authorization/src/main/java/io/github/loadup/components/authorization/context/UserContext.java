package io.github.loadup.components.authorization.context;

/*-
 * #%L
 * LoadUp Components Authorization
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
