package io.github.loadup.upms.api.dto;

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

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户详情返回对象")
public class UserDetailDTO implements Serializable {

  @Schema(description = "用户ID")
  private String id;

  @Schema(description = "账号")
  private String account;

  @Schema(description = "昵称")
  private String nickname;

  @Schema(description = "真实姓名")
  private String realName;

  @Schema(description = "头像地址")
  private String avatar;

  @Schema(description = "手机号")
  private String mobile;

  @Schema(description = "邮箱")
  private String email;

  @Schema(description = "性别：0-未知，1-男，2-女")
  private Integer gender;

  @Schema(description = "状态：0-正常，1-停用")
  private Integer status;

  @Schema(description = "所属部门ID")
  private String deptId;

  @Schema(description = "所属部门名称")
  private String deptName;

  @Schema(description = "最后登录时间")
  private LocalDateTime lastLoginTime;

  @Schema(description = "创建时间")
  private LocalDateTime createdAt;

  private List<String> roles;
  private List<String> permissions;
}
