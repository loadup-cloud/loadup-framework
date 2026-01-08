package com.github.loadup.commons.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;

@Schema(description = "成功响应体")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"result", "data", "pageInfo"})
public record SuccessResponse<T>(
    @Schema(description = "结果元数据") @JsonProperty("result") Result result,
    @Schema(description = "业务数据") @JsonProperty("data") T data,
    @Schema(description = "分页信息") @JsonProperty("pageInfo") PageInfo pageInfo)
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
  public static <T> SuccessResponse<Collection<T>> ofPage(
      Collection<T> data, Long total, Long size, Long index) {
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
