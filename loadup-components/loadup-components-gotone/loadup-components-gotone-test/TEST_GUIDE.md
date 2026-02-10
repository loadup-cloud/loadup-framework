# Gotone 组件测试文档

## 测试架构

Gotone 组件采用 **Testify 数据驱动测试框架** + **Testcontainers** 进行集成测试。

### 核心技术栈

- **Testify**: 声明式 YAML 配置测试框架
- **TestContainers**: 提供真实的 MySQL 容器环境
- **TestNG**: 测试执行引擎
- **Spring Boot Test**: Spring 集成测试支持

## 测试结构

```
loadup-components-gotone-test/
├── src/test/java/
│   └── io/github/loadup/components/gotone/test/
│       ├── GotoneTestBase.java              # 测试基类
│       ├── TestApplication.java             # 测试应用
│       └── service/
│           └── NotificationServiceTest.java # 服务测试类
└── src/test/resources/
    ├── application-test.yml                 # 测试配置
    └── testcases/
        └── service/
            └── NotificationServiceTest/
                ├── testSendNotification_Success.yaml
                ├── testSendNotification_WithIdempotence.yaml
                ├── testSendNotification_ServiceNotFound.yaml
                ├── testSendNotification_WithSpecifiedChannels.yaml
                └── testSendNotification_MultiChannel.yaml
```

## 测试用例说明

### 1. testSendNotification_Success
**测试场景**: 成功发送通知（EMAIL + SMS 多渠道）

**配置文件**: `testSendNotification_Success.yaml`

**验证点**:
- ✅ 自动识别收件人类型（邮箱 → EMAIL，手机号 → SMS）
- ✅ 多渠道并发发送
- ✅ 响应结果正确
- ✅ 数据库记录保存成功（2条记录）

### 2. testSendNotification_WithIdempotence
**测试场景**: 幂等性验证 - 相同 requestId 只发送一次

**配置文件**: `testSendNotification_WithIdempotence.yaml`

**验证点**:
- ✅ 重复请求被正确识别
- ✅ 返回幂等性提示信息
- ✅ 数据库记录数量不变

### 3. testSendNotification_ServiceNotFound
**测试场景**: 服务不存在异常处理

**配置文件**: `testSendNotification_ServiceNotFound.yaml`

**验证点**:
- ✅ 抛出 CommonException
- ✅ 异常消息正确

### 4. testSendNotification_WithSpecifiedChannels
**测试场景**: 指定渠道发送（只发送 EMAIL）

**配置文件**: `testSendNotification_WithSpecifiedChannels.yaml`

**验证点**:
- ✅ 只发送指定渠道
- ✅ 其他渠道不执行
- ✅ 数据库只有指定渠道的记录

### 5. testSendNotification_MultiChannel
**测试场景**: 多渠道并发发送（EMAIL + SMS + PUSH）

**配置文件**: `testSendNotification_MultiChannel.yaml`

**验证点**:
- ✅ 3个渠道同时发送
- ✅ 所有渠道都成功
- ✅ 数据库保存3条记录

## 运行测试

### 运行所有测试

```bash
cd loadup-components-gotone-test
mvn clean test
```

### 运行指定测试类

```bash
mvn test -Dtest=NotificationServiceTest
```

### 运行指定测试方法

```bash
mvn test -Dtest=NotificationServiceTest#testSendNotification_Success
```

## YAML 配置说明

### 基本结构

```yaml
variables:          # 变量定义（支持 Faker 和自定义函数）
  serviceCode: "ORDER_CREATED"
  userName: "${faker.name().firstName()}"

setup:              # 测试准备（数据库初始化）
  clean_sql: |
    DELETE FROM gotone_notification_record;
    INSERT INTO gotone_notification_service ...;

input:              # 测试输入参数（与方法参数顺序对应）
  - "${serviceCode}"
  - ["test@example.com"]
  - { orderNo: "12345" }

mocks:              # Mock 配置（AOP 拦截）
  - bean: "notificationChannelManager"
    method: "getProvider"
    args: ["EMAIL", "smtp"]
    thenReturn: { ... }

expect:             # 断言配置
  response:         # 响应断言
    "$.success": true
  database:         # 数据库断言
    table: gotone_notification_record
    rows:
      - _match: { service_code: "${serviceCode}" }
        status: "SUCCESS"
  exception:        # 异常断言
    type: "io.github.loadup.commons.error.CommonException"
    message: "服务不存在"
```

