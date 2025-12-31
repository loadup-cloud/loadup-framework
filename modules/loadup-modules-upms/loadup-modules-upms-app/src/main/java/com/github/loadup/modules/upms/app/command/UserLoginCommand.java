package com.github.loadup.modules.upms.app.command;

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
