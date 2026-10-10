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

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.upms.client.command.RoleCreateCommand;
import io.github.loadup.modules.upms.client.command.RoleUpdateCommand;
import io.github.loadup.modules.upms.client.dto.RoleDTO;
import io.github.loadup.modules.upms.client.query.RolePageQuery;
import java.util.List;

/** Public business contract for RoleService. */
public interface RoleFacade {
    RoleDTO createRole(RoleCreateCommand command);

    RoleDTO updateRole(RoleUpdateCommand command);

    void deleteRole(String id);

    RoleDTO getRoleById(String id);

    PageDTO<RoleDTO> queryRoles(RolePageQuery query);

    List<RoleDTO> getRoleTree();

    void assignRoleToUser(String roleId, String userId);

    void removeRoleFromUser(String roleId, String userId);

    void assignPermissionsToRole(String roleId, List<String> permissionIds);
}
