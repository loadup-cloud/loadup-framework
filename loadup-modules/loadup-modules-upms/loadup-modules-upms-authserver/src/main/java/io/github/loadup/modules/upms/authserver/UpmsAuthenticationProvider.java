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

import io.github.loadup.modules.upms.client.command.UserLoginCommand;
import io.github.loadup.modules.upms.client.dto.AuthenticatedUser;
import io.github.loadup.modules.upms.client.facade.AuthenticationFacade;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.service.UserPermissionService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** Delegates user credential checks to UPMS without issuing tokens. */
public final class UpmsAuthenticationProvider implements AuthenticationProvider {
    private final AuthenticationFacade authenticationService;
    private final RoleGateway roleGateway;
    private final UserPermissionService permissionService;

    public UpmsAuthenticationProvider(
            AuthenticationFacade authenticationService,
            RoleGateway roleGateway,
            UserPermissionService permissionService) {
        this.authenticationService = authenticationService;
        this.roleGateway = roleGateway;
        this.permissionService = permissionService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        return authenticate(authentication, null);
    }

    /** Authenticate a first-party HTTP login with its server-observed client address. */
    public Authentication authenticate(Authentication authentication, String ipAddress) {
        UserLoginCommand command = new UserLoginCommand();
        command.setUsername(authentication.getName());
        command.setPassword(String.valueOf(authentication.getCredentials()));
        command.setIpAddress(ipAddress);
        AuthenticatedUser user;
        try {
            user = authenticationService.login(command);
        } catch (RuntimeException ex) {
            throw new BadCredentialsException("Invalid username or password", ex);
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        roleGateway.findByUserId(user.getUserId()).stream()
                .filter(role -> role.isEnabled()
                        && role.getRoleCode() != null
                        && !role.getRoleCode().isBlank())
                .forEach(role -> authorities.add(new SimpleGrantedAuthority(
                        role.getRoleCode().startsWith("ROLE_") ? role.getRoleCode() : "ROLE_" + role.getRoleCode())));
        permissionService
                .getUserPermissionCodes(user.getUserId())
                .forEach(code -> authorities.add(new SimpleGrantedAuthority(code)));
        UpmsPrincipal principal = new UpmsPrincipal(user.getUserId(), user.getUsername(), authorities);
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
