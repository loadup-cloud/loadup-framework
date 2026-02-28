package io.github.loadup.modules.upms.domain.gateway;

/*-
 * #%L
 * Loadup Modules UPMS Domain Layer
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
import java.util.List;
import java.util.Optional;

/**
 * 用户OAuth绑定仓储接口
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface UserOAuthBindingGateway {

    /**
     * 保存绑定
     */
    UserOAuthBinding save(UserOAuthBinding binding);

    /**
     * 根据提供商和OpenID查询绑定
     */
    Optional<UserOAuthBinding> findByProviderAndOpenId(String provider, String openId);

    /**
     * 根据用户ID查询所有绑定
     */
    List<UserOAuthBinding> findByUserId(String userId);

    /**
     * 根据用户ID和提供商查询绑定
     */
    Optional<UserOAuthBinding> findByUserIdAndProvider(String userId, String provider);

    /**
     * 删除绑定
     */
    void delete(String id);

    /**
     * 根据用户ID和提供商删除绑定
     */
    void deleteByUserIdAndProvider(String userId, String provider);
}
