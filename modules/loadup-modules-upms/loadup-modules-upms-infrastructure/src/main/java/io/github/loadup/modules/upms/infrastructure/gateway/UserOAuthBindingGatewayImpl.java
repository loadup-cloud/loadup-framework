package io.github.loadup.modules.upms.infrastructure.gateway;

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

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.commons.util.IdUtils;
import io.github.loadup.modules.upms.domain.entity.UserOAuthBinding;
import io.github.loadup.modules.upms.domain.gateway.UserOAuthBindingGateway;
import io.github.loadup.modules.upms.infrastructure.converter.UserOAuthBindingConverter;
import io.github.loadup.modules.upms.infrastructure.dataobject.UserOAuthBindingDO;
import io.github.loadup.modules.upms.infrastructure.mapper.UserOAuthBindingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * 用户OAuth绑定 Gateway 实现
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserOAuthBindingGatewayImpl implements UserOAuthBindingGateway {

    private final UserOAuthBindingMapper mapper;
    private final UserOAuthBindingConverter converter;

    @Override
    public UserOAuthBinding save(UserOAuthBinding binding) {
        UserOAuthBindingDO dataObject = converter.toDO(binding);

        // 设置审计字段
        if (dataObject.getId() == null) {
            // 新增
            dataObject.setId(IdUtils.uuid());
            dataObject.setCreatedAt(LocalDateTime.now());
        }
        dataObject.setUpdatedAt(LocalDateTime.now());

        mapper.insertOrUpdate(dataObject);

        return converter.toEntity(dataObject);
    }

    @Override
    public Optional<UserOAuthBinding> findByProviderAndOpenId(String provider, String openId) {
        UserOAuthBindingDO dataObject = mapper.selectByProviderAndOpenId(provider, openId);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public List<UserOAuthBinding> findByUserId(String userId) {
        List<UserOAuthBindingDO> dataObjects = mapper.selectByUserId(userId);
        return dataObjects.stream()
                .map(converter::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserOAuthBinding> findByUserIdAndProvider(String userId, String provider) {
        UserOAuthBindingDO dataObject = mapper.selectByUserIdAndProvider(userId, provider);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public void delete(String id) {
        mapper.deleteById(id);
    }

    @Override
    public void deleteByUserIdAndProvider(String userId, String provider) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("user_id = ?", userId)
                .and("provider = ?", provider);

        mapper.deleteByQuery(queryWrapper);
    }
}

