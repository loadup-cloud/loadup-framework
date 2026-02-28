package io.github.loadup.modules.upms.client.service;

/*-
 * #%L
 * Loadup Modules UPMS Client Layer
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

import io.github.loadup.modules.upms.client.dto.UserDetailDTO;
import java.util.List;

/** UPMS 外部调用接口 */
public interface UserQueryService {

    /** 获取用户基本信息 */
    UserDetailDTO getUserById(Long userId);

    /** 批量获取用户信息 */
    List<UserDetailDTO> listUsersByIds(List<Long> userIds);
}
