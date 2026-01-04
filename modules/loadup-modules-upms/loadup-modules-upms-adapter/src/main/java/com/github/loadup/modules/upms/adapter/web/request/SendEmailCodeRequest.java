package com.github.loadup.modules.upms.adapter.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Send Email Code Request
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class SendEmailCodeRequest {
  @NotBlank(message = "邮箱不能为空")
  @Email(message = "邮箱格式不正确")
  private String email;
}
