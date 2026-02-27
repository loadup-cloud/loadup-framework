package io.github.loadup.modules.upms.app.strategy.oauth;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.loadup.modules.upms.app.config.UpmsSecurityProperties;
import io.github.loadup.upms.api.constant.OAuthProvider;
import io.github.loadup.upms.api.dto.OAuthToken;
import io.github.loadup.upms.api.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * GitHub OAuth Provider
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "loadup.upms.security.oauth.github", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class GitHubOAuthProvider implements io.github.loadup.modules.upms.app.strategy.oauth.OAuthProvider {

    private final UpmsSecurityProperties securityProperties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String USER_INFO_URL = "https://api.github.com/user";

    @Override
    public String getProviderName() {
        return OAuthProvider.GITHUB;
    }

    @Override
    public String getAuthorizationUrl(String state, String redirectUri) {
        UpmsSecurityProperties.OAuthConfig config = securityProperties.getOauth().getGithub();

        return AUTHORIZE_URL + "?client_id=" + config.getClientId()
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&state=" + state
                + "&scope=user:email";
    }

    @Override
    public OAuthToken exchangeToken(String code, String redirectUri) {
        try {
            UpmsSecurityProperties.OAuthConfig config = securityProperties.getOauth().getGithub();

            // 构建请求参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", config.getClientId());
            params.add("client_secret", config.getClientSecret());
            params.add("code", code);
            params.add("redirect_uri", redirectUri);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Accept", "application/json");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(TOKEN_URL, request, String.class);

            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            return OAuthToken.builder()
                    .accessToken(jsonNode.get("access_token").asText())
                    .scope(jsonNode.has("scope") ? jsonNode.get("scope").asText() : null)
                    .build();

        } catch (Exception e) {
            log.error("Failed to exchange GitHub token", e);
            throw new RuntimeException("GitHub 授权失败: " + e.getMessage());
        }
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/json");

            HttpEntity<String> request = new HttpEntity<>(headers);

            // 获取用户信息
            ResponseEntity<String> response = restTemplate.exchange(
                    USER_INFO_URL,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            return OAuthUserInfo.builder()
                    .openId(jsonNode.get("id").asText())
                    .nickname(jsonNode.has("name") ? jsonNode.get("name").asText() : jsonNode.get("login").asText())
                    .avatar(jsonNode.has("avatar_url") ? jsonNode.get("avatar_url").asText() : null)
                    .email(jsonNode.has("email") ? jsonNode.get("email").asText() : null)
                    .build();

        } catch (Exception e) {
            log.error("Failed to get GitHub user info", e);
            throw new RuntimeException("获取 GitHub 用户信息失败: " + e.getMessage());
        }
    }
}

