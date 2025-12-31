package com.github.loadup.commons.result;

/*-
 * #%L
 * loadup-commons-lang
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

import java.io.Serial;
import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.github.loadup.commons.dto.DTO;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Lise
 * @since 1.0.0
 */
@Getter
@Setter
public class Result extends DTO implements ResultCode, Serializable {

  @Serial private static final long serialVersionUID = -6017673453793484984L;

  private String code;

  /** resultCode */
  private String status;

  /** resultMsg */
  private String message;

  public static Result buildSuccess() {
    Result result = new Result();
    result.setCode(CommonResultCodeEnum.SUCCESS.getCode());
    result.setStatus(ResultStatusEnum.SUCCESS.getCode());
    result.setMessage(CommonResultCodeEnum.SUCCESS.getMessage());
    return result;
  }

  public static Result buildFailure() {
    Result result = new Result();
    result.setCode(CommonResultCodeEnum.PROCESS_FAIL.getCode());
    result.setStatus(CommonResultCodeEnum.PROCESS_FAIL.getStatus());
    result.setMessage(CommonResultCodeEnum.PROCESS_FAIL.getMessage());
    return result;
  }

  public static Result buildFailure(String errorCode, String errorMessage) {
    Result result = new Result();
    CommonResultCodeEnum resultCodeEnum = CommonResultCodeEnum.getByResultCode(errorCode);
    result.setCode(resultCodeEnum.getCode());
    result.setStatus(resultCodeEnum.getStatus());
    result.setMessage(resultCodeEnum.getMessage());
    if (StringUtils.isNotBlank(errorMessage)) {
      result.setMessage(errorMessage);
    }
    return result;
  }

  public static Result buildFailure(String errorCode) {
    Result result = new Result();
    CommonResultCodeEnum resultCodeEnum = CommonResultCodeEnum.getByResultCode(errorCode);
    result.setCode(resultCodeEnum.getCode());
    result.setStatus(resultCodeEnum.getStatus());
    result.setMessage(resultCodeEnum.getMessage());
    return result;
  }

  public static Result buildFailure(ResultCode errorCode, String errorMessage) {
    Result result = new Result();
    result.setCode(errorCode.getCode());
    result.setStatus(errorCode.getStatus());
    result.setMessage(errorCode.getMessage());
    if (StringUtils.isNotBlank(errorMessage)) {
      result.setMessage(errorMessage);
    }
    return result;
  }

  public static Result buildFailure(ResultCode errorCode) {
    Result result = new Result();
    result.setCode(errorCode.getCode());
    result.setStatus(errorCode.getStatus());
    result.setMessage(errorCode.getMessage());
    return result;
  }
}
