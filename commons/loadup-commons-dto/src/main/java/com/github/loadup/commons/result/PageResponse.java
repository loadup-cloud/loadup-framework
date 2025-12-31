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

public class PageResponse<T> extends MultiResponse {
  @Serial private static final long serialVersionUID = -2340286928682564976L;

  private Long totalCount = 0L;

  private Long pageSize = 1L;

  private Long pageIndex = 1L;

  private Collection<T> data;

  public static <T> PageResponse<T> of(Long pageSize, Long pageIndex) {
    PageResponse<T> response = new PageResponse<>();

    response.setResult(Result.buildSuccess());
    response.setData(Collections.emptyList());
    response.setTotalCount(0L);
    response.setPageSize(pageSize);
    response.setPageIndex(pageIndex);
    return response;
  }

  public static <T> PageResponse<T> of(
      Collection<T> data, Long totalCount, Long pageSize, Long pageIndex) {
    PageResponse<T> response = new PageResponse<>();
    if (Objects.isNull(data)) {
      response.setResult(Result.buildFailure(CommonResultCodeEnum.NOT_FOUND));
      return response;
    }
    response.setResult(Result.buildSuccess());
    response.setData(data);
    response.setTotalCount(totalCount);
    response.setPageSize(pageSize);
    response.setPageIndex(pageIndex);
    return response;
  }

  public static PageResponse buildSuccess() {
    PageResponse response = new PageResponse();
    response.setResult(Result.buildSuccess());
    return response;
  }

  public static PageResponse buildFailure() {
    PageResponse response = new PageResponse();
    response.setResult(Result.buildFailure());
    return response;
  }

  public static PageResponse buildFailure(String errCode) {
    PageResponse response = new PageResponse();
    response.setResult(Result.buildFailure(errCode));
    return response;
  }

  public static PageResponse buildFailure(ResultCode errCode) {
    PageResponse response = new PageResponse();
    response.setResult(Result.buildFailure(errCode));
    return response;
  }

  public static PageResponse buildFailure(ResultCode errCode, String errMessage) {
    PageResponse response = new PageResponse();
    response.setResult(Result.buildFailure(errCode, errMessage));
    return response;
  }

  public static Response buildFailure(String errCode, String errMessage) {
    Response response = new Response();
    response.setResult(Result.buildFailure(errCode, errMessage));
    return response;
  }

  public Long getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(Long totalCount) {
    this.totalCount = totalCount;
  }

  public Long getPageSize() {
    if (pageSize < 1) {
      return 1L;
    }
    return pageSize;
  }

  public void setPageSize(Long pageSize) {
    if (pageSize < 1) {
      this.pageSize = 1L;
    } else {
      this.pageSize = pageSize;
    }
  }

  public Long getPageIndex() {
    if (pageIndex < 1) {
      return 1L;
    }
    return pageIndex;
  }

  public void setPageIndex(Long pageIndex) {
    if (pageIndex < 1) {
      this.pageIndex = 1L;
    } else {
      this.pageIndex = pageIndex;
    }
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

  public Long getTotalPages() {
    return this.totalCount % this.pageSize == 0
        ? this.totalCount / this.pageSize
        : (this.totalCount / this.pageSize) + 1;
  }
}
