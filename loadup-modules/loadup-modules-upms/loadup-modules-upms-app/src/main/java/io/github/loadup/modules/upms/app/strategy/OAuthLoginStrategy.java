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

import io.github.loadup.modules.upms.app.strategy.oauth.OAuthProvider;
import io.github.loadup.modules.upms.client.constant.LoginType;
import io.github.loadup.modules.upms.client.dto.AuthenticatedUser;
import io.github.loadup.modules.upms.client.dto.LoginCredentials;
import io.github.loadup.modules.upms.client.dto.OAuthToken;
import io.github.loadup.modules.upms.client.dto.OAuthUserInfo;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.entity.UserOAuthBinding;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.gateway.UserOAuthBindingGateway;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * OAuth 登录策略
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Component
public class OAuthLoginStrategy implements LoginStrategy {
    private static final Logger log = LoggerFactory.getLogger(OAuthLoginStrategy.class);

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
            PasswordEncoder passwordEncoder) {
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
        Optional<UserOAuthBinding> binding =
                bindingGateway.findByProviderAndOpenId(credentials.getProvider(), oauthUser.getOpenId());

        if (binding.isPresent()) {
            // 已绑定：直接登录
            User user =
                    userGateway.findById(binding.get().getUserId()).orElseThrow(() -> new RuntimeException("关联用户不存在"));

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
                    .newUser(true) // 标记为新用户，前端可引导完善信息
                    .build();
        }
    }

    /**
     * 自动注册用户（首次 OAuth 登录）
     */
    private User autoRegisterUser(OAuthUserInfo oauthUser, String provider, OAuthToken token) {
        // 生成用户名（如 github_123456）
        String username = provider + "_"
                + oauthUser
                        .getOpenId()
                        .substring(0, Math.min(8, oauthUser.getOpenId().length()));

        // 确保用户名唯一
        int suffix = 1;
        String finalUsername = username;
        while (userGateway.existsByUsername(finalUsername)) {
            finalUsername = username + "_" + suffix++;
        }

        User user = new User();
        user.setUsername(finalUsername);
        user.setNickname(oauthUser.getNickname());
        user.setAvatar(oauthUser.getAvatar());
        user.setEmail(oauthUser.getEmail());
        user.setMobile(oauthUser.getMobile());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // 随机密码;
        user.setDeptId("1"); // 默认部门;
        user.setStatus((short) 1);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEmailVerified(StringUtils.isNotBlank(oauthUser.getEmail()));
        user.setMobileVerified(StringUtils.isNotBlank(oauthUser.getMobile()));
        user.setDeleted(false);
        user.setCreatedBy("0");
        user.setCreatedTime(LocalDateTime.now());

        user = userGateway.save(user);

        // 创建绑定关系
        UserOAuthBinding binding = new UserOAuthBinding();
        binding.setUserId(user.getId());
        binding.setProvider(provider);
        binding.setOpenId(oauthUser.getOpenId());
        binding.setUnionId(oauthUser.getUnionId());
        binding.setNickname(oauthUser.getNickname());
        binding.setAvatar(oauthUser.getAvatar());
        binding.setAccessToken(token.getAccessToken()); // TODO: 加密存储;
        binding.setRefreshToken(token.getRefreshToken());
        binding.setExpiresAt(
                token.getExpiresIn() != null ? LocalDateTime.now().plusSeconds(token.getExpiresIn()) : null);
        binding.setBoundAt(LocalDateTime.now());
        binding.setCreatedAt(LocalDateTime.now());

        bindingGateway.save(binding);

        log.info("Auto registered new user via OAuth: username={}, provider={}", user.getUsername(), provider);

        return user;
    }
}
