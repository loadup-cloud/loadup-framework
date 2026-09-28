/*-
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

import io.github.loadup.commons.request.query.IdQuery;
import io.github.loadup.modules.upms.client.dto.UserDetailDTO;
import io.github.loadup.modules.upms.client.service.UserQueryService;
import org.springframework.stereotype.Service;

@Service
public class UserQueryServiceImpl implements UserQueryService {
    private final UserService userService;

    public UserQueryServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetailDTO getUserById(String userId) {
        return userService.getUserById(new IdQuery(userId));
    }
}
