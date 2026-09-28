package io.github.loadup.modules.upms.client.service;

import io.github.loadup.modules.upms.client.command.UserLoginCommand;
import io.github.loadup.modules.upms.client.command.UserRegisterCommand;
import io.github.loadup.modules.upms.client.dto.AuthenticatedUser;
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
     * @return 已认证的用户身份
     */
    AuthenticatedUser login(UserLoginCommand command);

    UserDetailDTO register(UserRegisterCommand command);
}
