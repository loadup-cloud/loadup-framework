package com.github.loadup.commons.enums;

/*-
 * #%L
 * loadup-commons-dto
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import com.github.loadup.commons.result.ResultCode;
import com.github.loadup.commons.result.ResultStatusEnum;
import java.util.Arrays;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum CommonResultCodeEnum implements ResultCode {
  SUCCESS(ResultStatusEnum.SUCCESS, "Success."),
  UNKNOWN(ResultStatusEnum.UNKNOWN, "Unknown failed."),
  PARAM_ILLEGAL(ResultStatusEnum.FAIL, "Parameter illegal."),
  PROCESS_FAIL(ResultStatusEnum.FAIL, "Process fail."),
  ACCESS_DENIED(ResultStatusEnum.FAIL, "Access denied."),
  INVALID_CLIENT(ResultStatusEnum.FAIL, "Invalid client."),
  NOT_FOUND(ResultStatusEnum.FAIL, "Key is not found."),
  SYS_ERROR(ResultStatusEnum.FAIL, "System error."),
  ;
  private String status;

  private String message;

  CommonResultCodeEnum(ResultStatusEnum status, String message) {
    this.status = status.getCode();
    this.message = message;
  }

  public static CommonResultCodeEnum getByResultCode(String resultCode) {
    return Arrays.stream(CommonResultCodeEnum.values())
        .filter(value -> StringUtils.equals(value.getCode(), resultCode))
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
