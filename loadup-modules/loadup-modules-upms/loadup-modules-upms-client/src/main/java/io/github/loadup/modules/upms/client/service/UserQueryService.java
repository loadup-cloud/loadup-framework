package io.github.loadup.modules.upms.client.service;

/*-
 * #%L
 * Loadup Modules UPMS Client Layer
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

import io.github.loadup.modules.upms.client.dto.UserDetailDTO;
import java.util.List;

/**
 * UPMS 外部调用接口
 */
public interface UserQueryService {

    /**
     * 获取用户基本信息
     */
    UserDetailDTO getUserById(Long userId);

    /**
     * 批量获取用户信息
     */
    List<UserDetailDTO> listUsersByIds(List<Long> userIds);
}
