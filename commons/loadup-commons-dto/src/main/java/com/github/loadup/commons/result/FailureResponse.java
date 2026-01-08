package com.github.loadup.commons.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

/** 失败响应实现 使用 Record 保证数据不可变，通过 Jackson 注解确保 JSON 格式平整 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"result", "traceId"})
public record FailureResponse<T>(
    @Schema(description = "结果元数据") @JsonProperty("result") Result result,
    @Schema(description = "请求追踪ID") @JsonProperty("traceId") String traceId)
    implements IResponse<T> {

  @Override
  public Result getResult() {
    return result;
  }

  // --- 静态工厂方法 ---

  public static <T> FailureResponse<T> of(ResultCode code) {
    return new FailureResponse<>(Result.buildFailure(code), null);
  }

  public static <T> FailureResponse<T> of(String errCode, String errMessage) {
    return new FailureResponse<>(Result.buildFailure(errCode, errMessage), null);
  }

  /** 带 TraceId 的工厂方法（推荐在 GlobalExceptionHandler 中使用） */
  public static <T> FailureResponse<T> of(String errCode, String errMessage, String traceId) {
    return new FailureResponse<>(Result.buildFailure(errCode, errMessage), traceId);
  }

  public static <T> FailureResponse<T> of(ResultCode code, String errMessage, String traceId) {
    return new FailureResponse<>(Result.buildFailure(code, errMessage), traceId);
  }
}
