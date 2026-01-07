package com.github.loadup.commons.result;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "success",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = SuccessResponse.class, name = "true"),
  @JsonSubTypes.Type(value = FailureResponse.class, name = "false")
})
@Schema(description = "统一响应接口")
public sealed interface IResponse<T> permits SuccessResponse, FailureResponse {

  @Schema(description = "执行状态")
  boolean isSuccess();

  @Schema(description = "结果元数据")
  Result getResult();
}
