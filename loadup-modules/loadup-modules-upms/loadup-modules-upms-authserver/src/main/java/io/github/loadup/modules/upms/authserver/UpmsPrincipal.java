package io.github.loadup.modules.upms.authserver;

import io.github.loadup.components.authserver.jwt.LoadUpSubject;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Authenticated UPMS identity used by Spring Authorization Server. */
public record UpmsPrincipal(String subjectId, String username, Collection<? extends GrantedAuthority> authorities)
        implements UserDetails, LoadUpSubject {

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
