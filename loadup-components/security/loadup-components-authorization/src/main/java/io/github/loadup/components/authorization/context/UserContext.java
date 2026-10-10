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
package io.github.loadup.components.authorization.context;

import io.github.loadup.components.authorization.model.LoadUpUser;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class UserContext {

    private UserContext() {
        // Utility class
    }

    /**
     * Write the user into the Spring Security context.
     *
     * <p>Roles are exposed as {@code ROLE_<role>} (plus the raw value) authorities and
     * permissions as plain authorities, so both {@code hasRole(...)} and
     * {@code hasAuthority(...)} expressions work with {@code @PreAuthorize}.
     *
     * @param user the user to set; {@code null} clears the context
     */
    public static void set(LoadUpUser user) {
        if (user == null) {
            clear();
            return;
        }
        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(user, null, authoritiesOf(user));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Get the current user from the Spring Security context.
     *
     * @return current user, or {@code null} when no {@link LoadUpUser} principal is set
     */
    public static LoadUpUser get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoadUpUser user)) {
            return null;
        }
        return user;
    }

    /**
     * Get the current user ID.
     *
     * @return user ID, or {@code null} when no user is set
     */
    public static String getUserId() {
        LoadUpUser user = get();
        return user != null ? user.getUserId() : null;
    }

    /**
     * Get the current username.
     *
     * @return username, or {@code null} when no user is set
     */
    public static String getUsername() {
        LoadUpUser user = get();
        return user != null ? user.getUsername() : null;
    }

    /**
     * Clear the current user from the Spring Security context.
     */
    public static void clear() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Check whether a user is currently set in context.
     *
     * @return {@code true} when a user is present, {@code false} otherwise
     */
    public static boolean isPresent() {
        return get() != null;
    }

    private static List<GrantedAuthority> authoritiesOf(LoadUpUser user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRoles() != null) {
            for (String role : user.getRoles()) {
                authorities.add(new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role));
                if (!role.startsWith("ROLE_")) {
                    authorities.add(new SimpleGrantedAuthority(role));
                }
            }
        }
        if (user.getPermissions() != null) {
            for (String permission : user.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission));
            }
        }
        return authorities;
    }
}
