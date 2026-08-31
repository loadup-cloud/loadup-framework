package io.github.loadup.modules.upms.infrastructure.dataobject;

/*-
 * #%L
 * Loadup Modules UPMS Infrastructure Layer
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

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import java.time.LocalDateTime;

/**
 * 用户OAuth第三方账号绑定 Data Object
 */
@Table("upms_user_oauth_binding")
public class UserOAuthBindingDO extends BaseDO {

    private String userId;
    private String provider;
    private String openId;
    private String unionId;
    private String nickname;
    private String avatar;
    /**
     * 访问令牌（加密存储）
     */
    private String accessToken;
    /**
     * 刷新令牌（加密存储）
     */
    private String refreshToken;

    private LocalDateTime expiresAt;
    private LocalDateTime boundAt;

    public UserOAuthBindingDO(
            String userId,
            String provider,
            String openId,
            String unionId,
            String nickname,
            String avatar,
            String accessToken,
            String refreshToken,
            LocalDateTime expiresAt,
            LocalDateTime boundAt) {
        this.userId = userId;
        this.provider = provider;
        this.openId = openId;
        this.unionId = unionId;
        this.nickname = nickname;
        this.avatar = avatar;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.boundAt = boundAt;
    }

    public UserOAuthBindingDO() {}

    public String getUserId() {
        return this.userId;
    }

    public String getProvider() {
        return this.provider;
    }

    public String getOpenId() {
        return this.openId;
    }

    public String getUnionId() {
        return this.unionId;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public LocalDateTime getExpiresAt() {
        return this.expiresAt;
    }

    public LocalDateTime getBoundAt() {
        return this.boundAt;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setBoundAt(LocalDateTime boundAt) {
        this.boundAt = boundAt;
    }
}
