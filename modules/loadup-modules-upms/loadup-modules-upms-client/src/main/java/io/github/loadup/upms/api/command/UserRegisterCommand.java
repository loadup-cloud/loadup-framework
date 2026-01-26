package io.github.loadup.upms.api.command;

import lombok.Data;

/**
 * User Register Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class UserRegisterCommand {

  private String username;

  private String password;

  private String nickname;

  private String email;

  private String mobile;

  private String captchaKey;

  private String captchaCode;

  private String smsCode;
}
