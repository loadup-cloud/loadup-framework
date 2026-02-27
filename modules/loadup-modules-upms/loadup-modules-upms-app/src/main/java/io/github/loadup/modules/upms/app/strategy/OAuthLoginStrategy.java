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

import io.github.loadup.modules.upms.app.strategy.oauth.OAuthProvider;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.entity.UserOAuthBinding;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.gateway.UserOAuthBindingGateway;
import io.github.loadup.upms.api.constant.LoginType;
import io.github.loadup.upms.api.dto.AuthenticatedUser;
import io.github.loadup.upms.api.dto.LoginCredentials;
import io.github.loadup.upms.api.dto.OAuthToken;
import io.github.loadup.upms.api.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OAuth 登录策略
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthLoginStrategy implements LoginStrategy {

    private final Map<String, OAuthProvider> providerMap = new ConcurrentHashMap<>();
    private final UserOAuthBindingGateway bindingGateway;
    private final UserGateway userGateway;
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造器注入所有 OAuth Provider
     */
    public OAuthLoginStrategy(
            List<OAuthProvider> providers,
            UserOAuthBindingGateway bindingGateway,
            UserGateway userGateway,
            PasswordEncoder passwordEncoder
    ) {
        this.bindingGateway = bindingGateway;
        this.userGateway = userGateway;
        this.passwordEncoder = passwordEncoder;

        if (providers != null && !providers.isEmpty()) {
            providers.forEach(provider -> {
                providerMap.put(provider.getProviderName(), provider);
                log.info("Registered OAuth provider: {}", provider.getProviderName());
            });
        }
    }

    @Override
    public String getLoginType() {
        return LoginType.OAUTH;
    }

    @Override
    public AuthenticatedUser authenticate(LoginCredentials credentials) {
        // 1. 校验参数
        if (StringUtils.isBlank(credentials.getProvider())) {
            throw new RuntimeException("OAuth 提供商不能为空");
        }
        if (StringUtils.isBlank(credentials.getCode())) {
            throw new RuntimeException("授权码不能为空");
        }

        // 2. 获取 OAuth Provider
        OAuthProvider provider = providerMap.get(credentials.getProvider());
        if (provider == null) {
            throw new UnsupportedOperationException("不支持的 OAuth 提供商: " + credentials.getProvider());
        }

        // 3. 通过 code 换取 token
        OAuthToken token = provider.exchangeToken(credentials.getCode(), credentials.getRedirectUri());

        // 4. 获取第三方用户信息
        OAuthUserInfo oauthUser = provider.getUserInfo(token.getAccessToken());

        // 5. 查询是否已绑定本地账号
        Optional<UserOAuthBinding> binding = bindingGateway.findByProviderAndOpenId(
                credentials.getProvider(),
                oauthUser.getOpenId()
        );

        if (binding.isPresent()) {
            // 已绑定：直接登录
            User user = userGateway.findById(binding.get().getUserId())
                    .orElseThrow(() -> new RuntimeException("关联用户不存在"));

            // 检查账号状态
            if (!user.isActive()) {
                throw new RuntimeException("账号已被锁定或停用");
            }

            // 更新登录信息
            user.updateLastLogin(credentials.getIpAddress());
            userGateway.update(user);

            return AuthenticatedUser.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatar(oauthUser.getAvatar()) // 使用第三方头像
                    .email(user.getEmail())
                    .mobile(user.getMobile())
                    .newUser(false)
                    .build();
        } else {
            // 未绑定：自动注册新用户
            User newUser = autoRegisterUser(oauthUser, credentials.getProvider(), token);

            return AuthenticatedUser.builder()
                    .userId(newUser.getId())
                    .username(newUser.getUsername())
                    .nickname(oauthUser.getNickname())
                    .avatar(oauthUser.getAvatar())
                    .email(newUser.getEmail())
                    .mobile(newUser.getMobile())
                    .newUser(true)  // 标记为新用户，前端可引导完善信息
                    .build();
        }
    }

    /**
     * 自动注册用户（首次 OAuth 登录）
     */
    private User autoRegisterUser(OAuthUserInfo oauthUser, String provider, OAuthToken token) {
        // 生成用户名（如 github_123456）
        String username = provider + "_" + oauthUser.getOpenId().substring(0, Math.min(8, oauthUser.getOpenId().length()));

        // 确保用户名唯一
        int suffix = 1;
        String finalUsername = username;
        while (userGateway.existsByUsername(finalUsername)) {
            finalUsername = username + "_" + suffix++;
        }

        User user = User.builder()
                .username(finalUsername)
                .nickname(oauthUser.getNickname())
                .avatar(oauthUser.getAvatar())
                .email(oauthUser.getEmail())
                .mobile(oauthUser.getMobile())
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // 随机密码
                .deptId("1") // 默认部门
                .status((short) 1)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .emailVerified(StringUtils.isNotBlank(oauthUser.getEmail()))
                .mobileVerified(StringUtils.isNotBlank(oauthUser.getMobile()))
                .deleted(false)
                .createdBy("0")
                .createdTime(LocalDateTime.now())
                .build();

        user = userGateway.save(user);

        // 创建绑定关系
        UserOAuthBinding binding = UserOAuthBinding.builder()
                .userId(user.getId())
                .provider(provider)
                .openId(oauthUser.getOpenId())
                .unionId(oauthUser.getUnionId())
                .nickname(oauthUser.getNickname())
                .avatar(oauthUser.getAvatar())
                .accessToken(token.getAccessToken()) // TODO: 加密存储
                .refreshToken(token.getRefreshToken())
                .expiresAt(token.getExpiresIn() != null
                        ? LocalDateTime.now().plusSeconds(token.getExpiresIn())
                        : null)
                .boundAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        bindingGateway.save(binding);

        log.info("Auto registered new user via OAuth: username={}, provider={}", user.getUsername(), provider);

        return user;
    }
}

