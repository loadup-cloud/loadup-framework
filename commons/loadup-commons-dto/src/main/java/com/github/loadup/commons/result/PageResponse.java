package com.github.loadup.commons.result;

/*-
 * #%L
 * loadup-commons-dto
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.io.Serial;
import java.util.*;

public class PageResponse<T> extends MultiResponse {
    @Serial
    private static final long serialVersionUID = -2340286928682564976L;

    private Long totalCount = 0L;

    private Long pageSize = 1L;

    private Long pageIndex = 1L;

    private Collection<T> data;

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
        return this.totalCount % this.pageSize == 0 ? this.totalCount / this.pageSize : (this.totalCount / this.pageSize) + 1;
    }

    public static <T> PageResponse<T> of(Long pageSize, Long pageIndex) {
        PageResponse<T> response = new PageResponse<>();

        response.setResult(Result.buildSuccess());
        response.setData(Collections.emptyList());
        response.setTotalCount(0L);
        response.setPageSize(pageSize);
        response.setPageIndex(pageIndex);
        return response;
    }

    public static <T> PageResponse<T> of(Collection<T> data, Long totalCount, Long pageSize, Long pageIndex) {
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
}
