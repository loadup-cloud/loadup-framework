package io.github.loadup.modules.upms.infrastructure.converter;

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
        UserOAuthBinding userOAuthBinding = new UserOAuthBinding();
        userOAuthBinding.setId(dataObject.getId());
        userOAuthBinding.setUserId(dataObject.getUserId());
        userOAuthBinding.setProvider(dataObject.getProvider());
        userOAuthBinding.setOpenId(dataObject.getOpenId());
        userOAuthBinding.setUnionId(dataObject.getUnionId());
        userOAuthBinding.setNickname(dataObject.getNickname());
        userOAuthBinding.setAvatar(dataObject.getAvatar());
        userOAuthBinding.setAccessToken(dataObject.getAccessToken());
        userOAuthBinding.setRefreshToken(dataObject.getRefreshToken());
        userOAuthBinding.setExpiresAt(dataObject.getExpiresAt());
        userOAuthBinding.setBoundAt(dataObject.getBoundAt());
        userOAuthBinding.setCreatedAt(dataObject.getCreatedAt());
        userOAuthBinding.setUpdatedAt(dataObject.getUpdatedAt());
        return userOAuthBinding;
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
