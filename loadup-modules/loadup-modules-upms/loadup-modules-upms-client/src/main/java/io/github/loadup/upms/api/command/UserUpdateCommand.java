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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data

/**
 * @since 1.0.0
 * @author LoadUp Framework
 *     <p>User Update Command
 */
public class UserUpdateCommand {

  private String updatedBy;

  private String remark;

  private List<String> roleIds;

  private Short status; // 1-Normal, 0-Disabled

  private LocalDate birthday;

  private Short gender; // 0-Unknown, 1-Male, 2-Female

  private String avatar;

  @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
  private String mobile;

  @Email(message = "邮箱格式不正确")
  private String email;

  private String deptId;

  @Size(max = 50, message = "真实姓名长度不能超过50")
  private String realName;

  @Size(max = 50, message = "昵称长度不能超过50")
  private String nickname;

  @NotNull(message = "用户ID不能为空")
  private String id;
}
