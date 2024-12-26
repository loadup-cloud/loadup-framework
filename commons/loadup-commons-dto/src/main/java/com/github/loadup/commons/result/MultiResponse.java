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

public class MultiResponse<T> extends Response {
    @Serial
    private static final long serialVersionUID = 195635483708408296L;

    private Collection<T> data;

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

    public static MultiResponse buildSuccess() {
        MultiResponse response = new MultiResponse();
        response.setResult(Result.buildSuccess());
        return response;
    }

    public static MultiResponse buildFailure() {
        MultiResponse response = new MultiResponse();
        response.setResult(Result.buildFailure());
        return response;
    }

    public static MultiResponse buildFailure(String errCode) {
        MultiResponse response = new MultiResponse();
        response.setResult(Result.buildFailure(errCode));
        return response;
    }

    public static MultiResponse buildFailure(ResultCode errCode) {
        MultiResponse response = new MultiResponse();
        response.setResult(Result.buildFailure(errCode));
        return response;
    }

    public static MultiResponse buildFailure(ResultCode errCode, String errMessage) {
        MultiResponse response = new MultiResponse();
        response.setResult(Result.buildFailure(errCode, errMessage));
        return response;
    }

    public static Response buildFailure(String errCode, String errMessage) {
        Response response = new Response();
        response.setResult(Result.buildFailure(errCode, errMessage));
        return response;
    }
}
