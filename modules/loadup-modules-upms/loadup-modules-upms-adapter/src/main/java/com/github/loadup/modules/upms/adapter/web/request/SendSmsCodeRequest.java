package com.github.loadup.modules.upms.adapter.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Send SMS Code Request
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class SendSmsCodeRequest {
  @NotBlank(message = "手机号不能为空")
  @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
  private String phone;
}
