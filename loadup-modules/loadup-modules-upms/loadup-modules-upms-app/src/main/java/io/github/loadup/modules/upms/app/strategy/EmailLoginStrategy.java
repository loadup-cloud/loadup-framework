package io.github.loadup.modules.upms.app.strategy;

/*-
 * #%L
 * Loadup Modules UPMS App Layer
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

import io.github.loadup.modules.upms.app.service.VerificationCodeService;
import io.github.loadup.modules.upms.client.constant.LoginType;
import io.github.loadup.modules.upms.client.dto.AuthenticatedUser;
import io.github.loadup.modules.upms.client.dto.LoginCredentials;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 邮箱验证码登录策略
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Component
public class EmailLoginStrategy implements LoginStrategy {
    private static final Logger log = LoggerFactory.getLogger(EmailLoginStrategy.class);

    private final UserGateway userGateway;
    private final VerificationCodeService verificationCodeService;

    @Override
    public String getLoginType() {
        return LoginType.EMAIL;
    }

    @Override
    public AuthenticatedUser authenticate(LoginCredentials credentials) {
        // 1. 校验参数
        if (StringUtils.isBlank(credentials.getEmail())) {
            throw new RuntimeException("邮箱不能为空");
        }
        if (StringUtils.isBlank(credentials.getEmailCode())) {
            throw new RuntimeException("验证码不能为空");
        }

        // 2. 验证邮箱验证码
        boolean valid = verificationCodeService.verifyEmailCode(credentials.getEmail(), credentials.getEmailCode());
        if (!valid) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 3. 查询用户
        User user = userGateway.findByEmail(credentials.getEmail()).orElseThrow(() -> new RuntimeException("邮箱未注册"));

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

    public EmailLoginStrategy(UserGateway userGateway, VerificationCodeService verificationCodeService) {
        this.userGateway = userGateway;
        this.verificationCodeService = verificationCodeService;
    }
}
