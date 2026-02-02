package io.github.loadup.upms.api.service;

/*-
 * #%L
 * Loadup Modules UPMS Client Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
