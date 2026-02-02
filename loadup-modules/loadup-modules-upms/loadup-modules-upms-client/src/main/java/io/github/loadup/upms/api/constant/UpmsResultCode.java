package io.github.loadup.upms.api.constant;

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

import io.github.loadup.commons.result.ResultCode;
import io.github.loadup.commons.result.ResultStatusEnum;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.Strings;

@Getter
@AllArgsConstructor
public enum UpmsResultCode implements ResultCode {
  USER_NOT_FOUND(ResultStatusEnum.FAIL, "用户不存在"),
  USER_LOCKED(ResultStatusEnum.FAIL, "用户已被锁定"),
  PASSWORD_ERROR(ResultStatusEnum.FAIL, "密码错误"),
  UNAUTHORIZED(ResultStatusEnum.FAIL, "无权访问该资源"),
  ROLE_NOT_FOUND(ResultStatusEnum.FAIL, "角色不存在");

  private final String status;

  private final String message;

  UpmsResultCode(ResultStatusEnum status, String message) {
    this.status = status.getCode();
    this.message = message;
  }

  public static UpmsResultCode getByResultCode(String resultCode) {
    return Arrays.stream(UpmsResultCode.values())
        .filter(value -> Strings.CI.equals(value.getCode(), resultCode))
        .findFirst()
        .orElse(null);
  }

  @Override
  public String getCode() {
    return name();
  }

  @Override
  public String getStatus() {
    return status;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
