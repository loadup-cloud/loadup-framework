package com.github.loadup.upms.api.service;

import com.github.loadup.upms.api.command.UserLoginCommand;
import com.github.loadup.upms.api.command.UserRegisterCommand;
import com.github.loadup.upms.api.dto.AccessTokenDTO;
import com.github.loadup.upms.api.dto.UserDetailDTO;

/**
 * 认证应用服务契约
 *
 * @author LoadUp Framework
 */
public interface AuthenticationService {

  /**
   * 用户登录
   *
   * @param command 登录参数
   * @return 访问令牌对象
   */
  AccessTokenDTO login(UserLoginCommand command);

  /** 退出登录 */
  void logout();

  UserDetailDTO register(UserRegisterCommand command);

  AccessTokenDTO refreshToken(String refreshToken);
}
