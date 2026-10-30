/*
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
package io.github.loadup.modules.upms.app.strategy;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.modules.upms.app.strategy.oauth.OAuthProvider;
import io.github.loadup.modules.upms.app.strategy.oauth.OAuthToken;
import io.github.loadup.modules.upms.app.strategy.oauth.OAuthUserInfo;
import io.github.loadup.modules.upms.client.dto.AuthenticatedUserDTO;
import io.github.loadup.modules.upms.client.error.UpmsResultCodeEnum;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.gateway.UserOAuthBindingGateway;
import io.github.loadup.modules.upms.domain.model.User;
import io.github.loadup.modules.upms.domain.model.UserOAuthBinding;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Component
public class OAuthLoginStrategy implements LoginStrategy {
    private final Clock clock;

    private final Map<String, OAuthProvider> providerMap = new ConcurrentHashMap<>();
    private final UserOAuthBindingGateway bindingGateway;
    private final UserGateway userGateway;
    private final PasswordEncoder passwordEncoder;

    public OAuthLoginStrategy(
            List<OAuthProvider> providers,
            UserOAuthBindingGateway bindingGateway,
            UserGateway userGateway,
            PasswordEncoder passwordEncoder,
            Clock clock) {
        this.clock = clock;
        this.bindingGateway = bindingGateway;
        this.userGateway = userGateway;
        this.passwordEncoder = passwordEncoder;

        if (providers != null && !providers.isEmpty()) {
            providers.forEach(provider -> {
                providerMap.put(provider.getProviderName(), provider);
                LogUtil.info(OAuthLoginStrategy.class, "Registered OAuth provider: {}", provider.getProviderName());
            });
        }
    }

    @Override
    public String getLoginType() {
        return LoginType.OAUTH;
    }

    @Override
    public AuthenticatedUserDTO authenticate(LoginCredentials credentials) {

        if (StringUtils.isBlank(credentials.getProvider())) {
            throw new CommonException(UpmsResultCodeEnum.OPERATION_FAILED);
        }
        if (StringUtils.isBlank(credentials.getCode())) {
            throw new CommonException(UpmsResultCodeEnum.OPERATION_FAILED);
        }

        OAuthProvider provider = providerMap.get(credentials.getProvider());
        if (provider == null) {
            throw new CommonException(UpmsResultCodeEnum.OPERATION_NOT_ALLOWED);
        }

        OAuthToken token = provider.exchangeToken(credentials.getCode(), credentials.getRedirectUri());

        OAuthUserInfo oauthUser = provider.getUserInfo(token.getAccessToken());

        Optional<UserOAuthBinding> binding =
                bindingGateway.findByProviderAndOpenId(credentials.getProvider(), oauthUser.getOpenId());

        if (binding.isPresent()) {

            User user = userGateway
                    .findById(binding.get().getUserId())
                    .orElseThrow(() -> new CommonException(UpmsResultCodeEnum.OPERATION_FAILED));

            if (!user.isActive()) {
                throw new CommonException(UpmsResultCodeEnum.ACCOUNT_IS_LOCKED_OR_DISABLED);
            }

            user.updateLastLogin(credentials.getIpAddress(), LocalDateTime.ofInstant(clock.instant(), clock.getZone()));
            userGateway.update(user);

            return AuthenticatedUserDTO.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatar(oauthUser.getAvatar())
                    .email(user.getEmail())
                    .mobile(user.getMobile())
                    .newUser(false)
                    .build();
        } else {

            User newUser = autoRegisterUser(oauthUser, credentials.getProvider(), token);

            return AuthenticatedUserDTO.builder()
                    .userId(newUser.getId())
                    .username(newUser.getUsername())
                    .nickname(oauthUser.getNickname())
                    .avatar(oauthUser.getAvatar())
                    .email(newUser.getEmail())
                    .mobile(newUser.getMobile())
                    .newUser(true)
                    .build();
        }
    }

    private User autoRegisterUser(OAuthUserInfo oauthUser, String provider, OAuthToken token) {

        String username = provider + "_"
                + oauthUser
                        .getOpenId()
                        .substring(0, Math.min(8, oauthUser.getOpenId().length()));

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
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setDeptId("1");
        user.setStatus((short) 1);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEmailVerified(StringUtils.isNotBlank(oauthUser.getEmail()));
        user.setMobileVerified(StringUtils.isNotBlank(oauthUser.getMobile()));
        user.setDeleted(false);
        user.setCreatedBy("0");

        user = userGateway.save(user);

        UserOAuthBinding binding = new UserOAuthBinding();
        binding.setUserId(user.getId());
        binding.setProvider(provider);
        binding.setOpenId(oauthUser.getOpenId());
        binding.setUnionId(oauthUser.getUnionId());
        binding.setNickname(oauthUser.getNickname());
        binding.setAvatar(oauthUser.getAvatar());
        binding.setAccessToken(token.getAccessToken());
        binding.setRefreshToken(token.getRefreshToken());
        binding.setExpiresAt(
                token.getExpiresIn() != null
                        ? LocalDateTime.ofInstant(clock.instant(), clock.getZone())
                                .plusSeconds(token.getExpiresIn())
                        : null);
        binding.setBoundAt(LocalDateTime.ofInstant(clock.instant(), clock.getZone()));

        bindingGateway.save(binding);

        LogUtil.info(
                OAuthLoginStrategy.class,
                "Auto registered new user via OAuth: username={}, provider={}",
                user.getUsername(),
                provider);

        return user;
    }
}
