package io.github.loadup.commons.result;

/*-
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;

@Schema(description = "成功响应体")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"result", "data", "pageInfo"})
public record SuccessResponse<T>(
        @Schema(description = "结果元数据") @JsonProperty("result")
        Result result,

        @Schema(description = "业务数据") @JsonProperty("data") T data,

        @Schema(description = "分页信息") @JsonProperty("pageInfo")
        PageInfo pageInfo)
        implements IResponse<T> {

    @Override
    public Result getResult() {
        return result;
    }

    // --- 静态工厂方法 ---

    /**
     * 单体数据成功
     */
    public static <T> SuccessResponse<T> of(T data) {
        return new SuccessResponse<>(Result.buildSuccess(), data, null);
    }

    /**
     * 无数据成功 (Void)
     */
    public static SuccessResponse<Void> success() {
        return new SuccessResponse<>(Result.buildSuccess(), null, null);
    }

    /**
     * 分页数据成功
     */
    public static <T> SuccessResponse<Collection<T>> ofPage(Collection<T> data, Long total, Long size, Long index) {
        return new SuccessResponse<>(Result.buildSuccess(), data, new PageInfo(total, size, index));
    }

    public static <T> SuccessResponse<Collection<T>> ofPage(PageDTO<T> dto) {
        return new SuccessResponse<>(
                Result.buildSuccess(),
                dto.getData(),
                new PageInfo(
                        dto.getPageInfo().totalCount(),
                        dto.getPageInfo().pageSize(),
                        dto.getPageInfo().pageIndex()));
    }
}
