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

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户OAuth第三方账号绑定 Data Object
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("upms_user_oauth_binding")
public class UserOAuthBindingDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.None)
    private String id;

    /**
     * 本地用户ID
     */
    private String userId;

    /**
     * OAuth提供商：wechat | github | google
     */
    private String provider;

    /**
     * 第三方平台用户唯一ID
     */
    private String openId;

    /**
     * 联合ID（如微信UnionID）
     */
    private String unionId;

    /**
     * 第三方昵称
     */
    private String nickname;

    /**
     * 第三方头像
     */
    private String avatar;

    /**
     * 访问令牌（加密存储）
     */
    private String accessToken;

    /**
     * 刷新令牌（加密存储）
     */
    private String refreshToken;

    /**
     * 令牌过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 绑定时间
     */
    private LocalDateTime boundAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

