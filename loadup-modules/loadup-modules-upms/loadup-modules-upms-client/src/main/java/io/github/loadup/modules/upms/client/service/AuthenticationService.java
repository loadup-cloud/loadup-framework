package io.github.loadup.modules.upms.client.service;

/*-
 * #%L
 * Loadup Modules UPMS Client Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.github.loadup.modules.upms.client.command.UserLoginCommand;
import io.github.loadup.modules.upms.client.command.UserRegisterCommand;
import io.github.loadup.modules.upms.client.dto.AccessTokenDTO;
import io.github.loadup.modules.upms.client.dto.UserDetailDTO;

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

    /**
     * 退出登录
     */
    void logout();

    UserDetailDTO register(UserRegisterCommand command);

    AccessTokenDTO refreshToken(String refreshToken);
}
