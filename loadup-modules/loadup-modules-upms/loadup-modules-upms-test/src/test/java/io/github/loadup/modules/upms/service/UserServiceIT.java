package io.github.loadup.modules.upms.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.modules.upms.TestApplication;
import io.github.loadup.modules.upms.app.dto.UserDetailDTO;
import io.github.loadup.modules.upms.app.service.UserService;
import io.github.loadup.modules.upms.client.command.UserCreateCommand;
import io.github.loadup.testify.starter.scenario.TestScenario;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestApplication.class)
@EnableTestContainers(ContainerType.MYSQL)
class UserServiceIT {

    @Autowired
    private UserService userService;

    @Autowired
    private TestScenario scenario;

    @BeforeEach
    void cleanUp() {
        scenario.clean("upms_user");
        scenario.clean("upms_department");
    }

    @Test
    void createUser_shouldPersistUser() {
        String departmentId = scenario.uuid();
        String departmentCode = "DEPT_" + scenario.uuid().replace("-", "").substring(0, 8);
        scenario.insert(
                "upms_department",
                Map.ofEntries(
                        Map.entry("id", departmentId),
                        Map.entry("tenant_id", "1"),
                        Map.entry("parent_id", "0"),
                        Map.entry("dept_name", "Test Department"),
                        Map.entry("dept_code", departmentCode),
                        Map.entry("dept_level", 1),
                        Map.entry("sort_order", 0),
                        Map.entry("status", 1),
                        Map.entry("remark", "test data"),
                        Map.entry("created_by", "tester"),
                        Map.entry("created_at", LocalDateTime.now()),
                        Map.entry("updated_by", "tester"),
                        Map.entry("updated_at", LocalDateTime.now()),
                        Map.entry("deleted", 0)));


        String username = "user_" + scenario.uuid().replace("-", "").substring(0, 12);
        String email = username + "@example.com";
        String mobile = "139" + scenario.uuid().replace("-", "").substring(0, 8);
        UserCreateCommand command = new UserCreateCommand();
        command.setUsername(username);
        command.setPassword("secret123");
        command.setNickname("Tester");
        command.setDeptId(departmentId);
        command.setEmail(email);
        command.setMobile(mobile);
        command.setStatus((short) 1);
        command.setCreatedBy("tester");

        UserDetailDTO dto = userService.createUser(command);

        assertThat(dto).isNotNull();
        scenario.assertDb("upms_user")
                .where("username", username)
                .exists()
                .has("status", 1)
                .has("email", email);
    }
}
