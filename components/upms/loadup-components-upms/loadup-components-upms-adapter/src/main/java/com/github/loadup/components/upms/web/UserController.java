package com.github.loadup.components.upms.web;

/*-
 * #%L
 * loadup-components-upms-adapter
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

import com.github.loadup.commons.request.query.IdQuery;
import com.github.loadup.commons.result.MultiResponse;
import com.github.loadup.commons.result.Response;
import com.github.loadup.commons.result.SingleResponse;
import com.github.loadup.components.tracer.TraceUtil;
import com.github.loadup.components.upms.client.api.UserService;
import com.github.loadup.components.upms.client.cmd.UserChangePasswordCmd;
import com.github.loadup.components.upms.client.cmd.UserSaveCmd;
import com.github.loadup.components.upms.client.dto.SimpleUserDTO;
import com.github.loadup.components.upms.client.dto.UserDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping(value = "/queryById")
    public SingleResponse<UserDTO> queryById(@RequestBody IdQuery query) {
        log.info("queryById:{}", TraceUtil.getTracerId());
        return userService.getUserById(query);
    }

    @PostMapping(value = "/queryByRoleId")
    public MultiResponse<SimpleUserDTO> queryByRoleId(@RequestBody IdQuery query) {
        return userService.getUserByRoleId(query.getId());
    }

    @PostMapping(value = "/changePassword")
    public Response changePassword(@RequestBody UserChangePasswordCmd cmd) {
        return userService.changePassword(cmd);
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody UserSaveCmd cmd) {
        return userService.save(cmd);
    }
}
