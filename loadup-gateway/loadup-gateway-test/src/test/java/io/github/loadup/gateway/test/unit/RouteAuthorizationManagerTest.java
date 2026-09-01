package io.github.loadup.gateway.test.unit;

/*-
 * #%L
 * LoadUp Gateway Test
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

import io.github.loadup.gateway.webmvc.security.RouteAuthorizationManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RouteAuthorizationManager")
class RouteAuthorizationManagerTest {

    @Test
    @DisplayName("comma-separated permission lists compile to hasAnyAuthority")
    void permissionListCompilesToHasAnyAuthority() {
        assertThat(RouteAuthorizationManager.toSpel("user:list,user:delete"))
                .isEqualTo("hasAnyAuthority('user:list', 'user:delete')");
    }

    @Test
    @DisplayName("full SpEL expressions pass through unchanged")
    void fullSpelPassesThrough() {
        assertThat(RouteAuthorizationManager.toSpel("hasRole('ADMIN')")).isEqualTo("hasRole('ADMIN')");
        assertThat(RouteAuthorizationManager.toSpel("hasAnyAuthority('a','b') and isAuthenticated()"))
                .isEqualTo("hasAnyAuthority('a','b') and isAuthenticated()");
    }
}
