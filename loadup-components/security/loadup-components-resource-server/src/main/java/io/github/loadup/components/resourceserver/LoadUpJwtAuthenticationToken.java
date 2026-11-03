package io.github.loadup.components.resourceserver;

import io.github.loadup.components.authorization.model.LoadUpUser;
import java.util.Collection;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Keeps the verified JWT available after ProviderManager's standard credentials erasure. */
@NullMarked
final class LoadUpJwtAuthenticationToken extends JwtAuthenticationToken {
    private final LoadUpUser principal;

    LoadUpJwtAuthenticationToken(Jwt jwt, LoadUpUser principal, Collection<? extends GrantedAuthority> authorities) {
        super(jwt, authorities);
        this.principal = principal;
    }

    @Override
    public LoadUpUser getPrincipal() {
        return principal;
    }
}
