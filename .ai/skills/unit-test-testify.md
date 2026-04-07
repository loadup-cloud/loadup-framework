# Skill: Unit Test with Testify — LoadUp 测试框架专属规范

> loadup-testify 是项目内部封装的声明式集成测试框架，通过 YAML 文件驱动测试用例。
> 本技能基于 `loadup-testify-spring-boot-starter` 和 `loadup-components-testcontainers` 真实源码编写。

---

## 1. 框架架构

```
@Test 方法
    ↓ TestifyBase.runTest(lambda)
    ↓ YamlLoader — 加载与方法同名的 .yaml 文件
    ↓ VariableEngine — 解析 ${faker.xxx} / ${time.xxx} / ${fn.xxx} 变量
    ↓ MockEngine — AOP 拦截，替换 Bean 方法返回值
    ↓ SqlExecutionEngine — 执行 clean_sql / db_setup 准备数据
    ↓ 业务方法调用（lambda 中的实际代码）
    ↓ AssertionFacade
        ├── ResponseAssertEngine   — 断言返回值（JsonPath + 递归字段比对）
        ├── DbAssertEngine         — 断言数据库状态（轮询重试）
        └── ExceptionAssertEngine  — 断言异常类型和消息
```

---

## 2. 依赖配置

```xml
<!-- loadup-modules-{mod}-test/pom.xml -->
<dependencies>
    <!-- 业务模块 -->
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-modules-{mod}-app</artifactId>
    </dependency>
    <!-- 测试框架 -->
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
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-components-testcontainers</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-components-database</artifactId>
    </dependency>
</dependencies>
```

---

## 3. 集成测试类（真实 DB，首选）

```java
package io.github.loadup.modules.{mod}.service;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.testify.TestifyBase;
import io.github.loadup.testify.annotation.TestifyParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(classes = {Mod}TestApplication.class)
@EnableTestContainers(ContainerType.MYSQL)   // 启动真实 MySQL 容器，禁止用 @MockBean 替代
class {Entity}ServiceIT extends TestifyBase {

    @Autowired
    private {Entity}Service service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // 清理测试数据，保证用例独立性
        jdbcTemplate.execute("DELETE FROM {table} WHERE {condition}");
    }

    // YAML 文件名必须与方法名一致：testCreate.yaml
    @Test
    void testCreate(@TestifyParam("entity") {Entity}CreateCommand cmd) {
        runTest(() -> service.create(cmd));
    }

    // 无入参时 YAML input 为位置列表
    @Test
    void testGetById(String id) {
        runTest(() -> service.getById(id));
    }
}
```

**ContainerType 枚举全量值**：

| 值 | 容器 |
|----|------|
| `MYSQL` | MySQL 8.0 |
| `POSTGRESQL` | PostgreSQL |
| `MONGODB` | MongoDB |
| `REDIS` | Redis |
| `KAFKA` | Kafka |
| `ELASTICSEARCH` | Elasticsearch |
| `LOCALSTACK` | LocalStack (AWS) |

---

## 4. 单元测试（纯逻辑，无 DB）

```java
@ExtendWith(MockitoExtension.class)
class {Entity}ServiceTest {

    @Mock
    {Entity}Gateway gateway;

    @InjectMocks
    {Entity}Service service;

    @Test
    void create_shouldThrow_whenKeyExists() {
        when(gateway.existsById(any())).thenReturn(true);
        assertThatThrownBy(() -> service.create(new {Entity}CreateCommand()))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

---

## 5. YAML 测试用例完整参考

YAML 文件放在 `src/test/resources/` 下，文件名 = 测试方法名（如 `testCreate.yaml`）。

### 5.1 完整结构示例

```yaml
# 1. 变量定义
variables:
  userId: "test-12345"
  userName: "${faker.name().firstName()}"   # Datafaker 生成
  email: test@example.com
  nowTime: ${time.now()}                    # 当前时间

# 2. 测试方法入参
input:
  # 位置列表（映射到方法参数位置）
  - "arg1-value"
  - "arg2-value"
  # OR 命名 Map（搭配 @TestifyParam("key") 使用）
  entity:
    userId: '${userId}'
    userName: '${userName}'
    email: ${email}

# 3. 数据准备
setup:
  # 方式 A：直接 SQL
  clean_sql: DELETE FROM users WHERE user_id = '${userId}';

  # 方式 B：内联数据行
  db_setup:
    - table: users
      data:
        - user_id: "${userId}"
          user_name: "Admin"
          status: ACTIVE
          created_at: '2026-01-01 00:00:00'

  # 方式 C：CSV 文件
  db_setup:
    - table: users
      file: users.csv        # 相对于 test/resources

# 4. Mock 配置
mocks:
  # 模拟返回值
  - bean: "orderService"
    method: "createOrder"
    args:
      orderId: "${userId}"   # 精确匹配参数
      orderName: "${userName}"
    thenReturn:
      orderId: "${userId}"
      orderName: "${userName}"
    delay: 500               # 模拟延迟（ms，可选）

  # 模拟抛异常
  - bean: "paymentService"
    method: "pay"
    args: ["any"]            # "any" 通配任意参数
    thenThrow:
      type: 'java.lang.IllegalArgumentException'
      message: 'Simulated exception for userId: ${userId}'

