/*-
 * #%L
 * LoadUp UPMS Authorization Server Adapter
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
package io.github.loadup.modules.upms.authserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.loadup.modules.upms.client.command.UserLoginCommand;
import io.github.loadup.modules.upms.client.dto.AuthenticatedUser;
import io.github.loadup.modules.upms.client.facade.AuthenticationFacade;
import io.github.loadup.modules.upms.domain.entity.Role;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.service.UserPermissionService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

class UpmsAuthenticationProviderTest {
    @Test
    void delegatesCredentialsAndReturnsUserIdAndAuthorities() {
        AuthenticationFacade authenticationService = mock(AuthenticationFacade.class);
        RoleGateway roleGateway = mock(RoleGateway.class);
        UserPermissionService permissionService = mock(UserPermissionService.class);
        when(authenticationService.login(any(UserLoginCommand.class)))
                .thenReturn(AuthenticatedUser.builder()
                        .userId("u-1")
                        .username("ada")
                        .build());
        Role role = new Role();
        role.setRoleCode("ADMIN");
        role.setStatus((short) 1);
        when(roleGateway.findByUserId("u-1")).thenReturn(List.of(role));
        when(permissionService.getUserPermissionCodes("u-1")).thenReturn(Set.of("user:write"));

        var provider = new UpmsAuthenticationProvider(authenticationService, roleGateway, permissionService);
        var result = provider.authenticate(new UsernamePasswordAuthenticationToken("ada", "secret"));

        ArgumentCaptor<UserLoginCommand> command = ArgumentCaptor.forClass(UserLoginCommand.class);
        verify(authenticationService).login(command.capture());
        assertThat(command.getValue().getUsername()).isEqualTo("ada");
        assertThat(command.getValue().getPassword()).isEqualTo("secret");
        assertThat(((UpmsPrincipal) result.getPrincipal()).subjectId()).isEqualTo("u-1");
        assertThat(result.getAuthorities()).extracting("authority").contains("ROLE_ADMIN", "user:write");
    }
}
