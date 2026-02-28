package io.github.loadup.modules.upms.app.strategy;

/*-
 * #%L
 * Loadup Modules UPMS App Layer
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

import io.github.loadup.modules.upms.app.service.VerificationCodeService;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.client.constant.LoginType;
import io.github.loadup.modules.upms.client.dto.AuthenticatedUser;
import io.github.loadup.modules.upms.client.dto.LoginCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 手机验证码登录策略
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MobileLoginStrategy implements LoginStrategy {

    private final UserGateway userGateway;
    private final VerificationCodeService verificationCodeService;

    @Override
    public String getLoginType() {
        return LoginType.MOBILE;
    }

    @Override
    public AuthenticatedUser authenticate(LoginCredentials credentials) {
        // 1. 校验参数
        if (StringUtils.isBlank(credentials.getMobile())) {
            throw new RuntimeException("手机号不能为空");
        }
        if (StringUtils.isBlank(credentials.getSmsCode())) {
            throw new RuntimeException("验证码不能为空");
        }

        // 2. 验证短信验证码
        boolean valid = verificationCodeService.verifySmsCode(
                credentials.getMobile(),
                credentials.getSmsCode()
        );
        if (!valid) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 3. 查询用户
        User user = userGateway.findByMobile(credentials.getMobile())
                .orElseThrow(() -> new RuntimeException("手机号未注册"));

        // 4. 检查账号状态
        if (!user.isActive()) {
            throw new RuntimeException("账号已被锁定或停用");
        }

        // 5. 更新登录信息
        user.updateLastLogin(credentials.getIpAddress());
        userGateway.update(user);

        // 6. 构建认证结果
        return AuthenticatedUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .newUser(false)
                .build();
    }
}

