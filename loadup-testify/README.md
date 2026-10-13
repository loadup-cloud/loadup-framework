# LoadUp Testify

Testify 是面向 JUnit 6.0.3 + Spring Boot Test 的轻量测试辅助组件，提供测试数据准备、
清理和数据库异步断言，减少集成测试中的重复代码。

## 引入

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-testify-spring-boot-starter</artifactId>
    <scope>test</scope>
</dependency>
```

## 模块划分

| 模块 | 职责 |
|------|------|
| `loadup-testify-core` | 共享工具（`JsonUtil` 等） |
| `loadup-testify-data-engine` | 变量引擎：SpEL / Faker / 自定义函数（`time`、`fn`）解析 |
| `loadup-testify-assert-engine` | 断言引擎：Response / Db / Exception + 操作符 + diff 报告 |
| `loadup-testify-spring-boot-starter` | 对外 API：`TestScenario` / `ScenarioAssert` / `CaseFiles` + 自动装配 |
| `loadup-testify-test` | 集成测试 Demo |

依赖方向严格单向：core → data-engine → assert-engine → spring-boot-starter → test。

JUnit Jupiter 与 Platform 统一使用 6.0.3，由 LoadUp BOM 继承的 Spring Boot 4.1.1 BOM 管理。
消费工程不为 JUnit 模块单独指定版本；测试注解仍使用 `org.junit.jupiter` 包。
Testify starter 不传递 JUnit 或 Spring Boot Test；消费工程显式引入上述测试 starter。
响应和异常断言可在没有 `JdbcTemplate` Bean 时使用；`TestScenario` 与数据库断言引擎仅在该 Bean 存在时装配。
无数据库的测试工程应排除 Boot 的 `DataSourceAutoConfiguration`，避免 JDBC starter 尝试创建数据源。
未装配数据库断言引擎时调用 `verifyDb` 会抛出说明缺失依赖的 `AssertionError`。

## 使用

注入 `TestScenario`，在测试方法中用 Java 描述数据准备与断言：

```java
@SpringBootTest
@EnableTestContainers(ContainerType.MYSQL)
class OrderCreateIT {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TestScenario scenario;

    @Test
    void createOrder_shouldPersist() {
        String orderId = scenario.uuid();

        scenario.clean("`order`", "id", orderId);
        scenario.insert("user", Map.of("id", "user-1", "status", 1));

        orderService.create(orderId);

        scenario.assertDb("`order`")
                .where("id", orderId)
                .exists()
                .has("status", "CREATED");
    }
}
```

## 能力

- `scenario.clean(table)` / `scenario.clean(table, column, value)`：数据清理，表名和列名白名单校验。
- `scenario.insert(table, row)`：参数化插入。
- `scenario.assertDb(table)`：异步轮询断言，默认 5 秒超时。
- `scenarioAssert.verify(actual, expected[, variables])`：声明式断言，支持 `regex` / `approx` /
  `contains` / `json` / `size` / `gt/ge/lt/le` 操作符和 JsonPath 提取，失败输出字段级 diff 报告。
- `CaseFiles.loadYaml(...)` + JUnit Jupiter `@ParameterizedTest`：从文件批量加载不同入参的用例，
  逐个执行并校验对应出参。
- Mock 使用 Spring Boot Test 原生 `@MockitoBean` / `@MockitoSpyBean` + Mockito，Testify 不重复封装。
- 容器使用 `loadup-components-testcontainers` 的 `@EnableTestContainers`，由开发者自行引入，Testify 不内置。

Testify 不提供 YAML 编排或全局 AOP Mock；测试流程用普通 JUnit Jupiter 表达，YAML/JSON 仅作为批量用例
的数据源（`CaseFiles`），断言由 `ScenarioAssert` 按操作符声明式执行。
