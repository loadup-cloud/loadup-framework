package com.github.loadup.modules.upms.service.impl;

/*-
 * #%L
 * loadup-modules-upms-app
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
import com.github.loadup.commons.request.query.IdListQuery;
import com.github.loadup.commons.request.query.IdQuery;
import com.github.loadup.commons.result.*;
import com.github.loadup.commons.template.ServiceTemplate;
import com.github.loadup.commons.util.ValidateUtils;
import com.github.loadup.modules.upms.client.api.UserService;
import com.github.loadup.modules.upms.client.cmd.*;
import com.github.loadup.modules.upms.client.dto.SimpleUserDTO;
import com.github.loadup.modules.upms.client.dto.UserDTO;
import com.github.loadup.modules.upms.client.query.*;
import com.github.loadup.modules.upms.domain.UpmsUser;
import com.github.loadup.modules.upms.gateway.UserGateway;
import com.github.loadup.modules.upms.service.impl.convertor.UserDTOConvertor;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserGateway userGateway;

    @Override
    public SingleResponse<UserDTO> findById(IdQuery query) {
        Optional<UpmsUser> user = userGateway.findById(query.getId());
        if (user.isEmpty()) {
            return SingleResponse.buildFailure();
        }
        UserDTO userDTO = UserDTOConvertor.INSTANCE.toUserDTO(user.get());
        return SingleResponse.of(userDTO);
    }

    @Override
    public MultiResponse<UserDTO> listByIds(IdListQuery query) {
        List<UserDTO> dtoList = query.getIdList().stream().map(v -> userGateway.findById(v)).map(
                v -> UserDTOConvertor.INSTANCE.toUserDTO(v.get())).collect(Collectors.toList());
        return MultiResponse.of(dtoList);
    }

    @Override
    public SingleResponse<UserDTO> findByUserAccount(UserAccountQuery query) {
        UpmsUser user = userGateway.findByAccount(query.getAccount());
        UserDTO userDTO = UserDTOConvertor.INSTANCE.toUserDTO(user);
        return SingleResponse.of(userDTO);
    }

    @Override
    public SingleResponse<UserDTO> findByUserMobile(UserMobileQuery query) {
        Optional<UpmsUser> user = userGateway.findByMobile(query.getMobile());
        if (user.isEmpty()) {
            return SingleResponse.buildFailure();
        }
        UserDTO userDTO = UserDTOConvertor.INSTANCE.toUserDTO(user.get());
        return SingleResponse.of(userDTO);
    }

    @Override
    public MultiResponse<SimpleUserDTO> listByRoleIds(UserRoleListQuery query) {
        return ServiceTemplate.execute((Void) -> ValidateUtils.validate(query), // check parameter
                () -> { // process
                    List<UpmsUser> userList = userGateway.findByRoleIdList(query.getIdList());
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
    public MultiResponse<SimpleUserDTO> listByDeptIds(UserDeptListQuery query) {
        return null;
    }

    @Override
    public MultiResponse<SimpleUserDTO> listByPostIds(UserPostListQuery query) {
        return null;
    }

    @Override
    public SingleResponse<SimpleUserDTO> save(UserSaveCmd cmd) {
        return ServiceTemplate.execute((Void) -> ValidateUtils.validate(cmd), // check parameter
                () -> { // process
                    SimpleUserDTO dto = cmd.getUser();
                    UpmsUser user = UserDTOConvertor.INSTANCE.toUser(dto);
                    String plantPassword = dto.getPassword();
                    user.setPassword(plantPassword);
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
                    Optional<UpmsUser> user = userGateway.findById(cmd.getUserId());
                    if (user.isEmpty()) {
                        return SingleResponse.buildFailure(CommonResultCodeEnum.NOT_FOUND);
                    }
                    userGateway.saveUserRoles(cmd.getUserId(), cmd.getRoleIdList());
                    return findById(IdQuery.of(cmd.getUserId()));
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
                    if (!userGateway.exist(userId)) {
                        return Response.buildFailure(CommonResultCodeEnum.NOT_FOUND);
                    }
                    if (!userGateway.validatePassword(userId, cmd.getOldPassword())) {
                        return Response.buildFailure(CommonResultCodeEnum.PROCESS_FAIL);
                    }
                    userGateway.changePassword(userId, cmd.getNewPassword());
                    return Response.buildSuccess();
                },
                //
                (e) -> Result.buildFailure(CommonResultCodeEnum.UNKNOWN),
                //
                (Void) -> {
                });
    }

    @Override
    public Response resetPassword(UserResetPasswordCmd cmd) {
        return null;
    }

    @Override
    public Response lockUser(UserStatusUpdateCmd cmd) {
        return null;
    }

    @Override
    public Response unLockUser(UserStatusUpdateCmd cmd) {
        return null;
    }

    @Override
    public Response deleteUser(UserDeleteCmd cmd) {
        return null;
    }
}
