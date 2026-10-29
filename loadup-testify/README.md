# LoadUp Testify

Testify 是用于 JUnit Jupiter / Spring Boot Test 的测试辅助工具，提供案例文件加载、变量展开、数据准备、响应/异常/数据库断言。测试顺序和业务调用由普通
Java 测试控制，适合 Service、Facade 和持久化集成测试。

它不是测试编排平台、HTTP 客户端、Mock 平台或生产组件。所有消费依赖使用 `test` scope。

## 接入路径

| 需求                         | 使用方式                             | 必需条件                           |
|------------------------------|--------------------------------------|------------------------------------|
| 对象、JSON、JsonPath 断言    | `ScenarioAssert.verify / verifyJson` | Testify starter                    |
| 异常断言                     | `ScenarioAssert.verifyException`     | 测试代码捕获 Throwable             |
| SQL 数据准备/清理            | `TestScenario`                       | JdbcTemplate 与独立测试数据库      |
| 简单异步数据库断言           | `TestScenario.assertDb`              | JdbcTemplate                       |
| 多行、计数、操作符数据库断言 | `ScenarioAssert.verifyDb`            | DbAssertEngine 与 JdbcTemplate     |
| 文件批量用例                 | `CaseFiles` + `@MethodSource`        | 显式解析变量、绑定 input、调用业务 |
| 真实数据库容器               | Testcontainers 组件                  | 额外 test 依赖、Docker             |
| Mock 外部协作者              | Spring Test / Mockito                | 使用其原生 API，Testify 不包装     |

## Maven 接入

消费工程先引入 LoadUp BOM，再添加以下依赖；版本由 BOM 管理。JUnit 版本跟随所选 BOM，注解包仍为 `org.junit.jupiter`。starter
不传递 JUnit 或 Spring Boot Test。

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

真实 MySQL 集成测试另外引入 `loadup-components-testcontainers` 和 `com.mysql:mysql-connector-j`，均使用 test
scope。数据库初始化由消费工程通过 Flyway 或 Spring SQL init 负责；Testify 不创建业务表。

## 最小响应断言

以下片段可直接放入已有 Spring Boot 测试类；通过构造器注入 `io.github.loadup.testify.starter.scenario.ScenarioAssert`：

```java
private final ScenarioAssert assertions;

@Autowired
MyServiceTest(ScenarioAssert assertions) {
    this.assertions = assertions;
}

@Test
void verifiesResultFields() {
    assertions.verify(
            Map.of("status", "CREATED", "amount", 1200),
            Map.of("status", "CREATED", "amount", Map.of("op", "ge", "val", 1000)));
}
```

没有数据库时仍可使用响应、异常和变量引擎，但 starter 会引入 JDBC 栈。需要排除 Boot 4 的
`org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration`，或者自行配置测试 DataSource。不要注入此时不存在的
TestScenario；调用 verifyDb 会返回说明缺少数据库断言引擎的 AssertionError。

## 数据库接入与隔离

建议目录：

```text
src/test/java/.../OrderCreateIT.java
src/test/resources/application.yml
src/test/resources/application-test.yml
src/test/resources/application-ci.yml
src/test/resources/testcases/order/create.yaml
```

`application.yml` 激活 test profile；本地与 CI 分别维护容器/数据库配置。基于已有测试启动类使用
`@SpringBootTest(classes = TestApplication.class)`、`@ActiveProfiles("test")` 和
`@EnableTestContainers(ContainerType.MYSQL)`。容器注解来自 `io.github.loadup.components.testcontainers.annotation`。

最小本地配置示例（实际 schema.sql 由消费工程提供）：

```yaml
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
loadup:
  testcontainers:
    enabled: true
    mysql:
      enabled: true
      image: mysql:8.0
```

Testify 没有独立的 `loadup.testify.*` 开关或超时配置；容器配置属于 Testcontainers 组件，轮询超时由
API/断言数据传入。具体容器配置见 [Testcontainers 接入文档](../loadup-components/platform/loadup-components-testcontainers/README.md)。

注入 `TestScenario` 后：

```java
String id = scenario.uuid();
scenario.clean("payment_order", "id", id);
scenario.insert("payment_order", Map.of("id", id, "status", "CREATED"));

scenario.assertDb("payment_order")
        .where("id", id)
        .withTimeout(Duration.ofSeconds(10))
        .exists()
        .has("status", "CREATED");
```

