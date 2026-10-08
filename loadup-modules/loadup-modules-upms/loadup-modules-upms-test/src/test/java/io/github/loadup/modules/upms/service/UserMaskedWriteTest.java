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
package io.github.loadup.modules.upms.service;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import io.github.loadup.modules.upms.app.service.UserService;
import io.github.loadup.modules.upms.client.command.UserCreateCommand;
import io.github.loadup.modules.upms.client.command.UserUpdateCommand;
import io.github.loadup.modules.upms.domain.gateway.DepartmentGateway;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserMaskedWriteTest {
    @Test
    void rejectsMaskBeforeReadingOrWritingDatabase() {
        var users = mock(UserGateway.class);
        var roles = mock(RoleGateway.class);
        var departments = mock(DepartmentGateway.class);
        var passwords = mock(PasswordEncoder.class);
        var service = new UserService(users, roles, departments, passwords);
        var create = new UserCreateCommand();
        create.setMobile("138****5678");
        assertThatIllegalArgumentException().isThrownBy(() -> service.createUser(create));
        var update = new UserUpdateCommand();
        update.setRealName("A****");
        assertThatIllegalArgumentException().isThrownBy(() -> service.updateUser(update));
        verifyNoInteractions(users, roles, departments, passwords);
    }
}
