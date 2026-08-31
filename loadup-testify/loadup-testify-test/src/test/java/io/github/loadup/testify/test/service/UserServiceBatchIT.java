package io.github.loadup.testify.test.service;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.testify.core.util.JsonUtil;
import io.github.loadup.testify.data.engine.variable.VariableEngine;
import io.github.loadup.testify.starter.cases.CaseFiles;
import io.github.loadup.testify.starter.cases.TestifyCase;
import io.github.loadup.testify.starter.scenario.ScenarioAssert;
import io.github.loadup.testify.starter.scenario.TestScenario;
import io.github.loadup.testify.test.TestApplication;
import io.github.loadup.testify.test.model.User;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Data-driven integration test: cases are loaded from a YAML file, each case carries its own input
 * and expected output, and assertions are expressed declaratively with operators.
 */
@SpringBootTest(classes = TestApplication.class)
@EnableTestContainers(ContainerType.MYSQL)
class UserServiceBatchIT {

    @Autowired
    private UserService userService;

    @Autowired
    private TestScenario scenario;

    @Autowired
    private ScenarioAssert scenarioAssert;

    @Autowired
    private VariableEngine variableEngine;

    static Stream<TestifyCase> cases() {
        return CaseFiles.loadYaml("testcases/UserServiceIT/batch-create.yaml").stream();
    }

    @BeforeEach
    void cleanUp() {
        scenario.clean("users");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void createUser_batch(TestifyCase testCase) {
        Map<String, Object> variables = variableEngine.resolveVariables(testCase.variables());
        Object resolvedInput = variableEngine.resolveValue(testCase.input(), variables);
        User input = (User) JsonUtil.convertValue(resolvedInput, User.class);
        scenario.clean("users", "user_id", input.getUserId());

        User actual = userService.createUser(input);

        scenarioAssert.verify(actual, testCase.expect(), variables);
    }
}
