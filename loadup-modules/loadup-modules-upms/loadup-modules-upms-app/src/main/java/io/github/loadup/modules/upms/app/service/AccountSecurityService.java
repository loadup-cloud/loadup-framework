/*
 * #%L
 * Loadup Modules UPMS App Layer
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
package io.github.loadup.modules.upms.app.service;

import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.commons.dto.PageQuery;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.upms.app.converter.UpmsDTOConverter;
import io.github.loadup.modules.upms.client.command.UserPasswordChangeCommand;
import io.github.loadup.modules.upms.client.dto.LoginEntryDTO;
import io.github.loadup.modules.upms.client.dto.SecurityOverviewDTO;
import io.github.loadup.modules.upms.domain.entity.LoginLog;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.LoginLogGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import java.util.List;
import org.springframework.stereotype.Service;

/** Self-service account security queries and password updates. */
@Service
public class AccountSecurityService implements io.github.loadup.modules.upms.client.facade.AccountSecurityFacade {
    private final UpmsDTOConverter dtoConverter;
    private final UserGateway users;
    private final LoginLogGateway logins;
    private final UserService userService;

    public AccountSecurityService(
            UserGateway users, LoginLogGateway logins, UserService userService, UpmsDTOConverter dtoConverter) {
        this.dtoConverter = dtoConverter;
        this.users = users;
        this.logins = logins;
        this.userService = userService;
    }

    public SecurityOverviewDTO overview(String userId) {
        User user =
                users.findById(requiredUser(userId)).orElseThrow(() -> new IllegalArgumentException("user not found"));
        return dtoConverter.toSecurityOverview(user);
    }

    public PageDTO<LoginEntryDTO> loginHistory(String userId, int page, int size) {
        if (page < 1 || size < 1 || size > 100) throw new IllegalArgumentException("invalid page or size");
        PageResult<LoginLog> result = logins.findByUserId(requiredUser(userId), PageQuery.of(page, size));
        List<LoginEntryDTO> entries =
                result.records().stream().map(dtoConverter::toLoginEntry).toList();
        return PageDTO.of(entries, result.total(), result.page(), result.size());
    }

    public void changePassword(String userId, String oldPassword, String newPassword, String confirmPassword) {
        userService.changePassword(
                new UserPasswordChangeCommand(requiredUser(userId), oldPassword, newPassword, confirmPassword));
    }

    private static String requiredUser(String userId) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId is required");
        return userId;
    }
}
