# Testify Spring Boot Starter

Testify 的主要消费入口，提供 ScenarioAssert、TestScenario、CaseFiles，以及数据/断言引擎的 Spring 自动配置。完整首次接入步骤和案例文件示例见 [总体接入手册](../README.md)。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-testify-spring-boot-starter</artifactId>
    <scope>test</scope>
</dependency>
```

另外显式声明 spring-boot-starter-test；需要真实数据库容器时添加 loadup-components-testcontainers 和 JDBC driver。Testify 不携带 JUnit/Boot Test，也不自动启动容器或创建表。

## 能力矩阵

| 类型 | 使用 | 数据库要求 |
|---|---|---|
| ScenarioAssert | verify、verifyJson、verifyException | 无需 DataSource Bean |
| ScenarioAssert.verifyDb | 多行/操作符/计数断言 | JdbcTemplate / DbAssertEngine |
| TestScenario | uuid、clean、insert、assertDb | JdbcTemplate |
| CaseFiles | loadYaml / loadJson | 无，读取 classpath |
| VariableEngine | 变量展开 | 无 |

默认没有 loadup.testify.* 配置项。容器属性属于 Testcontainers；等待属性在 DbAssert.withTimeout 或数据库断言定义中。无数据库测试要排除 Boot DataSourceAutoConfiguration，因为 starter 的 JDBC 依赖仍可能触发数据源创建。

## 注入与使用

通过测试类构造器注入，不要字段注入。变量引擎和断言引擎为普通 Bean；CaseFiles 为静态 classpath 加载器。链式 DbAssert 每次新建，不跨用例共享。

```java
assertions.verifyJson(actualJson, Map.of("$.data.status", "CREATED"));
scenario.assertDb("payment_order").where("id", id).exists().has("status", "CREATED");
```

两个片段的 actualJson、id 与已创建的表来自消费测试。标识符不支持反引号；默认异步断言5秒、100ms间隔。清理为真实 DELETE，不执行逻辑删除。

## 替换 Bean

自动配置使用 ConditionalOnMissingBean。替换 VariableEngine 时显式注册内置与自定义 TestifyFunction；仅创建函数 Bean 不会被收集。DbAssertEngine 可选，ScenarioAssert 使用 ObjectProvider 取得它；缺失时 verifyDb 抛出明确 AssertionError。

## 案例文件格式

支持 cases 数组、根数组、单个对象；name/id、input/params、expect/expected 为兼容别名。缺省名字为 case，variables 缺省为空。无 input/expect 的对象仍可加载，具体测试必须自行检查必需节点。

loadYaml/loadJson 路径是资源相对路径，例如 testcases/order/create.yaml，不加 classpath: 前缀。文件加载错误包装为 IllegalStateException。加载器不解析变量、不执行流程、不自动清理；调用过程见总手册的参数化测试示例。

## 验证参考

- TestifyAutoConfigurationTest：有/无 JDBC 条件装配。
- ScenarioAssertTest：声明式比较及失败报告。
- UserServiceIT / UserServiceBatchIT：真实数据库与批量案例。

相关命令和 Docker 条件见 [测试模块](../loadup-testify-test/README.md)。
