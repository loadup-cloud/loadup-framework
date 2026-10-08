package io.github.loadup.components.resourceserver;

import io.github.loadup.components.authorization.model.LoadUpUser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Converts a verified {@link Jwt} into the standard Spring Security {@link Authentication} with a
 * {@link LoadUpUser} principal.
 *
 * <p>Claim contract (self-contained JWT): {@code sub} = userId, {@code username},
 * {@code roles} and {@code permissions} as arrays (or comma-separated strings). Roles are exposed
 * as {@code ROLE_<role>} plus the raw value; permissions are exposed as raw authorities, so both
 * {@code hasRole(...)} and {@code hasAuthority(...)} work in route-level and method-level rules.
 */
public class LoadUpJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        LoadUpUser user = LoadUpUser.builder()
                .userId(jwt.getSubject())
                .username(stringClaim(jwt, "username"))
                .roles(stringListClaim(jwt, "roles"))
                .permissions(stringListClaim(jwt, "permissions"))
                .build();
        return UsernamePasswordAuthenticationToken.authenticated(user, jwt, authoritiesOf(user));
    }

    private static List<GrantedAuthority> authoritiesOf(LoadUpUser user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRoles() != null) {
            for (String role : user.getRoles()) {
                if (role == null || role.isBlank()) {
                    continue;
                }
                authorities.add(new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role));
                if (!role.startsWith("ROLE_")) {
                    authorities.add(new SimpleGrantedAuthority(role));
                }
            }
        }
        if (user.getPermissions() != null) {
            for (String permission : user.getPermissions()) {
                if (permission != null && !permission.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority(permission));
                }
            }
        }
        return authorities;
    }

    private static String stringClaim(Jwt jwt, String name) {
        Object value = jwt.getClaims().get(name);
        return value != null ? String.valueOf(value) : null;
    }

    private static List<String> stringListClaim(Jwt jwt, String name) {
        Object value = jwt.getClaims().get(name);
        if (value == null) {
            return List.of();
        }
        if (value instanceof Collection<?> collection) {
            List<String> result = new ArrayList<>(collection.size());
            for (Object item : collection) {
                if (item != null) {
                    result.add(String.valueOf(item));
                }
            }
            return result;
        }
        String text = String.valueOf(value);
        if (text.isBlank()) {
            return List.of();
        }
        return List.of(text.split(","));
    }
}
