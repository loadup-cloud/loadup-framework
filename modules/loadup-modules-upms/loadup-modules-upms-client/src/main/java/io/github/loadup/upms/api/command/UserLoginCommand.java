package io.github.loadup.upms.api.command;

import lombok.Data;

/**
 * User Login Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class UserLoginCommand {

  private String username;

  private String password;

  private String captchaKey;

  private String captchaCode;

  private String ipAddress;

  private String userAgent;
}
