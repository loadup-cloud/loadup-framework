package io.github.loadup.modules.upms.infrastructure.converter;

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

import io.github.loadup.modules.upms.domain.entity.UserOAuthBinding;
import io.github.loadup.modules.upms.infrastructure.dataobject.UserOAuthBindingDO;
import org.springframework.stereotype.Component;

/**
 * 用户OAuth绑定转换器
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Component
public class UserOAuthBindingConverter {

    /**
     * DO 转 Entity
     */
    public UserOAuthBinding toEntity(UserOAuthBindingDO dataObject) {
        if (dataObject == null) {
            return null;
        }

        return UserOAuthBinding.builder()
                .id(dataObject.getId())
                .userId(dataObject.getUserId())
                .provider(dataObject.getProvider())
                .openId(dataObject.getOpenId())
                .unionId(dataObject.getUnionId())
                .nickname(dataObject.getNickname())
                .avatar(dataObject.getAvatar())
                .accessToken(dataObject.getAccessToken())
                .refreshToken(dataObject.getRefreshToken())
                .expiresAt(dataObject.getExpiresAt())
                .boundAt(dataObject.getBoundAt())
                .createdAt(dataObject.getCreatedAt())
                .updatedAt(dataObject.getUpdatedAt())
                .build();
    }

    /**
     * Entity 转 DO
     */
    public UserOAuthBindingDO toDO(UserOAuthBinding entity) {
        if (entity == null) {
            return null;
        }

        UserOAuthBindingDO dataObject = new UserOAuthBindingDO();
        dataObject.setId(entity.getId());
        dataObject.setUserId(entity.getUserId());
        dataObject.setProvider(entity.getProvider());
        dataObject.setOpenId(entity.getOpenId());
        dataObject.setUnionId(entity.getUnionId());
        dataObject.setNickname(entity.getNickname());
        dataObject.setAvatar(entity.getAvatar());
        dataObject.setAccessToken(entity.getAccessToken());
        dataObject.setRefreshToken(entity.getRefreshToken());
        dataObject.setExpiresAt(entity.getExpiresAt());
        dataObject.setBoundAt(entity.getBoundAt());
        dataObject.setCreatedAt(entity.getCreatedAt());
        dataObject.setUpdatedAt(entity.getUpdatedAt());
        return dataObject;
    }
}

