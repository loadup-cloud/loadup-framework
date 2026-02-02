package io.github.loadup.modules.upms.adapter.web.request;

/*-
 * #%L
 * Loadup Modules UPMS Adapter Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * Assign Permissions Request
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class AssignPermissionsRequest {
  @NotNull(message = "角色ID不能为空")
  private String roleId;

  @NotEmpty(message = "权限ID列表不能为空")
  private List<String> permissionIds;
}
