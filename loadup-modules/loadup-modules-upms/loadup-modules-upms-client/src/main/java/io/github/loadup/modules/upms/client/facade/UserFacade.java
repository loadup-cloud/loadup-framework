/*
 * #%L
 * Loadup Modules UPMS Client Layer
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
package io.github.loadup.modules.upms.client.facade;

import io.github.loadup.commons.request.query.IdQuery;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.upms.client.command.UserCreateCommand;
import io.github.loadup.modules.upms.client.command.UserPasswordChangeCommand;
import io.github.loadup.modules.upms.client.command.UserUpdateCommand;
import io.github.loadup.modules.upms.client.dto.UserDetailDTO;
import io.github.loadup.modules.upms.client.query.UserQuery;

/** Public business contract for UserService. */
public interface UserFacade extends UserQueryFacade {
    UserDetailDTO createUser(UserCreateCommand command);

    UserDetailDTO updateUser(UserUpdateCommand command);

    void deleteUser(String id);

    UserDetailDTO getUserById(IdQuery idQuery);

    PageDTO<UserDetailDTO> queryUsers(UserQuery query);

    void changePassword(UserPasswordChangeCommand command);

    void lockUser(String id);

    void unlockUser(String id);

    UserDetailDTO getUserById(String userId);
}
