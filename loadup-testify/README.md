# Testify 自动化测试框架深度开发手册

Testify 是一款基于 Spring Boot 生态构建的 **声明式、数据驱动** 集成测试框架。它通过 YAML 定义测试逻辑，利用 AOP 拦截技术实现动态
Mock，并提供了一套支持异步重试和复杂逻辑比对的断言系统。

---

## 一、 技术栈 (Technology Stack)

Testify 集成了以下核心技术，确保了框架的灵活性和强大功能：

* **核心引擎**: Java 17+, Spring Boot (IoC/AOP/ConversionService)
* **测试驱动**: TestNG (DataProvider 机制)
* **Mock 拦截**: Spring AOP (基于 `BeanPostProcessor` 的动态代理)
* **变量引擎**: Spring Expression Language (SpEL), Java Faker (数据模拟)
* **JSON 处理**: Jackson (序列化), Jayway JsonPath (路径提取)
* **断言增强**: JSONAssert (对象比对), 自定义 Operator 体系
* **数据库**: Spring JdbcTemplate (执行清理与校验)

---

## 二、 架构设计与核心流程

Testify 采用 **“基类驱动 + 闭包执行”** 的模式。

1. **DataProvider 映射**: `TestifyDataProvider` 解析 YAML 中的 `input` 列表，自动绑定到 `@Test` 方法的入参。
2. **变量生命周期**: 在测试方法执行前，`VariableEngine` 预解析 `variables`。
3. **AOP 拦截网**: `runTest` 执行期间，`MockInterceptor` 处于激活状态，根据配置拦截指定的 Bean。
4. **闭包执行 (Lambda)**：业务代码在 `runTest` 的闭包内运行，框架自动捕获异常并记录结果。
5. **自动断言**: 业务完成后，自动触发 Response 和 Database 断言。

---

## 三、 YAML 配置规范 (Full Reference)

### 3.1 变量配置 (`variables`)

支持动态函数，可在 YAML 其他位置通过 `${varName}` 引用。

```yaml
variables:
  userId: "${time.now()}"             # 自定义时间函数
  userName: "${faker.name().firstName()}" # Faker 函数
  email: "test@example.com"
  nowTime: ${time.now()}

```

### 3.2 参数输入 (`input`)

列表项顺序必须与 `@Test` 方法参数顺序完全一致。

```yaml
input:
  - "test-1234"      # 对应参数1
  - ${userName}      # 对应参数2 (解析变量)
  - "test@example.com" # 对应参数3

```

### 3.3 环境准备 (`setup`)

```yaml
setup:
  clean_sql: DELETE FROM users WHERE user_id = '${userId}'; # 执行清理
  # db_setup: 预置数据（可选）

```

### 3.4 智能 Mock (`mocks`)

基于 AOP 拦截，支持匹配 `any`、变量、或 JsonPath。

```yaml
mocks:
  - bean: "orderService"
    method: "createOrder"
    args: ["${userName}"] # 参数匹配
    thenReturn:           # 模拟成功返回
      orderId: "123456"
    # thenThrow: "com.xxx.BizException" # 模拟异常返回
    delay: 500             # 延时响应 (ms)

```

### 3.5 闭环断言 (`expect`)

#### (1) 异常断言 (`exception`) - **核心增强**

用于测试异常流，验证方法是否抛出了预期的异常及其描述。

```yaml
expect:
  exception:
    type: 'java.lang.IllegalArgumentException' # 异常全路径类名
    message: 'User ID cannot be empty'         # 异常消息（支持模糊匹配）

```

#### (2) Response 响应断言

支持递归比对和 JsonPath。

```yaml
expect:
  response:
    "$.userId": "test-1234"      # JsonPath 模式
    userName: ${userName}        # 递归属性模式
    createdAt:                   # 操作符模式
      op: approx
      val: ${nowTime}

```

#### (3) Database 数据库断言

支持异步重试。

```yaml
expect:
  database:
    table: users
    timeout: 3000                # 开启 3s 异步重试轮询
    rows:
      - _match: { user_id: "test-1234" }
        status: ACTIVE
        created_at: { op: approx, val: ${nowTime} }

```

---

## 四、 代码编写手册

### 4.1 测试类实现

继承 `TestifyBase`，并使用 DataProvider 绑定：

```java
public class UserCreateTest extends TestifyBase {

    @Test(
        dataProvider = "testifyData",
        dataProviderClass = io.github.loadup.testify.starter.testng.TestifyDataProvider.class)
    public void testCreateUser(String userId, String userName, String email) {
        // runTest 包裹业务代码，自动完成 Mock 注册与断言触发
        runTest(() -> {
            // val() 辅助函数从当前解析后的上下文中获取实时值
            userService.createUser(userId, val(userName), email);
        });
    }
}

```

### 4.2 操作符体系 (`op`)

| 操作符       | 场景                   | 示例                                      |
|-----------|----------------------|-----------------------------------------|
| `approx`  | 动态时间、数值近似比对          | `{op: approx, val: "${now}"}`           |
| `json`    | 调用 JSONAssert 进行局部比对 | `{op: json, mode: lenient, val: {...}}` |
| `notNull` | 非空检查                 | `{op: notNull}`                         |
| `gt / lt` | 数值范围校验               | `{op: gt, val: 0}`                      |
| `regex`   | 字符串正则匹配              | `{op: regex, val: "^138.*"}`            |

---

## 五、 高级特性与最佳实践

### 5.1 异常断言的执行原理

当你在 YAML 中定义了 `expect.exception` 时，`runTest` 内部的 Lambda 执行会被 `try-catch` 包裹。

* 如果业务代码没有抛出异常，断言失败。
* 如果抛出的异常类型或 Message 与 YAML 不符，断言失败。
* 这种机制让异常测试（如校验参数、权限拦截）变得极其简单。

### 5.2 异步链路处理

针对 MQ 消费入库延迟场景：

* 在 `database` 配置中声明 `timeout: 3000`。
* 框架会利用循环轮询机制，在 3 秒内不断尝试查询数据库。只要在超时前匹配成功，用例即判定为通过。

### 5.3 变量一致性规范

* **变量源**: 始终在 `variables` 中生成随机测试数据（如 `userId: ${time.now()}`）。
* **全链路引用**: 在 `input`（入参）、`mocks`（匹配）、`expect`（校验）中统一引用占位符，确保测试数据的“自洽”。

---

**Testify 框架通过高度抽象的 YAML 配置，将繁琐的集成测试转化为清晰的数据流配置，是保障复杂微服务链路稳定性的核心利器。**