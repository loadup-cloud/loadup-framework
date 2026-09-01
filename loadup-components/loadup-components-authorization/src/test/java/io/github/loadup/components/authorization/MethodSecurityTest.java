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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.components.authorization.config.AuthorizationAutoConfiguration;
import io.github.loadup.components.authorization.context.UserContext;
import io.github.loadup.components.authorization.model.LoadUpUser;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;

@SpringBootTest(classes = MethodSecurityTest.TestConfig.class)
class MethodSecurityTest {

    @Autowired
    private ProtectedService service;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void preAuthorize_allowsWhenRoleMatches() {
        UserContext.set(LoadUpUser.builder()
                .userId("u-1")
                .username("admin")
                .roles(List.of("ADMIN"))
                .build());

        assertThat(service.adminOnly()).isEqualTo("ok");
    }

    @Test
    void preAuthorize_deniesWhenRoleMissing() {
        UserContext.set(LoadUpUser.builder()
                .userId("u-2")
                .username("user")
                .roles(List.of("USER"))
                .build());

        assertThatThrownBy(() -> service.adminOnly()).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void preAuthorize_deniesWhenNotAuthenticated() {
        UserContext.clear();

        assertThatThrownBy(() -> service.adminOnly()).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void hasAuthority_matchesPermission() {
        UserContext.set(LoadUpUser.builder()
                .userId("u-3")
                .username("editor")
                .permissions(List.of("user:write"))
                .build());

        assertThat(service.canWrite()).isEqualTo("ok");
    }

    @SpringBootConfiguration
    @ImportAutoConfiguration(AuthorizationAutoConfiguration.class)
    static class TestConfig {

        @Bean
        ProtectedService protectedService() {
            return new ProtectedService();
        }
    }

    static class ProtectedService {

        @PreAuthorize("hasRole('ADMIN')")
        public String adminOnly() {
            return "ok";
        }

        @PreAuthorize("hasAuthority('user:write')")
        public String canWrite() {
            return "ok";
        }
    }
}
