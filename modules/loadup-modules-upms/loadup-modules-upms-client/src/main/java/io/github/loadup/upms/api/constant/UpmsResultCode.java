package io.github.loadup.upms.api.constant;

import io.github.loadup.commons.result.ResultCode;
import io.github.loadup.commons.result.ResultStatusEnum;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.Strings;

@Getter
@AllArgsConstructor
public enum UpmsResultCode implements ResultCode {
  USER_NOT_FOUND(ResultStatusEnum.FAIL, "用户不存在"),
  USER_LOCKED(ResultStatusEnum.FAIL, "用户已被锁定"),
  PASSWORD_ERROR(ResultStatusEnum.FAIL, "密码错误"),
  UNAUTHORIZED(ResultStatusEnum.FAIL, "无权访问该资源"),
  ROLE_NOT_FOUND(ResultStatusEnum.FAIL, "角色不存在");

  private final String status;

  private final String message;

  UpmsResultCode(ResultStatusEnum status, String message) {
    this.status = status.getCode();
    this.message = message;
  }

  public static UpmsResultCode getByResultCode(String resultCode) {
    return Arrays.stream(UpmsResultCode.values())
        .filter(value -> Strings.CI.equals(value.getCode(), resultCode))
        .findFirst()
        .orElse(null);
  }

  @Override
  public String getCode() {
    return name();
  }

  @Override
  public String getStatus() {
    return status;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
