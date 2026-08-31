package io.github.loadup.modules.upms.domain.gateway;

/*-
 * #%L
 * Loadup Modules UPMS Domain Layer
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
