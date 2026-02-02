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

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Department Create Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class DepartmentCreateCommand {

  private String parentId;

  @NotBlank(message = "部门名称不能为空")
  @Size(max = 50, message = "部门名称长度不能超过50")
  private String deptName;

  @NotBlank(message = "部门编码不能为空")
  @Size(max = 50, message = "部门编码长度不能超过50")
  @Pattern(regexp = "^[A-Z0-9_]+$", message = "部门编码只能包含大写字母、数字和下划线")
  private String deptCode;

  private Integer sortOrder;

  private String leaderUserId;

  @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
  private String mobile;

  @Email(message = "邮箱格式不正确")
  private String email;

  /** Status: 1-Normal, 0-Disabled */
  private Short status;

  private String remark;

  private String createdBy;
}
