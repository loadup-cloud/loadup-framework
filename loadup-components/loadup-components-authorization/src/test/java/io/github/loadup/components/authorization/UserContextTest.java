package io.github.loadup.components.authorization;

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
