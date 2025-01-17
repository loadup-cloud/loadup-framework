package com.github.loadup.components.upms.service.impl;

/*-
 * #%L
 * loadup-components-upms-app
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import com.github.loadup.commons.result.CommonResultCodeEnum;
import com.github.loadup.commons.result.MultiResponse;
import com.github.loadup.commons.result.Result;
import com.github.loadup.commons.result.SingleResponse;
import com.github.loadup.commons.template.ServiceTemplate;
import com.github.loadup.commons.util.ValidateUtils;
import com.github.loadup.components.upms.client.api.RoleService;
import com.github.loadup.components.upms.client.cmd.RoleSaveCmd;
import com.github.loadup.components.upms.client.cmd.RoleUsersSaveCmd;
import com.github.loadup.components.upms.client.dto.RoleDTO;
import com.github.loadup.components.upms.client.dto.SimpleRoleDTO;
import com.github.loadup.components.upms.domain.Role;
import com.github.loadup.components.upms.gateway.RoleGateway;
import com.github.loadup.components.upms.service.impl.convertor.RoleDTOConvertor;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class RoleServiceImpl implements RoleService {
    @Resource
    private RoleGateway roleGateway;

    @Override
    public SingleResponse<RoleDTO> getRoleById(String roleId) {
        Role role = roleGateway.getById(roleId);
        RoleDTO userDTO = RoleDTOConvertor.INSTANCE.toRoleDTO(role);
        return SingleResponse.of(userDTO);
    }

    @Override
    public MultiResponse<SimpleRoleDTO> getRoleByUserId(String userId) {
        List<Role> roleList = roleGateway.getByUserId(userId);
        List<SimpleRoleDTO> roleDTOList = RoleDTOConvertor.INSTANCE.toSimpleRoleDTO(roleList);
        return MultiResponse.of(roleDTOList);
    }

    @Override
    public SingleResponse<SimpleRoleDTO> save(RoleSaveCmd cmd) {
        return ServiceTemplate.execute(
                (Void) -> ValidateUtils.validate(cmd), // check parameter
                () -> { // process
                    SimpleRoleDTO dto = cmd.getRole();
                    Role role = RoleDTOConvertor.INSTANCE.toRole(dto);
                    if (StringUtils.isBlank(role.getStatus())) {
                        role.setStatus("NORMAL");
                    }
                    role = roleGateway.create(role);
                    return SingleResponse.of(RoleDTOConvertor.INSTANCE.toSimpleRoleDTO(role));
                },
                (e) -> Result.buildFailure(CommonResultCodeEnum.UNKNOWN),
                (Void) -> {
                }
        );
    }

    @Override
    public SingleResponse<RoleDTO> saveRoleUsers(RoleUsersSaveCmd cmd) {
        return ServiceTemplate.execute(
                (Void) -> ValidateUtils.validate(cmd),
                () -> {
                    Role role = roleGateway.getById(cmd.getRoleId());
                    if (Objects.isNull(role)) {
                        return SingleResponse.buildFailure(CommonResultCodeEnum.NOT_FOUND);
                    }
                    roleGateway.saveRoleUsers(cmd.getRoleId(), cmd.getUserIdList());
                    return getRoleById(role.getId());
                },
                (e) -> Result.buildFailure(CommonResultCodeEnum.UNKNOWN),
                (Void) -> {
                }
        );
    }
}
