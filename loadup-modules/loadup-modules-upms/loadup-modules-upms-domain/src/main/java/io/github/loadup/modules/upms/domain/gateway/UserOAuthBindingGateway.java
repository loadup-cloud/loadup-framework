/*
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
package io.github.loadup.modules.upms.domain.gateway;

import io.github.loadup.modules.upms.domain.model.UserOAuthBinding;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface UserOAuthBindingGateway {

    UserOAuthBinding save(UserOAuthBinding binding);

    Optional<UserOAuthBinding> findByProviderAndOpenId(String provider, String openId);

    List<UserOAuthBinding> findByUserId(String userId);

    Optional<UserOAuthBinding> findByUserIdAndProvider(String userId, String provider);

    void delete(String id);

    void deleteByUserIdAndProvider(String userId, String provider);
}
