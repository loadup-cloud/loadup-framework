package io.github.loadup.modules.upms.service;

/*-
 * #%L
 * Loadup UPMS Test
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

import io.github.loadup.modules.upms.TestApplication;
import io.github.loadup.modules.upms.app.service.UserService;
import io.github.loadup.testify.starter.base.TestifyBase;
import io.github.loadup.upms.api.command.UserCreateCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;

@SpringBootTest(classes = TestApplication.class)
public class UserServiceTest extends TestifyBase {

    @Autowired
    private UserService userService;

    @Test(dataProvider = "testifyData")
    public void testCreateUser(UserCreateCommand cmd) {
        runTest(() -> userService.createUser(val(cmd)));
    }
}
