package io.github.loadup.commons.result;

/*-
 * #%L
 * Loadup Common DTO
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

    /** 单体数据成功 */
    public static <T> SuccessResponse<T> of(T data) {
        return new SuccessResponse<>(Result.buildSuccess(), data, null);
    }

    /** 无数据成功 (Void) */
    public static SuccessResponse<Void> success() {
        return new SuccessResponse<>(Result.buildSuccess(), null, null);
    }

    /** 分页数据成功 */
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
