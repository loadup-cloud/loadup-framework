package io.github.loadup.upms.api.service;

import io.github.loadup.upms.api.command.UserLoginCommand;
import io.github.loadup.upms.api.command.UserRegisterCommand;
import io.github.loadup.upms.api.dto.AccessTokenDTO;
import io.github.loadup.upms.api.dto.UserDetailDTO;

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
