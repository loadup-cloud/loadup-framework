/*
 * #%L
 * Loadup Common DTO
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.commons.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.json.ToStringAsJson;
import io.github.loadup.commons.result.ResultCode;
import io.swagger.v3.oas.annotations.media.Schema;

/** Shared pagination bounds; HTTP field names remain page and size. */
public record PageQuery(
        @jakarta.validation.constraints.Min(1)
        @JsonProperty("page")
        @Schema(description = "Page number, starting at 1", minimum = "1", defaultValue = "1")
        Integer pageNum,

        @jakarta.validation.constraints.Min(1)
        @jakarta.validation.constraints.Max(100)
        @JsonProperty("size")
        @Schema(description = "Page size", minimum = "1", maximum = "100", defaultValue = "20")
        Integer pageSize) {
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    public PageQuery {
        pageNum = pageNum == null ? Integer.valueOf(1) : pageNum;
        pageSize = pageSize == null ? Integer.valueOf(DEFAULT_PAGE_SIZE) : pageSize;
        validate(pageNum, pageSize, CommonResultCodeEnum.PARAM_ILLEGAL);
    }

    public static PageQuery of(Integer page, Integer size) {
        return new PageQuery(page, size);
    }
    /** Preserves the calling module's published error code while sharing all bounds. */
    public static PageQuery normalized(Integer page, Integer size, ResultCode code) {
        int index = page == null ? 1 : page;
        int count = size == null ? DEFAULT_PAGE_SIZE : size;
        validate(index, count, code);
        return new PageQuery(index, count);
    }

    private static void validate(int page, int size, ResultCode code) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) throw new CommonException(code);
    }

    public int page() {
        return pageNum;
    }

    public int size() {
        return pageSize;
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
