package io.github.loadup.modules.upms.domain.valueobject;

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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Status Value Object
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatus {

  public static final short NORMAL = 1;
  public static final short DISABLED = 0;
  public static final short LOCKED = 2;

  private short status;

  private String description;

  public static UserStatus normal() {
    return new UserStatus(NORMAL, "正常");
  }

  public static UserStatus disabled() {
    return new UserStatus(DISABLED, "停用");
  }

  public static UserStatus locked() {
    return new UserStatus(LOCKED, "锁定");
  }

  public static UserStatus of(short status) {
    return switch (status) {
      case NORMAL -> normal();
      case DISABLED -> disabled();
      case LOCKED -> locked();
      default -> throw new IllegalArgumentException("Invalid user status: " + status);
    };
  }

  public boolean isNormal() {
    return status == NORMAL;
  }

  public boolean isDisabled() {
    return status == DISABLED;
  }

  public boolean isLocked() {
    return status == LOCKED;
  }
}
