# Testify Assert Engine

提供变量解析、Response / DB / Exception 三类断言及差异报告。变量引擎的公开入口为 `VariableEngine`，输入是变量 Map、字符串、
JsonNode 或集合，输出为展开后的测试数据，不进行数据库插入。Spring 使用者优先注入 starter 的 ScenarioAssert；直接使用引擎时显式提供
VariableEngine，数据库引擎额外需要 JdbcTemplate。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-testify-assert-engine</artifactId>
    <scope>test</scope>
</dependency>
```

通常使用 starter 自动装配的 VariableEngine；直接创建时显式注册函数：

```java
VariableEngine engine = new VariableEngine(List.of(new TimeFunction(), new CommonFunction()));
Map<String, Object> values = engine.resolveVariables(Map.of(
        "id", "${fn.uuid()}", "label", "case-${id}"));
Object input = engine.resolveValue(Map.of("id", "${id}"), values);
```

类型路径为 `io.github.loadup.testify.data.engine.variable` 与 `.function`。

## 变量引擎能力

| 能力       | 规则                                              |
|------------|---------------------------------------------------|
| 单占位符   | `${...}` 保留解析出的原始类型，例如数字           |
| 混合文本   | `prefix-${id}` 拼接为字符串                       |
| 变量引用   | `${id}` / `${#id}`，最多10轮依赖求解              |
| 结构递归   | JsonNode、Map、Collection 逐层处理                |
| Faker      | `${faker.name().firstName()}`，每次求值构建 Faker |
| 自定义函数 | 构造器显式注册 TestifyFunction 实例               |

### 内置函数

| 表达式                                 | 说明                         |
|----------------------------------------|------------------------------|
| `${fn.uuid()}`                         | UUID 字符串                  |
| `${fn.random(1,100)}`                  | [1,100)；max<=min 时返回 min |
| `${fn.randomString(8)}`                | 英文数字字符                 |
| `${time.now()}` / `${time.now('-2h')}` | 本地 LocalDateTime 文本      |
| `${time.format('uuuuMMdd')}`           | 当前本地日期格式化           |
| `${time.format('+1d','uuuu-MM-dd')}`   | 偏移后格式化                 |
| `${time.epochMilli()}`                 | epoch 毫秒                   |
| `${time.nativeNow('+1h')}`             | LocalDateTime 对象           |
| `${time.startOfDay('+1d')}`            | 偏移后日期的本地零点         |

偏移仅支持一个 `[+/-]数字[d|h|m|s]`，不支持月、年或复合偏移。TimeFunction 不注入 Clock，精确固定时间测试使用常量变量。

### 自定义函数接入

```java
public final class FixtureFunction implements TestifyFunction {
    public String getPrefix() { return "fixture"; }
    public String merchant() { return "merchant-test"; }
}

@Bean
VariableEngine testifyVariableEngine() {
    return new VariableEngine(List.of(
            new TimeFunction(), new CommonFunction(), new FixtureFunction()));
}
```

替换整个 VariableEngine Bean，保留所需内置函数。仅新增函数 Bean 不会被 starter 自动收集。同前缀后注册者覆盖前者，context
中的同名变量也可遮蔽函数对象，命名应避免冲突。

### 解析边界

解析失败或结果为 null 时，占位符通常原样保留；resolveVariables 不保证所有 key 最终出现。先检查必须的变量，再调用业务。原始非字符串变量不会在
resolveVariables 内递归展开，需要 resolveValue。

Map/JsonNode 对象的 null 项会被移除；Java Collection 的 null 会被过滤，而 JsonNode 数组保留
null。若测试空值语义，直接构造期望或显式断言，避免把解析器当作无损 JSON 转换器。仅处理可信测试资源；StandardEvaluationContext
不是表达式安全沙箱。

## 断言能力矩阵

| 引擎                  | 输入                                  | 比较方式                       |
|-----------------------|---------------------------------------|--------------------------------|
| ResponseAssertEngine  | 期望 JsonNode、实际对象、变量 context | 递归字段、JsonPath、操作符     |
| DbAssertEngine        | table/rows 定义、变量 context         | 数据库记录、行定位、计数与轮询 |
| ExceptionAssertEngine | 期望定义、Throwable、context          | 最深 cause 的类型与消息        |

## 响应断言

```yaml
status: CREATED
amount: {op: ge, val: 100}
'$.items[0].id': '${id}'
createdAt: {op: approx, val: '${now}', threshold: 1000}
```

普通字段递归比较，JsonPath 从实际结果中提取字段。实际对象可以包含未声明字段；需要严格 JSON 整体比较时在字段级使用 json /
mode: full。缺失路径、缺失字段和不匹配值会收集为字段 diff。

支持 eq、gt/ge/lt/le、regex、contains、size、approx、json；详见 [总体操作符表](../README.md)。approx threshold 默认3000ms；json
默认 lenient，full 表示严格比较。未知操作符回退 eq。顶层 Map 被 ScenarioAssert 当作对象字段结构，并不等价于字段级操作符声明。

## 数据库断言契约

```yaml
table: users
mode: lenient
timeout: 5000
interval: 200
rows:
  - _match: {user_id: '${id}'}
    user_id: '${id}'
    status: ACTIVE
```

- mode 默认 lenient；strict 对获取到的 actualRows 数量与 expectedRows 数量比较。
- timeout 默认0，interval 默认500，均为毫秒；仅 AssertionError 会按等待策略重试，数据库查询异常可能直接退出。
- `_match` 定位记录；其中普通值生成参数化相等 SQL，操作符值留到内存比较。
- 没有 `_match` 时按读取结果的行索引比较；SQL 不提供稳定排序。
- `_count` 检查匹配集合数量；0 可表达不存在。只要有期望行带 `_count`，strict 的普通总行数比较会被跳过。
- 无匹配条件或满足当前回退条件时，执行 `SELECT ... LIMIT 100`。不要用此入口证明大表完整性。
- 多条 `_match` 的结果集合拼接可能包含重复记录，计数测试应让条件互斥或只用一个匹配条件。
- 默认列名归一化为 caseInsensitive，不自动将 camelCase 转为 snake_case。建议使用数据库真实列名。

表/列标识符接受字母、数字和下划线，不接受引用符或 schema 前缀。测试数据需要独立数据库及限定 ID；SQL 准备不触发 Flex 审计和租户能力。

## 异常断言

```yaml
type: IllegalArgumentException
message: invalid input
```

引擎取最深 cause 后比较；不要把外层 CommonException 的 resultCode 当作根因类型。type 选择类名或完整类名；message
当前路径转换为字符串后进行比较，应使用文本相等定义。复杂消息、外层类型和 ResultCode 用 JUnit/AssertJ 验证更直接。

## 失败报告

FieldDiff 记录路径、期望、实际和原因；数据库通过 RowDiff 增加行号，再由 DiffReportBuilder 拼接 AssertionError
信息。测试报告可能包含样本字段，使用合成数据，不将生产凭证作为期望输入。

引擎不负责调用服务、读取
HTTP、捕获异常、事务提交或生命周期清理；由测试代码显式安排。可执行示例在 [test 模块](../loadup-testify-test/README.md)。