# 5. 断言
expect:
  # 断言返回值
  response:
    userId: "${userId}"                          # 等值断言（默认 eq）
    createdAt: {op: approx, val: ${nowTime}}     # 时间近似断言
    "$.order.orderId": "${userId}"               # JsonPath 断言
    "$.nested.field": {op: contains, val: "key"} # 包含断言

  # 断言数据库状态
  database:
    table: users
    mode: strict   # strict：只断言指定行，不检查额外行
    rows:
      - _match:                              # 定位行的条件
          user_id: "${userId}"
        user_name: {op: contains, val: ${userName}}
        email: {op: regex, val: .+@example\.com}
        status: ACTIVE
        created_at: {op: approx, val: ${nowTime}}

  # 断言异常
  exception:
    type: 'java.lang.IllegalArgumentException'
    message: 'User ID cannot be empty'
```

### 5.2 变量函数速查

| 表达式 | 说明 |
|--------|------|
| `${faker.name().firstName()}` | 随机名字（Datafaker） |
| `${faker.internet().emailAddress()}` | 随机邮箱 |
| `${faker.random().nextLong(1000)}` | 随机整数 |
| `${time.now()}` | 当前时间（LocalDateTime.now().toString()） |
| `${time.now("+1d")}` | 当前时间 + 1 天（支持 `+/-` + 数字 + `h/d/m/s`） |
| `${time.format("yyyy-MM-dd")}` | 格式化当前时间 |
| `${time.epochMilli()}` | 当前时间戳（毫秒） |
| `${fn.uuid()}` | 随机 UUID |

### 5.3 断言运算符（`op` 字段）

| op | 含义 | 支持类型 |
|----|------|---------|
| `eq`（默认，可省略） | 等值，null 安全，数字用 BigDecimal 比较 | 任意 |
| `gt` / `ge` / `lt` / `le` | 数值大小比较 | Number |
| `regex` | 正则匹配（String.matches） | String |
| `approx` | 时间近似（误差 ≤ 3000ms） | Number / TemporalAccessor / Date / ISO 字符串 |
| `contains` | 字符串包含 | String |
| `size` | 集合大小等于期望值 | Collection |
| `json` | JSONAssert LENIENT 比较 | String (JSON) |

---

## 6. 三文件 YAML 规范（缺一不可）

每个 `*-test` 模块 `src/test/resources/` 必须包含三个文件：

```
application.yml          ← 入口，固定内容
application-test.yml     ← 本地开发配置
application-ci.yml       ← CI 流水线配置
```

### application.yml（固定，不变）

```yaml
spring:
  profiles:
    active: test
```

### application-test.yml（本地开发）

```yaml
spring:
  application:
    name: loadup-{mod}-test
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      pool-name: LoadupTestPool
      minimum-idle: 2
      maximum-pool-size: 10
      connection-timeout: 30000
      connection-test-query: SELECT 1
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
      continue-on-error: false

mybatis-flex:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    cache-enabled: false
  global-config:
    print-sql: true

testcontainers:
  reuse:
    enable: true    # 本地启用重用，加快启动速度

logging:
  level:
    io.github.loadup: DEBUG
    org.springframework.jdbc: DEBUG
```

### application-ci.yml（CI 环境）

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-test-query: SELECT 1
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
      continue-on-error: false

mybatis-flex:
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
  global-config:
    print-sql: false

testcontainers:
  reuse:
    enable: false   # CI 禁用重用

logging:
  level:
    root: WARN
    io.github.loadup: INFO
```

---

## 7. 测试模块结构

```
loadup-modules-{mod}-test/
├── pom.xml   ← parent 必须指向根 loadup-parent，不得指向模块 pom
└── src/
    └── test/
        ├── java/
        │   └── io/github/loadup/modules/{mod}/
        │       ├── {Mod}TestApplication.java   ← @SpringBootApplication
        │       ├── {Entity}ServiceIT.java       ← 集成测试
        │       └── {Entity}ServiceTest.java     ← 单元测试
        └── resources/
            ├── application.yml
            ├── application-test.yml
            ├── application-ci.yml
            ├── schema.sql                       ← 必须与生产 schema.sql 完全一致
            └── testCreate.yaml                  ← 测试用例（方法名同名）
```

---

## 8. 命名约定

| 约定 | 规则 |
|------|------|
| 集成测试文件 | `XxxServiceIT.java` |
| 单元测试文件 | `XxxServiceTest.java` |
| 测试方法名 | `methodName_shouldResult_whenCondition` |
| YAML 用例文件 | 与测试方法名完全一致（如 `create_shouldPersist_whenValidCommand.yaml`） |

---

## 9. 禁止项

| 禁止行为 | 正确做法 |
|---------|---------|
| 集成测试用 `@MockBean` 替代 DB | `@EnableTestContainers(ContainerType.MYSQL)` |
| test 模块 `<parent>` 指向模块 pom | 统一指向根 `loadup-parent` |
| `schema.sql` 测试版与生产版字段不一致 | 测试 schema 同步维护 |
| 多个测试用例共用脏数据 | 每个 `@Test` 用 `@BeforeEach` 清理 |
