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
import java.util.*;

public class MultiResponse<T> extends Response {
  @Serial private static final long serialVersionUID = 195635483708408296L;

  private Collection<T> data;

  public static <T> MultiResponse<T> of(Collection<T> data) {
    MultiResponse<T> response = new MultiResponse<>();
    if (Objects.isNull(data) || data.isEmpty()) {
      response.setResult(Result.buildFailure(CommonResultCodeEnum.NOT_FOUND));
      return response;
    }
    response.setResult(Result.buildSuccess());
    response.setData(data);
    return response;
  }

  public static MultiResponse<Void> buildSuccess() {
    MultiResponse<Void> response = new MultiResponse<>();
    response.setResult(Result.buildSuccess());
    return response;
  }

  public static MultiResponse<Void> buildFailure() {
    MultiResponse<Void> response = new MultiResponse<>();
    response.setResult(Result.buildFailure());
    return response;
  }

  public static MultiResponse<Void> buildFailure(String errCode) {
    MultiResponse<Void> response = new MultiResponse<>();
    response.setResult(Result.buildFailure(errCode));
    return response;
  }

  public static MultiResponse<Void> buildFailure(ResultCode errCode) {
    MultiResponse<Void> response = new MultiResponse<>();
    response.setResult(Result.buildFailure(errCode));
    return response;
  }

  public static MultiResponse<Void> buildFailure(ResultCode errCode, String errMessage) {
    MultiResponse<Void> response = new MultiResponse<>();
    response.setResult(Result.buildFailure(errCode, errMessage));
    return response;
  }

  public static Response buildFailure(String errCode, String errMessage) {
    Response response = new Response();
    response.setResult(Result.buildFailure(errCode, errMessage));
    return response;
  }

  public List<T> getData() {
    if (null == data) {
      return Collections.emptyList();
    }
    if (data instanceof List) {
      return (List<T>) data;
    }
    return new ArrayList<>(data);
  }

  public void setData(Collection<T> data) {
    this.data = data;
  }

  public boolean isEmpty() {
    return Objects.isNull(data) || data.isEmpty();
  }

  public boolean isNotEmpty() {
    return !isEmpty();
  }
}
