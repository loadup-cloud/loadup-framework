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
package io.github.loadup.modules.upms.app.strategy.oauth;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.modules.upms.app.autoconfigure.UpmsSecurityProperties;
import io.github.loadup.modules.upms.app.strategy.OAuthProviderCode;
import io.github.loadup.modules.upms.client.error.UpmsResultCodeEnum;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * GitHub OAuth Provider
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(prefix = "loadup.modules.upms.security.oauth.github", name = "enabled", havingValue = "true")
public class GitHubOAuthProvider implements OAuthProvider {

    private static final String AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String USER_INFO_URL = "https://api.github.com/user";
    private final UpmsSecurityProperties securityProperties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GitHubOAuthProvider(UpmsSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public String getProviderName() {
        return OAuthProviderCode.GITHUB;
    }

    @Override
    public String getAuthorizationUrl(String state, String redirectUri) {
        UpmsSecurityProperties.OAuthConfig config =
                securityProperties.getOauth().getGithub();

        return AUTHORIZE_URL + "?client_id=" + config.getClientId()
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&state=" + state
                + "&scope=user:email";
    }

    @Override
    public OAuthToken exchangeToken(String code, String redirectUri) {
        try {
            UpmsSecurityProperties.OAuthConfig config =
                    securityProperties.getOauth().getGithub();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", config.getClientId());
            params.add("client_secret", config.getClientSecret());
            params.add("code", code);
            params.add("redirect_uri", redirectUri);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Accept", "application/json");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(TOKEN_URL, request, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            return OAuthToken.builder()
                    .accessToken(jsonNode.get("access_token").asString())
                    .scope(jsonNode.has("scope") ? jsonNode.get("scope").asString() : null)
                    .build();

        } catch (Exception e) {
            LogUtil.error(GitHubOAuthProvider.class, "Failed to exchange GitHub token", e);
            throw new CommonException(UpmsResultCodeEnum.OPERATION_FAILED);
        }
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/json");

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, request, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            return OAuthUserInfo.builder()
                    .openId(jsonNode.get("id").asString())
                    .nickname(
                            jsonNode.has("name")
                                    ? jsonNode.get("name").asString()
                                    : jsonNode.get("login").asString())
                    .avatar(
                            jsonNode.has("avatar_url")
                                    ? jsonNode.get("avatar_url").asString()
                                    : null)
                    .email(jsonNode.has("email") ? jsonNode.get("email").asString() : null)
                    .build();

        } catch (Exception e) {
            LogUtil.error(GitHubOAuthProvider.class, "Failed to get GitHub user info", e);
            throw new CommonException(UpmsResultCodeEnum.OPERATION_FAILED);
        }
    }
}
