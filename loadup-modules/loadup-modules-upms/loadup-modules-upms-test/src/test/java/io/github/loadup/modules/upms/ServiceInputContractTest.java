/*
 * #%L
 * Loadup UPMS Test
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
package io.github.loadup.modules.upms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.upms.app.converter.UpmsDTOConverter;
import io.github.loadup.modules.upms.app.service.AuthenticationService;
import io.github.loadup.modules.upms.app.service.PasswordResetService;
import io.github.loadup.modules.upms.app.service.UserService;
import io.github.loadup.modules.upms.app.service.VerificationCodeService;
import io.github.loadup.modules.upms.app.strategy.LoginStrategyManager;
import io.github.loadup.modules.upms.client.command.UserCreateCommand;
import io.github.loadup.modules.upms.client.command.UserPasswordChangeCommand;
import io.github.loadup.modules.upms.client.command.UserPasswordResetCommand;
import io.github.loadup.modules.upms.client.command.UserRegisterCommand;
import io.github.loadup.modules.upms.client.facade.UserQueryFacade;
import io.github.loadup.modules.upms.client.query.UserPageQuery;
import io.github.loadup.modules.upms.domain.gateway.DepartmentGateway;
import io.github.loadup.modules.upms.domain.gateway.LoginLogGateway;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class ServiceInputContractTest {
    private final UserGateway users = mock(UserGateway.class);
    private final RoleGateway roles = mock(RoleGateway.class);
    private final PasswordEncoder passwords = mock(PasswordEncoder.class);

    private UserService service() {
        return new UserService(
                users,
                roles,
                mock(DepartmentGateway.class),
                passwords,
                mock(UpmsDTOConverter.class),
                Clock.systemUTC());
    }

    @Test
    void allPasswordWriteEntryPointsRejectInvalidInputBeforeSideEffects() {
        var service = service();
        var create = new UserCreateCommand();
        create.setPassword("short");
        var change = new UserPasswordChangeCommand();
        change.setOldPassword("old");
        change.setNewPassword("short");
        change.setConfirmPassword("short");
        var reset = new UserPasswordResetCommand();
        reset.setNewPassword("short");
        var register = new UserRegisterCommand();
        register.setPassword("short");
        var codes = mock(VerificationCodeService.class);
        var resetService = new PasswordResetService(users, codes, passwords, Clock.systemUTC());
        var auth = new AuthenticationService(
                users,
                roles,
                mock(LoginLogGateway.class),
                passwords,
                mock(LoginStrategyManager.class),
                mock(UserQueryFacade.class),
                mock(UpmsDTOConverter.class),
                Clock.systemUTC());
        assertThatExceptionOfType(CommonException.class).isThrownBy(() -> service.createUser(create));
        assertThatExceptionOfType(CommonException.class).isThrownBy(() -> service.changePassword(change));
        assertThatExceptionOfType(CommonException.class).isThrownBy(() -> resetService.resetPassword(reset));
        assertThatExceptionOfType(CommonException.class).isThrownBy(() -> auth.register(register));
        verifyNoInteractions(users, roles, passwords, codes);
    }

    @Test
    void defaultsPaginationAndPassesWhitelistedSortToPersistencePort() {
        var service = service();
        var query = new UserPageQuery();
        query.setPage(null);
        query.setSize(null);
        query.setSortBy("username");
        query.setSortOrder("ASC");
        when(users.findAll(any(), eq("username"), eq("ASC"))).thenReturn(PageResult.of(List.of(), 0, 1, 20));
        var page = service.queryUsers(query);
        assertThat(page.getPageInfo().pageIndex()).isEqualTo(1);
        assertThat(page.getPageInfo().pageSize()).isEqualTo(20);
        verify(users).findAll(argThat(p -> p.pageNum() == 1 && p.pageSize() == 20), eq("username"), eq("ASC"));
        clearInvocations(users);
        query.setSortBy("id DESC; DROP TABLE users");
        assertThatExceptionOfType(CommonException.class).isThrownBy(() -> service.queryUsers(query));
        query.setSortBy("id");
        query.setSize(101);
        assertThatExceptionOfType(CommonException.class).isThrownBy(() -> service.queryUsers(query));
        verifyNoInteractions(users);
    }
}
