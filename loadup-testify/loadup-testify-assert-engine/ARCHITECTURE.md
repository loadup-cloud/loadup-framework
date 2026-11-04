# Assert Engine 架构

## 依赖与职责

assert-engine 在同一 JAR 内提供变量解析和三类断言，使用 Spring Expression/Context、Datafaker、JsonPath、JSONAssert、Spring
JDBC 与 commons-json。业务流程在外部测试中；引擎只接收期望、实际和已解析变量。三个断言引擎共享
TestifyAssertEngine.compare 契约。

## 变量引擎

### 责任边界

输入是测试定义和调用者提供的 context，输出是解析数据。引擎不存储测试状态、不读写业务数据库、不调用业务方法。

### 求值流程

1. 构造 VariableEngine 时按 getPrefix 收集函数，形成注册表。
2. resolveVariables 最多遍历10轮，跳过已解决的 key；只对原始字符串进行表达式求值。
3. 单个占位符经 SpEL 解析，保留值类型；混合文本逐段替换。
4. resolveValue 递归处理 Map、Collection 和 JsonNode。

每次求值新建 StandardEvaluationContext，加入 MapAccessor、函数对象、Faker 和 context 变量。context
的同名项可能覆盖已有函数。函数可调用公共实例方法；不存在 Spring Bean 自动扫描机制。

### 生命周期与可重复性

解析后的变量 Map 应限定在单个用例内，不能在每次断言重新生成随机 ID。默认时间函数使用本地系统时钟；Faker/随机函数不提供统一
seed API。需要可重复结果时使用常量、记录用例输入或替换自定义函数。

### 失败语义与设计限制

求值异常被捕获并保留占位符，未解决变量可能不进入 context。不能把“没有抛错”解释为成功解析。零值、缺失值、null
在不同结构中的保留策略不同，接入手册列出差异。

自定义扩展通过构造器注册或替换 VariableEngine Bean。注册表不在运行期间更新；自定义函数需要自行保证测试并发安全。当前没有强校验模式、循环引用诊断或全局
Clock/seed 约束。变更需验证类型保留、依赖链、混合模板和 null 处理。

## 响应路径

ResponseAssertEngine 将实际对象转 JsonNode，遍历期望对象。顶层 `$` 键走 JsonPath；其他字段递归比较。叶值交给
OperatorProcessor，失败聚合为 FieldDiff。结构比较以期望为驱动，不默认拒绝实际对象的额外字段。

## 操作符分派

OperatorProcessor 使用固定静态 matcher 列表：Number、Regex、ApproxTime、Json、String、List。普通期望和无匹配操作符走
SimpleMatcher.eq。OperatorMatcher 不是自动发现的 Spring SPI；新增 Bean 不会改变静态分派表。

## 数据库路径

DbAssertEngine 先展开期望变量，再对一个表定义或表定义数组执行查询。普通 `_match` 条件下推 SQL，操作符条件在内存匹配；缺少匹配条件时存在
LIMIT 100 回退。验证包含列名归一化、strict 数量、_count、字段比较和缺失行报告。

轮询只捕获 AssertionError，使用 timeout/interval 毫秒与系统时钟。它不同于 starter 的 DbAssert（默认5秒/100ms，并暂存
RuntimeException）。SQL 查询不排序、多 `_match` 结果不去重，strict/_count 的判断范围必须由测试设计明确。

## 异常路径

ExceptionAssertEngine 沿 cause 找到最深异常，再比较类型与消息。当前消息节点先转换文本，因此操作符 Map
不能直接假定获得正常字段断言语义。实际参数若不是 Throwable 不会正常完成类型校验，调用者必须传入捕获的异常。

## 报告与扩展

FieldDiff/RowDiff 与 DiffReportBuilder 集中失败呈现；AssertionError 交由 JUnit 记录。替换 Spring 引擎 Bean
可以修改策略；新增操作符需要显式修改分派实现，不能仅注册 matcher Bean。

真实 HTTP/Jackson、表结构、事务可见性与环境故障属于外部验证，不是引擎的真实性承诺。现有无数据库回归在
ScenarioAssertTest；数据库场景在 UserServiceIT 和 UserServiceBatchIT。
