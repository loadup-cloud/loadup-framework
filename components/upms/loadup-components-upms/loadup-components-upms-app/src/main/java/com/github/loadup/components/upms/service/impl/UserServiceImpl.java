package com.github.loadup.components.upms.service.impl;

/*-
 * #%L
 * loadup-components-upms-app
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

import com.github.loadup.commons.error.AssertUtil;
import com.github.loadup.commons.request.query.IdQuery;
import com.github.loadup.commons.result.*;
import com.github.loadup.commons.template.ServiceTemplate;
import com.github.loadup.commons.util.PasswordUtils;
import com.github.loadup.commons.util.ValidateUtils;
import com.github.loadup.components.cache.api.CacheBinding;
import com.github.loadup.components.upms.client.api.UserService;
import com.github.loadup.components.upms.client.cmd.UserChangePasswordCmd;
import com.github.loadup.components.upms.client.cmd.UserRolesSaveCmd;
import com.github.loadup.components.upms.client.cmd.UserSaveCmd;
import com.github.loadup.components.upms.client.dto.SimpleUserDTO;
import com.github.loadup.components.upms.client.dto.UserDTO;
import com.github.loadup.components.upms.domain.User;
import com.github.loadup.components.upms.gateway.UserGateway;
import com.github.loadup.components.upms.service.impl.convertor.UserDTOConvertor;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserGateway userGateway;

    @Resource
    private CacheBinding cacheBinding;

    @Override
    public SingleResponse<UserDTO> getUserById(IdQuery query) {
        User user = userGateway.getById(query.getId());
        UserDTO userDTO = UserDTOConvertor.INSTANCE.toUserDTO(user);
        cacheBinding.set("test", userDTO, 1000);
        Object o = cacheBinding.get("test");
        return SingleResponse.of(userDTO);
    }

    @Override
    public MultiResponse<SimpleUserDTO> getUserByRoleId(String roleId) {
        return ServiceTemplate.execute((Void) -> ValidateUtils.validate(roleId), // check parameter
                () -> { // process
                    List<User> userList = userGateway.getByRoleId(roleId);
                    List<SimpleUserDTO> userDTOList = UserDTOConvertor.INSTANCE.toSimpleUserDTOList(userList);
                    return MultiResponse.of(userDTOList);
                },
                //build failure
                (e) -> Result.buildFailure(CommonResultCodeEnum.UNKNOWN),
                //compose log
                (Void) -> {
                });

    }

    @Override
    public SingleResponse<SimpleUserDTO> save(UserSaveCmd cmd) {
        return ServiceTemplate.execute((Void) -> ValidateUtils.validate(cmd), // check parameter
                () -> { // process
                    SimpleUserDTO dto = cmd.getUser();
                    User user = UserDTOConvertor.INSTANCE.toUser(dto);
                    user.setSalt(PasswordUtils.getRandomSalt());
                    String plantPassword = dto.getPassword();
                    user.setPassword(PasswordUtils.encrypt(plantPassword, plantPassword, user.getSalt()));
                    user = userGateway.create(user);
                    return SingleResponse.of(UserDTOConvertor.INSTANCE.toSimpleUserDTO(user));
                },
                //build failure
                (e) -> Result.buildFailure(CommonResultCodeEnum.UNKNOWN),
                //compose log
                (Void) -> {
                });
    }

    @Override
    public SingleResponse<UserDTO> saveUserRoles(UserRolesSaveCmd cmd) {
        return ServiceTemplate.execute((Void) -> ValidateUtils.validate(cmd), // check parameter

                () -> { // process
                    User user = userGateway.getById(cmd.getUserId());
                    if (Objects.isNull(user)) {
                        return SingleResponse.buildFailure(CommonResultCodeEnum.NOT_FOUND);
                    }
                    userGateway.saveUserRoles(cmd.getUserId(), cmd.getRoleIdList());
                    return getUserById(IdQuery.of(cmd.getUserId()));
                },
                //
                (e) -> Result.buildFailure(CommonResultCodeEnum.UNKNOWN),
                //
                (Void) -> {
                });
    }

    @Override
    public Response changePassword(UserChangePasswordCmd cmd) {
        return ServiceTemplate.execute(
                // check parameter
                (Void) -> {
                    ValidateUtils.validate(cmd);
                    AssertUtil.equals(cmd.getNewPassword(), cmd.getConfirmPassword());
                },
                // process
                () -> {
                    String userId = cmd.getId();
                    User user = userGateway.getById(userId);
                    if (Objects.isNull(user)) {
                        return Response.buildFailure(CommonResultCodeEnum.NOT_FOUND);
                    }
                    String dbPassword = user.getPassword();
                    if (!cmd.getOldPassword().equals(PasswordUtils.decrypt(dbPassword, cmd.getOldPassword(), user.getSalt()))) {
                        return Response.buildFailure(CommonResultCodeEnum.PROCESS_FAIL);
                    }
                    user.setPassword(PasswordUtils.encrypt(cmd.getNewPassword(), cmd.getNewPassword(), user.getSalt()));
                    userGateway.changePassword(user);
                    return Response.buildSuccess();
                },
                //
                (e) -> Result.buildFailure(CommonResultCodeEnum.UNKNOWN),
                //
                (Void) -> {
                });
    }
}
