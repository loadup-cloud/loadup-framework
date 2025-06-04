/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.modules.upms.service.test;

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

import com.github.loadup.commons.result.MultiResponse;
import com.github.loadup.commons.result.Response;
import com.github.loadup.commons.result.SingleResponse;
import com.github.loadup.commons.util.PasswordUtils;
import com.github.loadup.commons.util.RandomUtil;
import com.github.loadup.modules.upms.client.api.RoleService;
import com.github.loadup.modules.upms.client.api.UserService;
import com.github.loadup.modules.upms.client.cmd.*;
import com.github.loadup.modules.upms.client.dto.*;
import com.github.loadup.modules.upms.client.query.UserRoleListQuery;
import com.github.loadup.modules.upms.dal.dataobject.UserDO;
import com.github.loadup.modules.upms.dal.repository.UserRepository;
import com.github.loadup.modules.upms.test.TestApplication;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestApplication.class)
public class UserServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleService roleService;

    @Test
    public void testSave() {
        SingleResponse<SimpleUserDTO> userById = createUser();
        Assertions.assertEquals("SUCCESS", userById.getResult().getCode());
        Assertions.assertEquals("ls", userById.getData().getNickName());
    }

    @Test
    public void testChangePassword() throws InterruptedException {
        String oldPwd = "123456";
        String newPwd = "12345678";
        SingleResponse<SimpleUserDTO> save = createUser();
        UserChangePasswordCmd changeCmd = new UserChangePasswordCmd();
        String userId = save.getData().getId();
        changeCmd.setId(userId);
        changeCmd.setOldPassword(oldPwd);
        changeCmd.setNewPassword(newPwd);
        changeCmd.setConfirmPassword(newPwd);
        // ThreadUtils.sleep(Duration.ofSeconds(2));
        Response response = userService.changePassword(changeCmd);
        Assertions.assertTrue(response.getResult().getCode() == "SUCCESS");
        Optional<UserDO> userDO = userRepository.findById(userId);
        Assertions.assertTrue(response.getResult().getCode() == "SUCCESS");
        userDO.ifPresent(user ->
                Assertions.assertEquals(newPwd, PasswordUtils.decrypt(user.getPassword(), newPwd, user.getSalt())));
    }

    @Test
    public void testSaveUserRoles() {
        SingleResponse<SimpleUserDTO> userDTO = createUser();
        SingleResponse<SimpleRoleDTO> roleDTO = createRole();

        UserRolesSaveCmd userRolesSaveCmd = new UserRolesSaveCmd();
        userRolesSaveCmd.setUserId(userDTO.getData().getId());
        List<String> roles = new ArrayList<>();
        roles.add(roleDTO.getData().getId());
        userRolesSaveCmd.setRoleIdList(roles);
        SingleResponse<UserDTO> response = userService.saveUserRoles(userRolesSaveCmd);
        Assertions.assertEquals("SUCCESS", response.getResult().getCode());
        Assertions.assertTrue(response.getData().getRoleList().size() == 1);

        SingleResponse<UserDTO> response2 = userService.saveUserRoles(userRolesSaveCmd);
        Assertions.assertEquals("SUCCESS", response2.getResult().getCode());
        Assertions.assertEquals(1, response2.getData().getRoleList().size());
    }

    @Test
    public void testListByRoleIds() {
        SingleResponse<SimpleUserDTO> userDTO = createUser();
        SingleResponse<SimpleRoleDTO> roleDTO = createRole();
        SingleResponse<SimpleRoleDTO> roleDTO2 = createRole();

        UserRolesSaveCmd userRolesSaveCmd = new UserRolesSaveCmd();
        userRolesSaveCmd.setUserId(userDTO.getData().getId());
        List<String> roles = new ArrayList<>();
        roles.add(roleDTO.getData().getId());
        roles.add(roleDTO2.getData().getId());
        userRolesSaveCmd.setRoleIdList(roles);

        UserRoleListQuery query = new UserRoleListQuery();
        query.setIdList(roles);
        MultiResponse<SimpleUserDTO> response = userService.listByRoleIds(query);
        Assertions.assertEquals("SUCCESS", response.getResult().getCode());
        List<SimpleUserDTO> userDTOList = response.getData();
        Assertions.assertEquals(1, userDTOList.size());

        UserDeleteCmd cmd = new UserDeleteCmd();
        userService.deleteUser(cmd);
    }

    private SingleResponse<SimpleRoleDTO> createRole() {
        RoleSaveCmd roleSaveCmd = new RoleSaveCmd();
        SimpleRoleDTO roleDTO = new SimpleRoleDTO();
        roleDTO.setRoleCode(RandomUtil.randomString(6));
        roleDTO.setRoleName("测试");
        roleSaveCmd.setRole(roleDTO);
        SingleResponse<SimpleRoleDTO> roleSaveResponse = roleService.save(roleSaveCmd);
        return roleSaveResponse;
    }

    private SingleResponse<SimpleUserDTO> createUser() {
        UserSaveCmd cmd = new UserSaveCmd();
        UserSaveDTO dto = new UserSaveDTO();
        dto.setAccount("ls");
        dto.setPassword("123456");
        cmd.setUser(dto);
        SingleResponse<SimpleUserDTO> userDTO = userService.save(cmd);
        return userDTO;
    }
}
