package com.github.loadup.commons.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;

@Schema(description = "成功响应体")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"success", "data", "pageInfo", "result"})
public record SuccessResponse<T>(
    @Schema(description = "业务数据") @JsonProperty("data") T data,
    @Schema(description = "结果元数据") @JsonProperty("result") Result result,
    @Schema(description = "分页信息") @JsonProperty("pageInfo") PageInfo pageInfo)
    implements IResponse<T> {

  @Override
  @JsonProperty("success")
  public boolean isSuccess() {
    return true;
  }

  @Override
  public Result getResult() {
    return result;
  }

  // --- 静态工厂方法 ---

  /** 单体数据成功 */
  public static <T> SuccessResponse<T> of(T data) {
    return new SuccessResponse<>(data, Result.buildSuccess(), null);
  }

  /** 无数据成功 (Void) */
  public static SuccessResponse<Void> success() {
    return new SuccessResponse<>(null, Result.buildSuccess(), null);
  }

  /** 分页数据成功 */
  public static <T> SuccessResponse<Collection<T>> ofPage(
      Collection<T> data, Long total, Long size, Long index) {
    return new SuccessResponse<>(data, Result.buildSuccess(), new PageInfo(total, size, index));
  }
}
