package io.github.loadup.modules.upms.infrastructure.dataobject;

/*-
 * #%L
 * Loadup Modules UPMS Infrastructure Layer
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
