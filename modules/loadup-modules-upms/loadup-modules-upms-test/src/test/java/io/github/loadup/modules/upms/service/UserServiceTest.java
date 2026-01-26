package io.github.loadup.modules.upms.service;

import io.github.loadup.modules.upms.TestApplication;
import io.github.loadup.modules.upms.app.service.UserService;
import io.github.loadup.testify.starter.base.TestifyBase;
import io.github.loadup.upms.api.command.UserCreateCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testng.annotations.Test;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
public class UserServiceTest extends TestifyBase {

  @Autowired private UserService userService;

  @Test(dataProvider = "testifyData")
  public void testCreateUser(UserCreateCommand cmd) {
    runTest(() -> userService.createUser(val(cmd)));
  }
}
