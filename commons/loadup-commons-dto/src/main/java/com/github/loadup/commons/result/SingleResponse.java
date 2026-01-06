package com.github.loadup.commons.result;

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

import java.io.Serial;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SingleResponse<T> extends Response {
  @Serial private static final long serialVersionUID = -3426130425512449976L;

  private T data;

  public static <T> SingleResponse<T> of(T data) {
    SingleResponse<T> response = new SingleResponse<>();
    if (Objects.isNull(data)) {
      response.setResult(Result.buildFailure(CommonResultCodeEnum.NOT_FOUND));
      return response;
    }
    response.setResult(Result.buildSuccess());
    response.setData(data);
    return response;
  }

  public static SingleResponse<Void> buildSuccess() {
    SingleResponse<Void> response = new SingleResponse<>();
    response.setResult(Result.buildSuccess());
    return response;
  }

  public static SingleResponse<Void> buildFailure() {
    SingleResponse<Void> response = new SingleResponse<>();
    response.setResult(Result.buildFailure());
    return response;
  }

  public static SingleResponse<Void> buildFailure(String errCode) {
    SingleResponse<Void> response = new SingleResponse<>();
    response.setResult(Result.buildFailure(errCode));
    return response;
  }

  public static SingleResponse<Void> buildFailure(ResultCode errCode) {
    SingleResponse<Void> response = new SingleResponse<>();
    response.setResult(Result.buildFailure(errCode));
    return response;
  }

  public static SingleResponse<Void> buildFailure(ResultCode errCode, String errMessage) {
    SingleResponse<Void> response = new SingleResponse<>();
    response.setResult(Result.buildFailure(errCode, errMessage));
    return response;
  }

  public static Response buildFailure(String errCode, String errMessage) {
    Response response = new Response();
    response.setResult(Result.buildFailure(errCode, errMessage));
    return response;
  }
}