这是数据准备与断言片段，真实业务调用应插入两者之间。若表采用 BaseDO 标准字段，使用 JDBC 准备数据时必须自行提供数据库要求的
tenant_id、created_at、updated_at、deleted 等值；JDBC 不会触发 Flex 监听器。

| API                           | 语义                                   |
|-------------------------------|----------------------------------------|
| `clean(table)`                | 物理删除整表记录，仅限独占测试库       |
| `clean(table, column, value)` | 按一个条件物理删除                     |
| `insert(table, row)`          | 参数化插入；不自动展开变量或补审计字段 |
| `assertDb(table).where(...)`  | 可累加相等条件                         |
| `exists / notExists`          | 等待有记录/没有记录                    |
| `has(column, expected)`       | 等待首条匹配记录的列值满足相等比较     |
| `withTimeout(Duration)`       | 为后续链式断言设置等待上限             |

标识符只接受 `[A-Za-z0-9_]+`；反引号、点号、schema 前缀和 SQL 表达式不受支持。避免保留字表名。默认等待5秒、每100ms轮询；每个链式断言单独等待，整条链不共享一个5秒截止时间。
`has` 的多行查询没有指定排序，应使用唯一 ID 条件。

轮询不代表事务提交。异步工作通常看不到测试事务中尚未提交的数据；避免靠测试外层 `@Transactional` 覆盖真实异步场景。使用唯一业务
ID 和限定范围清理；共享容器并行测试不能使用整表 clean。逻辑删除验证应查询 `deleted` 的真实值，clean 始终是物理删除。

## 声明式响应与操作符

```java
assertions.verify(actual, Map.of(
        "$.result.code", "SUCCESS",
        "$.data.id", "${id}",
        "$.data.amount", Map.of("op", "gt", "val", 0)), Map.of("id", id));
```

`SUCCESS` 仅用于协议示例，请使用实际项目的结果码。普通对象期望按字段递归检查，允许实际对象包含额外字段；键以 `$` 开头时走
JsonPath。`verifyJson` 接收原始 JSON 字符串。不同 API
的完整语义和限制见 [断言引擎 README](loadup-testify-assert-engine/README.md)。

| 操作符            | 示例                                           | 注意事项                              |
|-------------------|------------------------------------------------|---------------------------------------|
| 普通值 / eq       | `status: CREATED`                              | 默认相等比较                          |
| gt / ge / lt / le | `{op: ge, val: 100}`                           | 数值比较                              |
| regex             | `{op: regex, val: '^[A-Z]+$'}`                 | 正则匹配                              |
| contains          | `{op: contains, val: order}`                   | 字符串包含                            |
| size              | `{op: size, val: 2}`                           | 集合大小                              |
| approx            | `{op: approx, val: '${now}', threshold: 1000}` | 时间差；threshold 为毫秒，默认3000    |
| json              | `{op: json, val: '{"ok":true}', mode: full}`   | 默认 lenient；full 使用严格 JSON 比较 |

不要把未知操作符当作能力扩展：当前实现会回退相等比较，并非立即报不支持。顶层操作符 Map、顶层数组、空期望与变量展开存在入口差异；推荐使用对象字段或
JsonPath 下的操作符。

## 数据库与异常声明

```yaml
table: payment_order
mode: lenient
timeout: 5000
interval: 200
rows:
  - _match: {id: '${id}'}
    id: '${id}'
    status: CREATED
```

将上述结构作为 Map、JsonNode 或 JSON 文本传入 `assertions.verifyDb(expected, variables)`。默认 timeout 为0（立即检查），interval
为500ms，单位均为毫秒。它与 TestScenario 的5秒/100ms规则不同。`strict` 会检查获取到的记录数量；不是天然的整张表一致性校验。数据库集合匹配、
`_count` 和读取范围详见断言引擎文档。

```java
Throwable failure = assertThrows(CommonException.class, () -> service.execute(command));
assertions.verifyException(failure, Map.of("type", "CommonException"));
```

业务代码和 service.execute 为接入示意。异常引擎检查最深层 cause，因此包装异常未必按外层类型匹配；稳定 ResultCode 建议直接使用
AssertJ/JUnit 断言外层 CommonException。声明式 message 当前主要用于文本相等比较。

## 批量案例文件

`src/test/resources/testcases/echo.yaml`：

```yaml
cases:
  - name: echo supplied identifier
    variables:
      id: '${fn.uuid()}'
    input:
      id: '${id}'
      status: CREATED
    expect:
      id: '${id}'
      status: CREATED
```

