package io.github.loadup.upms.api.command;

/*-
 * #%L
 * Loadup Modules UPMS Client Layer
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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Permission Create Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class PermissionCreateCommand {

  private String parentId;

  @NotBlank(message = "权限名称不能为空")
  @Size(max = 50, message = "权限名称长度不能超过50")
  private String permissionName;

  @NotBlank(message = "权限编码不能为空")
  @Size(max = 100, message = "权限编码长度不能超过100")
  @Pattern(regexp = "^[a-z:]+$", message = "权限编码只能包含小写字母和冒号")
  private String permissionCode;

  /** Permission type: 1-Menu, 2-Button, 3-API */
  private Short permissionType;

  private String resourcePath;

  private String httpMethod;

  private String icon;

  private String componentPath;

  private Integer sortOrder;

  private Boolean visible;

  /** Status: 1-Normal, 0-Disabled */
  private Short status;

  private String remark;

  private String createdBy;
}
