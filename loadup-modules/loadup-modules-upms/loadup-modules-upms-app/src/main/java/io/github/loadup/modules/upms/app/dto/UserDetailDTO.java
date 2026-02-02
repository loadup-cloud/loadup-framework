package io.github.loadup.modules.upms.app.dto;

/*-
 * #%L
 * Loadup Modules UPMS App Layer
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

import io.github.loadup.upms.api.dto.RoleDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Detail DTO
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDTO {

  private String id;
  private String username;
  private String nickname;
  private String realName;
  private String deptId;
  private String deptName;
  private String email;
  private Boolean emailVerified;
  private String mobile;
  private Boolean mobileVerified;
  private String avatar;
  private Short gender;
  private LocalDate birthday;
  private Short status;
  private LocalDateTime lastLoginTime;
  private String lastLoginIp;
  private List<RoleDTO> roles;
  private String remark;
  private LocalDateTime createdTime;
  private LocalDateTime updatedTime;
}
