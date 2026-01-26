package io.github.loadup.commons.result;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "统一响应接口")
public sealed interface IResponse<T> permits SuccessResponse, FailureResponse {

  @Schema(description = "结果元数据")
  Result getResult();
}
