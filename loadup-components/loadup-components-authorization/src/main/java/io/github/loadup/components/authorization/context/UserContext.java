package io.github.loadup.components.authorization.context;

import io.github.loadup.components.authorization.model.LoadUpUser;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Thin adapter over {@link SecurityContextHolder} for the current user.
 *
 * <p>This is the LoadUp facade for accessing the authenticated user. It never stores state
 * itself: {@link #set(LoadUpUser)} writes an {@link Authentication} into the Spring Security
 * context and {@link #get()} reads it back. Method-level authorization is enforced by Spring
 * Security ({@code @PreAuthorize}, {@code @EnableMethodSecurity}) using the authorities derived
 * from the user's roles and permissions.
 *
 * <p>Context propagation (thread pools, async) follows the {@code SecurityContextHolder}
 * strategy configured by the application; no additional manual propagation is required.
 */
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
