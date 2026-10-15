/*
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
package io.github.loadup.components.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.authorization.context.UserContext;
import io.github.loadup.components.authorization.model.LoadUpUser;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    @Test
    void rolePredicatesSupportRawAndPrefixedRolesAndAnyAllSemantics() {
        var user = LoadUpUser.builder()
                .roles(List.of("ADMIN", "ROLE_AUDITOR"))
                .permissions(List.of("user:read"))
                .attributes(Map.of("tenant", "merchant-a"))
                .build();
        assertThat(user.hasRole("ADMIN")).isTrue();
        assertThat(user.hasRole("AUDITOR")).isTrue();
        assertThat(user.hasRole("ROLE_AUDITOR")).isTrue();
        assertThat(user.hasRole("MISSING")).isFalse();
        assertThat(user.hasAnyRole("MISSING", "AUDITOR")).isTrue();
        assertThat(user.hasAnyRole("MISSING", "OTHER")).isFalse();
        assertThat(user.hasAnyRole()).isFalse();
        assertThat(user.hasAllRoles("ADMIN", "AUDITOR")).isTrue();
        assertThat(user.hasAllRoles("ADMIN", "MISSING")).isFalse();
        assertThat(user.hasAllRoles()).isTrue();
        assertThat(user.hasPermission("user:read")).isTrue();
        assertThat(user.hasPermission("user:write")).isFalse();
        assertThat(user.getAttributes()).containsEntry("tenant", "merchant-a");
    }

    @Test
    void absentRolesAndPermissionsNeverGrantAccess() {
        var user = new LoadUpUser();
        assertThat(user.hasRole("ADMIN")).isFalse();
        assertThat(user.hasAnyRole("ADMIN")).isFalse();
        assertThat(user.hasAllRoles("ADMIN")).isFalse();
        assertThat(user.hasPermission("user:read")).isFalse();
        user.setRoles(null);
        user.setPermissions(null);
        assertThat(user.hasRole("ADMIN")).isFalse();
        assertThat(user.hasAnyRole((String[]) null)).isFalse();
        assertThat(user.hasAllRoles((String[]) null)).isFalse();
        assertThat(user.hasPermission(null)).isFalse();
        UserContext.set(user);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .isEmpty();
    }

    @Test
    void beanStyleUserConstructionBridgesIdentityAndAuthorities() {
        var user = new LoadUpUser();
        user.setUserId("merchant-user");
        user.setUsername("auditor");
        user.setRoles(List.of("ROLE_AUDITOR"));
        user.setPermissions(List.of("audit:read"));
        user.setAttributes(Map.of("tenant", "merchant-a"));
        UserContext.set(user);
        assertThat(user.hasAllRoles("AUDITOR")).isTrue();
        assertThat(user.hasPermission("audit:read")).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("merchant-user");
        assertThat(UserContext.getUsername()).isEqualTo("auditor");
        assertThat(UserContext.get().getAttributes()).containsEntry("tenant", "merchant-a");
        assertThat(user.toString()).startsWith("{").contains("merchant-user");
    }

    @Test
    void foreignAndAnonymousPrincipalsAreNotPresentedAsLoadUpUsers() {
        java.security.Principal principal = () -> "external-user";
        SecurityContextHolder.getContext()
                .setAuthentication(UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of()));
        assertThat(UserContext.get()).isNull();
        assertThat(UserContext.isPresent()).isFalse();
        assertThat(UserContext.getUserId()).isNull();
        assertThat(UserContext.getUsername()).isNull();
        SecurityContextHolder.getContext()
                .setAuthentication(new AnonymousAuthenticationToken(
                        "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
        assertThat(UserContext.get()).isNull();
        UserContext.clear();
        assertThat(UserContext.getUserId()).isNull();
        assertThat(UserContext.getUsername()).isNull();
    }
}
