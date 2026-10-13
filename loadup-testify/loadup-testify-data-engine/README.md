# Testify Data Engine

负责测试数据的变量解析。公开入口为 `VariableEngine`，输入是变量 Map、字符串、JsonNode 或集合，输出为展开后的测试数据，不进行数据库插入。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-testify-data-engine</artifactId>
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

## 能力矩阵

| 能力 | 规则 |
|---|---|
| 单占位符 | `${...}` 保留解析出的原始类型，例如数字 |
| 混合文本 | `prefix-${id}` 拼接为字符串 |
| 变量引用 | `${id}` / `${#id}`，最多10轮依赖求解 |
| 结构递归 | JsonNode、Map、Collection 逐层处理 |
| Faker | `${faker.name().firstName()}`，每次求值构建 Faker |
| 自定义函数 | 构造器显式注册 TestifyFunction 实例 |

## 内置函数

| 表达式 | 说明 |
|---|---|
| `${fn.uuid()}` | UUID 字符串 |
| `${fn.random(1,100)}` | [1,100)；max<=min 时返回 min |
| `${fn.randomString(8)}` | 英文数字字符 |
| `${time.now()}` / `${time.now('-2h')}` | 本地 LocalDateTime 文本 |
| `${time.format('uuuuMMdd')}` | 当前本地日期格式化 |
| `${time.format('+1d','uuuu-MM-dd')}` | 偏移后格式化 |
| `${time.epochMilli()}` | epoch 毫秒 |
| `${time.nativeNow('+1h')}` | LocalDateTime 对象 |
| `${time.startOfDay('+1d')}` | 偏移后日期的本地零点 |

偏移仅支持一个 `[+/-]数字[d|h|m|s]`，不支持月、年或复合偏移。TimeFunction 不注入 Clock，精确固定时间测试使用常量变量。

## 自定义函数接入

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

替换整个 VariableEngine Bean，保留所需内置函数。仅新增函数 Bean 不会被 starter 自动收集。同前缀后注册者覆盖前者，context 中的同名变量也可遮蔽函数对象，命名应避免冲突。

## 解析边界

解析失败或结果为 null 时，占位符通常原样保留；resolveVariables 不保证所有 key 最终出现。先检查必须的变量，再调用业务。原始非字符串变量不会在 resolveVariables 内递归展开，需要 resolveValue。

Map/JsonNode 对象的 null 项会被移除；Java Collection 的 null 会被过滤，而 JsonNode 数组保留 null。若测试空值语义，直接构造期望或显式断言，避免把解析器当作无损 JSON 转换器。仅处理可信测试资源；StandardEvaluationContext 不是表达式安全沙箱。

完整案例见 [总体手册](../README.md)，批量执行见 [示例模块](../loadup-testify-test/README.md)。
