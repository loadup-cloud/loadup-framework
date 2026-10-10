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

import io.github.loadup.modules.upms.client.command.PermissionCreateCommand;
import io.github.loadup.modules.upms.client.command.PermissionUpdateCommand;
import io.github.loadup.modules.upms.client.dto.PermissionDTO;
import java.util.List;

/** Public business contract for PermissionService. */
public interface PermissionFacade {
    PermissionDTO createPermission(PermissionCreateCommand command);

    PermissionDTO updatePermission(PermissionUpdateCommand command);

    void deletePermission(String id);

    PermissionDTO getPermissionById(String id);

    List<PermissionDTO> getPermissionTree();

    List<PermissionDTO> getPermissionsByType(Short permissionType);

    List<PermissionDTO> getUserPermissions(String userId);

    List<PermissionDTO> getUserMenuTree(String userId);
}
