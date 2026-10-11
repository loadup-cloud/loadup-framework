/*
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
package io.github.loadup.modules.upms.infrastructure.repository;

import static io.github.loadup.modules.upms.infrastructure.dataobject.table.Tables.USER_OAUTH_BINDING_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.commons.util.IdUtils;
import io.github.loadup.modules.upms.domain.gateway.UserOAuthBindingGateway;
import io.github.loadup.modules.upms.domain.model.UserOAuthBinding;
import io.github.loadup.modules.upms.infrastructure.converter.UserOAuthBindingConverter;
import io.github.loadup.modules.upms.infrastructure.dataobject.UserOAuthBindingDO;
import io.github.loadup.modules.upms.infrastructure.mapper.UserOAuthBindingDOMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Component
public class UserOAuthBindingGatewayImpl implements UserOAuthBindingGateway {

    private final UserOAuthBindingDOMapper mapper;
    private final UserOAuthBindingConverter converter;

    @Override
    public UserOAuthBinding save(UserOAuthBinding binding) {
        UserOAuthBindingDO dataObject = converter.toDO(binding);
        if (dataObject.getId() == null) {
            dataObject.setId(IdUtils.uuid());
        }

        mapper.insertOrUpdate(dataObject);
        return converter.toEntity(dataObject);
    }

    @Override
    public Optional<UserOAuthBinding> findByProviderAndOpenId(String provider, String openId) {
        UserOAuthBindingDO dataObject = mapper.selectOneByQuery(QueryWrapper.create()
                .where(USER_OAUTH_BINDING_DO.PROVIDER.eq(provider))
                .and(USER_OAUTH_BINDING_DO.OPEN_ID.eq(openId)));
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public List<UserOAuthBinding> findByUserId(String userId) {
        List<UserOAuthBindingDO> entities =
                mapper.selectListByQuery(QueryWrapper.create().where(USER_OAUTH_BINDING_DO.USER_ID.eq(userId)));
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(converter::toEntity).collect(Collectors.toList());
    }

    @Override
    public Optional<UserOAuthBinding> findByUserIdAndProvider(String userId, String provider) {
        UserOAuthBindingDO dataObject = mapper.selectOneByQuery(QueryWrapper.create()
                .where(USER_OAUTH_BINDING_DO.USER_ID.eq(userId))
                .and(USER_OAUTH_BINDING_DO.PROVIDER.eq(provider)));
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public void delete(String id) {
        mapper.deleteById(id);
    }

    @Override
    public void deleteByUserIdAndProvider(String userId, String provider) {
        mapper.deleteByQuery(QueryWrapper.create()
                .where(USER_OAUTH_BINDING_DO.USER_ID.eq(userId))
                .and(USER_OAUTH_BINDING_DO.PROVIDER.eq(provider)));
    }

    public UserOAuthBindingGatewayImpl(UserOAuthBindingDOMapper mapper, UserOAuthBindingConverter converter) {
        this.mapper = mapper;
        this.converter = converter;
    }
}