### 变量函数

**时间函数**:
```yaml
variables:
  nowTime: "${time.now()}"              # 当前时间戳
  orderNo: "ORDER_${time.now()}"        # 拼接时间戳
```

**Faker 函数**:
```yaml
variables:
  userName: "${faker.name().firstName()}"     # 随机名字
  email: "${faker.internet().emailAddress()}" # 随机邮箱
  phone: "${faker.phoneNumber().cellPhone()}" # 随机手机号
```

### 断言操作符

| 操作符 | 说明 | 示例 |
|--------|------|------|
| `approx` | 近似匹配（时间） | `{ op: approx, val: "${time.now()}" }` |
| `contains` | 包含 | `{ op: contains, val: "订单" }` |
| `size` | 集合大小 | `{ op: size, val: 3 }` |
| `notNull` | 非空 | `{ op: notNull }` |
| `gt / lt` | 大于/小于 | `{ op: gt, val: 0 }` |
| `regex` | 正则匹配 | `{ op: regex, val: "^138.*" }` |

### 数据库断言

**基本匹配**:
```yaml
database:
  table: gotone_notification_record
  rows:
    - _match: { service_code: "ORDER_CREATED" }
      status: "SUCCESS"
```

**计数断言**:
```yaml
database:
  table: gotone_notification_record
  rows:
    - _match: { service_code: "ORDER_CREATED" }
      _count: 2  # 必须有2条记录
```

**异步重试**:
```yaml
database:
  table: gotone_notification_record
  timeout: 3000  # 3秒内重试轮询
  rows:
    - _match: { status: "SUCCESS" }
```

## 测试最佳实践

### 1. 变量一致性

在 `variables` 中定义一次，在整个测试中复用：

```yaml
variables:
  serviceCode: "ORDER_CREATED"
  orderNo: "ORDER_${time.now()}"

setup:
  clean_sql: |
    DELETE FROM gotone_notification_service WHERE service_code = '${serviceCode}';

input:
  - "${serviceCode}"

expect:
  response:
    "$.serviceCode": "${serviceCode}"
```

### 2. 数据隔离

每个测试用例使用唯一的标识符：

```yaml
variables:
  testId: "${time.now()}"
  serviceCode: "TEST_${testId}"
```

### 3. 清理数据

在 `setup.clean_sql` 中删除测试数据：

```yaml
setup:
  clean_sql: |
    DELETE FROM gotone_notification_record WHERE service_code = '${serviceCode}';
    DELETE FROM gotone_service_channel WHERE service_code = '${serviceCode}';
    DELETE FROM gotone_notification_service WHERE service_code = '${serviceCode}';
```

### 4. Mock 策略

只 Mock 不可控的外部依赖（如第三方 API）：

```yaml
mocks:
  - bean: "notificationChannelManager"
    method: "getProvider"
    thenReturn: { ... }
```

数据库操作不需要 Mock，使用真实的 Testcontainers MySQL。

## 测试覆盖率

### 当前覆盖

| 模块 | 测试类 | 测试用例 | 场景覆盖 |
|------|--------|---------|---------|
| Service | 1 | 5 | ✅ 成功/幂等/异常/指定渠道/多渠道 |
| Manager | 待补充 | 待补充 | 规划中 |
| Processor | 待补充 | 待补充 | 规划中 |

### 目标覆盖率

- **行覆盖率**: ≥ 90%
- **分支覆盖率**: ≥ 85%
- **场景覆盖率**: ≥ 95%

## 常见问题

### 1. Testcontainers 启动慢

**解决方案**: 启用容器重用

```yaml
testcontainers:
  reuse:
    enable: true
```

### 2. YAML 配置找不到

**检查**:
- 文件路径是否正确：`src/test/resources/testcases/**/*.yaml`
- 文件命名是否与测试方法一致

### 3. 数据库断言失败

**调试步骤**:
1. 检查 `setup.clean_sql` 是否正确执行
2. 启用 SQL 日志：`logging.level.org.springframework.jdbc: DEBUG`
3. 检查字段名是否匹配（注意下划线命名）

## 参考资料

- [Testify 官方文档](../../loadup-testify/README.md)
- [TestContainers 文档](https://www.testcontainers.org/)
- [TestNG 文档](https://testng.org/doc/)

---

**测试框架版本**: Testify 0.0.2-SNAPSHOT  
**最后更新**: 2026-02-09

