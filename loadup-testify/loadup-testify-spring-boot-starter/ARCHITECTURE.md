# Starter 架构

## 所有权

Starter 负责 Spring 集成与易用入口，依赖 core、data-engine 和 assert-engine。依赖 spring-boot-starter-jdbc 与 Jackson
YAML；消费工程自行提供 Spring Boot Test、JUnit、容器和数据源配置。

## 装配顺序

AutoConfiguration.imports 注册 TestifyAutoConfiguration；它在 Boot DataSourceAutoConfiguration 和
JdbcTemplateAutoConfiguration 后运行，并要求 JdbcTemplate 类可见。

TestScenario 和 DbAssertEngine 额外要求 JdbcTemplate Bean。VariableEngine、ResponseAssertEngine、ExceptionAssertEngine
始终按缺失 Bean 装配；ScenarioAssert 取得可选 DB 引擎。因此“JDBC 类在 classpath”与“已有可用 DataSource/JdbcTemplate
Bean”是不同条件。

## 门面与执行

ScenarioAssert 统一接收 Map、JsonNode、对象或 JSON 文本期望；对象级期望委托 ResponseAssertEngine，叶值走
OperatorProcessor。verifyJson 先解析实际 JSON；verifyDb 要求可选引擎存在；verifyException 接收测试显式捕获的 Throwable。

TestScenario 用受限制标识符和参数化值实现 JDBC 清理/插入。DbAssert 每次由 assertDb 创建，持有 criteria 和 timeout；where
累积相等条件，exists/notExists/has 独立轮询。单步成功不会重置或回滚业务数据。

## 文件层

CaseFiles 为 YAML、JSON 各持有一个私有 Mapper，读取 ClassPathResource 并规范化 TestifyCase。TestifyCase 保存原始
JsonNode，不自动变成业务 Command。JUnit MethodSource 负责枚举和执行，变量解析和业务绑定由测试显式调用。

## 设计边界

没有全局 AOP Mock、运行计划、HTTP transport、数据源创建或表迁移。默认函数列表固定，Bean 替换是当前扩展入口。两套 DB
断言的超时、错误重试与字段比较不同；只在准确记录契约后组合使用。

详细装配矩阵在 [总架构文档](../ARCHITECTURE.md)，接入示例在 [README](README.md)。
