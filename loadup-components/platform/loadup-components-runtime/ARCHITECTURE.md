# Runtime 架构

本模块单向依赖 commons-json、commons-log、commons-context、commons-dto 及 Spring Boot，commons 不反向依赖 runtime。
通过 AutoConfiguration.imports 注册 Clock、日志属性、JSON customizer 和容器访问工具，不依赖业务包扫描或 Servlet 环境。

日志 EnvironmentPostProcessor 通过 spring.factories 注册，在日志系统初始化前添加最低优先级属性源，保持应用显式配置优先。
Clock 保持系统默认时区和 ConditionalOnMissingBean 覆盖语义。

JSON builder 定制复用 commons 的纯 Jackson 规则，mapper 实例由 Boot 管理。禁止将应用 mapper 写回 JsonUtil 静态字段，
避免容器启动前后行为变化及多个上下文相互覆盖。诊断 mapper 与 HTTP 脱敏继续由各自模块管理。

原 Clock 和日志环境处理器测试随实现迁移；RuntimeAssemblyTest 覆盖非 Web JSON 装配与 commons mapper 隔离。

## MapStruct 共享配置

`io.github.loadup.components.runtime.mapping.LoadUpMapStructConfig` 统一 Spring component model、构造器注入以及
目标字段 ERROR / 来源字段 WARN 策略。转换器显式依赖 `loadup-components-runtime` 和 `org.mapstruct:mapstruct`，并使用
`@Mapper(config = LoadUpMapStructConfig.class)`。该接口是编译期配置，不注册为 Spring Bean，也不加入 AutoConfiguration.imports。

Runtime 将 MapStruct 标记为 optional；只使用 Clock、日志或 JSON 装配的应用不因此传递引入 MapStruct。

## 部署标识

`DeploymentAutoConfiguration` 通过 `DeploymentProperties` 读取唯一配置 `loadup.deployment.code`，装配不可变
`DeploymentIdentity`。code 匹配 `[a-z][a-z0-9-]{0,63}`，缺失或非法即启动失败，不提供默认部署、不读取请求值，也不回退到
tenant 概念。

`DeploymentIdentity` 只表达"本进程属于哪套部署"：它不包含数据库 schema、JDBC、Actor 身份或权限，也不写入
ExecutionContext/ScopedValue。WebMVC 请求校验、内部命名 HTTP 客户端和非数据库资源命名空间按需读取该常量；业务用例和
领域模型不依赖它选择数据。

