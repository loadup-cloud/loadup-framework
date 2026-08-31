# Testify Demo

演示 `loadup-testify-spring-boot-starter` 与 `loadup-components-testcontainers`
的组合用法。

## 运行

```bash
mvn test -pl loadup-testify/loadup-testify-test
```

需要 Docker；MySQL 容器由 `@EnableTestContainers(ContainerType.MYSQL)` 启动。

## 演示点

- `UserServiceIT.createUser_shouldPersistUserAndUseMockedOrderService`：
  `TestScenario` 数据清理、`@MockitoBean` 替换 `OrderService`、`assertDb` 异步断言。
- `UserServiceIT.getUserById_shouldReturnUserInsertedByScenario`：
  `TestScenario.insert` 预置数据后查询。
- `UserServiceBatchIT`：`CaseFiles` 从 YAML 批量加载入参/出参用例，
  `@ParameterizedTest` 逐条执行，`ScenarioAssert` 按操作符（regex / approx / contains / JsonPath）断言。
- `ScenarioAssertTest`：声明式断言操作符的单元测试。
