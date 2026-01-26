package com.github.loadup.modules.upms.service;

import com.github.loadup.modules.upms.app.service.UserService;
import com.github.loadup.testify.starter.base.TestifyBase;
import com.github.loadup.upms.api.command.UserCreateCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class UserServiceTest extends TestifyBase {

  @Autowired private UserService userService;

  @Test(
      dataProvider = "testifyData",
      dataProviderClass = com.github.loadup.testify.starter.testng.TestifyDataProvider.class)
  public void testCreateUser(UserCreateCommand cmd) {
    runTest(() -> userService.createUser(cmd));
  }
}
