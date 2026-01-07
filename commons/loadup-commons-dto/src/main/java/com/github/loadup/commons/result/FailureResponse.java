package com.github.loadup.commons.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.Serial;

/** 失败响应实现 使用 Record 保证数据不可变，通过 Jackson 注解确保 JSON 格式平整 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"success", "traceId", "result"})
public record FailureResponse<T>(
    @JsonProperty("result") Result result, @JsonProperty("traceId") String traceId)
    implements IResponse<T> {

  @Serial private static final long serialVersionUID = 1L;

  /** 必须实现的接口方法：失败状态永远为 false */
  @JsonProperty("success")
  @Override
  public boolean isSuccess() {
    return false;
  }

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
