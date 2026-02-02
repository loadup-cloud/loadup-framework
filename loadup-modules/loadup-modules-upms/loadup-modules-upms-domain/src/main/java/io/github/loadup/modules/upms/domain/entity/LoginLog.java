package io.github.loadup.modules.upms.domain.entity;

/*-
 * #%L
 * Loadup Modules UPMS Domain Layer
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

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Log Entity - User login/logout audit log
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginLog {

  private String id;

  private String userId;

  private String username;

  private LocalDateTime loginTime;

  private LocalDateTime logoutTime;

  private String ipAddress;

  private String loginLocation;

  private String browser;

  private String os;

  /** Login status: 1-Success, 0-Failure */
  private Short loginStatus;

  private String loginMessage;

  public boolean isSuccess() {
    return loginStatus != null && loginStatus == 1;
  }
}
