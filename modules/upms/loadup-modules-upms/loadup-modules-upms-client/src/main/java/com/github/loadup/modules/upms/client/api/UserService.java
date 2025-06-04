/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.modules.upms.client.api;

/*-
 * #%L
 * loadup-modules-upms-client
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.commons.request.query.IdListQuery;
import com.github.loadup.commons.request.query.IdQuery;
import com.github.loadup.commons.result.MultiResponse;
import com.github.loadup.commons.result.Response;
import com.github.loadup.commons.result.SingleResponse;
import com.github.loadup.modules.upms.client.cmd.*;
import com.github.loadup.modules.upms.client.dto.SimpleUserDTO;
import com.github.loadup.modules.upms.client.dto.UserDTO;
import com.github.loadup.modules.upms.client.query.*;

public interface UserService {
    SingleResponse<UserDTO> findById(IdQuery query);

    MultiResponse<UserDTO> listByIds(IdListQuery query);

    SingleResponse<UserDTO> findByUserAccount(UserAccountQuery query);

    SingleResponse<UserDTO> findByUserMobile(UserMobileQuery query);

    MultiResponse<SimpleUserDTO> listByRoleIds(UserRoleListQuery query);

    MultiResponse<SimpleUserDTO> listByDeptIds(UserDeptListQuery query);

    MultiResponse<SimpleUserDTO> listByPostIds(UserPostListQuery query);

    SingleResponse<SimpleUserDTO> save(UserSaveCmd cmd);

    SingleResponse<UserDTO> saveUserRoles(UserRolesSaveCmd cmd);

    Response changePassword(UserChangePasswordCmd cmd);

    Response resetPassword(UserResetPasswordCmd cmd);

    Response lockUser(UserStatusUpdateCmd cmd);

    Response unLockUser(UserStatusUpdateCmd cmd);

    Response deleteUser(UserDeleteCmd cmd);
}
