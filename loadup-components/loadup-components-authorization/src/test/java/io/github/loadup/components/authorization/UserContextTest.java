package io.github.loadup.components.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.authorization.context.UserContext;
import io.github.loadup.components.authorization.model.LoadUpUser;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class UserContextTest {

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void set_get_roundTrip() {
        LoadUpUser user = LoadUpUser.builder().userId("u-1").username("admin").build();

        UserContext.set(user);

        assertThat(UserContext.isPresent()).isTrue();
        assertThat(UserContext.get()).isSameAs(user);
        assertThat(UserContext.getUserId()).isEqualTo("u-1");
        assertThat(UserContext.getUsername()).isEqualTo("admin");
    }

    @Test
    void clear_removesUserAndAuthentication() {
        UserContext.set(LoadUpUser.builder().userId("u-1").username("admin").build());

        UserContext.clear();

        assertThat(UserContext.isPresent()).isFalse();
        assertThat(UserContext.get()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void rolesAndPermissions_areExposedAsAuthorities() {
        LoadUpUser user = LoadUpUser.builder()
                .userId("u-1")
                .username("admin")
                .roles(List.of("ADMIN", "ROLE_AUDITOR"))
                .permissions(List.of("user:read", "user:delete"))
                .build();

        UserContext.set(user);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isSameAs(user);
        assertThat(authentication.getName()).isEqualTo("u-1");
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ADMIN", "ROLE_AUDITOR", "user:read", "user:delete");
    }

    @Test
    void setNull_clearsContext() {
        UserContext.set(LoadUpUser.builder().userId("u-1").username("admin").build());

        UserContext.set(null);

        assertThat(UserContext.isPresent()).isFalse();
    }
}