下例演示可直接运行的加载/变量/断言流程，`actual` 为输入回显；接入业务时替换回显为 Service 调用：

```java
static Stream<TestifyCase> cases() {
    return CaseFiles.loadYaml("testcases/echo.yaml").stream();
}

@ParameterizedTest(name = "{0}")
@MethodSource("cases")
void executeCase(TestifyCase testCase) {
    Map<String, Object> variables = variableEngine.resolveVariables(testCase.variables());
    Object input = variableEngine.resolveValue(testCase.input(), variables);
    Object actual = input;
    assertions.verify(actual, testCase.expect(), variables);
}
```

对应类型分别来自 `starter.cases`、`data.engine.variable` 和 `starter.scenario`。CaseFiles 只读 classpath
数据，不负责执行、注入、清理或自动解析变量。支持 `cases: [...]`、根数组和单对象；字段别名为
name/id、input/params、expect/expected。对象绑定可用 Testify `JsonUtil.convertValue`，但它使用独立测试 Mapper，不继承 WebMVC
全局 Jackson 配置。

## 变量与扩展

常用表达式：`${fn.uuid()}`、`${fn.random(1,100)}`、`${fn.randomString(8)}`、`${time.now('-2h')}`、
`${time.format('uuuuMMdd')}`、`${time.epochMilli()}`、`${faker.name().firstName()}`、`${id}`。random 上界不包含在内；时间默认使用系统本地时区。

必须先调用 resolveVariables，再把结果作为 resolveValue / verify 的 context。跨变量依赖最多尝试10轮；无法解析的占位符可能保持原样或不出现在结果
context，测试代码应主动校验必要变量。案例文件应来自可信测试资源，SpEL 不是不可信输入的沙箱。

自定义函数实现 `TestifyFunction`，然后提供一个替代 `VariableEngine` Bean，显式包含内置 time/fn 与自定义函数。仅声明函数
Bean 不会被默认自动配置收集；完整示例见 [数据引擎 README](loadup-testify-data-engine/README.md)。

## 执行与排错

本仓库的可执行示例见 [测试模块](loadup-testify-test/README.md)。需要运行时使用 Maven Daemon 和 clean，按目的选择：

```shell
# 无数据库的声明式断言/装配回归
mvnd clean test -pl loadup-testify/loadup-testify-test -am \
  -Dtest=ScenarioAssertTest,TestifyAutoConfigurationTest \
  -Dsurefire.failIfNoSpecifiedTests=false

# 真实 MySQL 集成示例，要求 Docker；使用 Surefire 显式选取 IT
mvnd clean test -pl loadup-testify/loadup-testify-test -am \
  -Dtest=UserServiceIT,UserServiceBatchIT \
  -Dsurefire.failIfNoSpecifiedTests=false
```

命令是手动验证入口，不表示本次文档更新已执行测试。不要把源码存在或用例被编译写成运行通过。

| 问题                         | 排查方向                                                                     |
|------------------------------|------------------------------------------------------------------------------|
| 找不到 TestScenario          | JdbcTemplate Bean 是否存在，数据源是否成功创建                               |
| 无数据库仍报 DataSource 错误 | JDBC starter 的自动配置仍开启，按上述路径排除                                |
| SQL 标识符非法               | 去掉反引号/schema/表达式，改用合法表名                                       |
| strict 行数不符              | 查看 `_match` 读取范围、共享测试数据和 LIMIT 100 回退                        |
| 异步断言超时                 | 事务是否提交、后台线程租户上下文、条件是否唯一、数据库是否可达               |
| `${...}` 未展开              | 是否先解析 variables，函数是否注册，是否超出依赖轮数                         |
| HTTP 报文和对象断言结果不同  | 用真实 HTTP/MockMvc 的序列化结果验证，而不是把测试 Mapper 当成 WebMVC Mapper |

## 子模块文档

- [Core 接入](loadup-testify-core/README.md) / [设计](loadup-testify-core/ARCHITECTURE.md)
- [数据引擎接入](loadup-testify-data-engine/README.md) / [设计](loadup-testify-data-engine/ARCHITECTURE.md)
- [断言引擎接入](loadup-testify-assert-engine/README.md) / [设计](loadup-testify-assert-engine/ARCHITECTURE.md)
- [Starter 接入](loadup-testify-spring-boot-starter/README.md) / [设计](loadup-testify-spring-boot-starter/ARCHITECTURE.md)
- [测试示例](loadup-testify-test/README.md) / [设计](loadup-testify-test/ARCHITECTURE.md)
