# Data Engine 架构

## 责任边界

输入是测试定义和调用者提供的 context，输出是解析数据。引擎不存储测试状态、不读写业务数据库、不调用业务方法。依赖 core、Spring
Expression/Context 和 Datafaker。

## 求值流程

1. 构造 VariableEngine 时按 getPrefix 收集函数，形成注册表。
2. resolveVariables 最多遍历10轮，跳过已解决的 key；只对原始字符串进行表达式求值。
3. 单个占位符经 SpEL 解析，保留值类型；混合文本逐段替换。
4. resolveValue 递归处理 Map、Collection 和 JsonNode。

每次求值新建 StandardEvaluationContext，加入 MapAccessor、函数对象、Faker 和 context 变量。context
的同名项可能覆盖已有函数。函数可调用公共实例方法；不存在 Spring Bean 自动扫描机制。

## 生命周期与可重复性

解析后的变量 Map 应限定在单个用例内，不能在每次断言重新生成随机 ID。默认时间函数使用本地系统时钟；Faker/随机函数不提供统一
seed API。需要可重复结果时使用常量、记录用例输入或替换自定义函数。

## 失败语义与设计限制

求值异常被捕获并保留占位符，未解决变量可能不进入 context。不能把“没有抛错”解释为成功解析。零值、缺失值、null
在不同结构中的保留策略不同，接入手册列出差异。

自定义扩展通过构造器注册或替换 VariableEngine Bean。注册表不在运行期间更新；自定义函数需要自行保证测试并发安全。当前没有强校验模式、循环引用诊断或全局
Clock/seed 约束。变更需验证类型保留、依赖链、混合模板和 null 处理。
