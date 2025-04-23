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

import com.github.loadup.commons.result.SingleResponse;
import com.github.loadup.modules.upms.client.api.RoleService;
import com.github.loadup.modules.upms.client.api.UserService;
import com.github.loadup.modules.upms.client.cmd.RoleSaveCmd;
import com.github.loadup.modules.upms.client.cmd.RoleUsersSaveCmd;
import com.github.loadup.modules.upms.client.cmd.UserSaveCmd;
import com.github.loadup.modules.upms.client.dto.RoleDTO;
import com.github.loadup.modules.upms.client.dto.SimpleRoleDTO;
import com.github.loadup.modules.upms.client.dto.SimpleUserDTO;
import com.github.loadup.modules.upms.test.TestApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = TestApplication.class)
public class RoleServiceTest {

    @Autowired
    private RoleService roleService;
    @Autowired
    private UserService userService;

    @Test
    public void testCreateRole() {
        SingleResponse<SimpleRoleDTO> response
                = createRole();
        Assertions.assertEquals("SUCCESS", response.getResult().getCode());
        Assertions.assertEquals("test_role", response.getData().getRoleCode());

    }

    @Test
    public void testSaveRoleUsers() {
        SingleResponse<SimpleUserDTO> userDTO = createUser();
        SingleResponse<SimpleRoleDTO> roleDTO = createRole();

        RoleUsersSaveCmd userRolesSaveCmd = new RoleUsersSaveCmd();
        userRolesSaveCmd.setRoleId(roleDTO.getData().getId());
        List<String> users = new ArrayList<>();
        users.add(userDTO.getData().getId());
        userRolesSaveCmd.setUserIdList(users);
        SingleResponse<RoleDTO> response = roleService.saveRoleUsers(userRolesSaveCmd);
        Assertions.assertEquals("SUCCESS", response.getResult().getCode());
        Assertions.assertTrue(response.getData().getUserList().size() == 1);

        SingleResponse<RoleDTO> response2 = roleService.saveRoleUsers(userRolesSaveCmd);
        Assertions.assertEquals("SUCCESS", response2.getResult().getCode());
        Assertions.assertEquals(1, response2.getData().getUserList().size());

    }

    private SingleResponse<SimpleUserDTO> createUser() {
        UserSaveCmd cmd = new UserSaveCmd();
        SimpleUserDTO dto = new SimpleUserDTO();
        dto.setNickname("ls");
        dto.setPassword("123456");
        cmd.setUser(dto);
        SingleResponse<SimpleUserDTO> userDTO = userService.save(cmd);
        return userDTO;
    }

    private SingleResponse<SimpleRoleDTO> createRole() {
        RoleSaveCmd cmd = new RoleSaveCmd();
        SimpleRoleDTO dto = new SimpleRoleDTO();
        dto.setRoleCode("test_role");
        dto.setRoleName("测试");
        cmd.setRole(dto);
        SingleResponse<SimpleRoleDTO> response = roleService.save(cmd);
        return response;
    }
}
