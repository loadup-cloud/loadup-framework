# Testify Assert Engine

提供 Response、DB、Exception 三类断言及差异报告。Spring 使用者优先注入 starter 的 ScenarioAssert；直接使用引擎时显式提供 VariableEngine，数据库引擎额外需要 JdbcTemplate。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-testify-assert-engine</artifactId>
    <scope>test</scope>
</dependency>
```

## 能力矩阵

| 引擎 | 输入 | 比较方式 |
|---|---|---|
| ResponseAssertEngine | 期望 JsonNode、实际对象、变量 context | 递归字段、JsonPath、操作符 |
| DbAssertEngine | table/rows 定义、变量 context | 数据库记录、行定位、计数与轮询 |
| ExceptionAssertEngine | 期望定义、Throwable、context | 最深 cause 的类型与消息 |

## 响应断言

```yaml
status: CREATED
amount: {op: ge, val: 100}
'$.items[0].id': '${id}'
createdAt: {op: approx, val: '${now}', threshold: 1000}
```

普通字段递归比较，JsonPath 从实际结果中提取字段。实际对象可以包含未声明字段；需要严格 JSON 整体比较时在字段级使用 json / mode: full。缺失路径、缺失字段和不匹配值会收集为字段 diff。

支持 eq、gt/ge/lt/le、regex、contains、size、approx、json；详见 [总体操作符表](../README.md)。approx threshold 默认3000ms；json 默认 lenient，full 表示严格比较。未知操作符回退 eq。顶层 Map 被 ScenarioAssert 当作对象字段结构，并不等价于字段级操作符声明。

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

引擎取最深 cause 后比较；不要把外层 CommonException 的 resultCode 当作根因类型。type 选择类名或完整类名；message 当前路径转换为字符串后进行比较，应使用文本相等定义。复杂消息、外层类型和 ResultCode 用 JUnit/AssertJ 验证更直接。

## 失败报告

FieldDiff 记录路径、期望、实际和原因；数据库通过 RowDiff 增加行号，再由 DiffReportBuilder 拼接 AssertionError 信息。测试报告可能包含样本字段，使用合成数据，不将生产凭证作为期望输入。

引擎不负责调用服务、读取 HTTP、捕获异常、事务提交或生命周期清理；由测试代码显式安排。可执行示例在 [test 模块](../loadup-testify-test/README.md)。
