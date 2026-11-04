# LoadUp Components Runtime

应用基础能力的 Spring Boot 装配层。引入 BOM 后添加依赖：

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-runtime</artifactId>
</dependency>
```

| 能力 | 默认行为 | 覆盖方式 |
| --- | --- | --- |
| Clock | `Clock.systemDefaultZone()` | 应用提供 `Clock` Bean |
| 日志 | 启动早期设置 console/file 默认格式 | `logging.pattern.*` 优先；`loadup.log.enabled=false` 关闭默认值 |
| JSON | Boot mapper builder 复用 `JsonUtil.customize` | 应用 Jackson 配置和 customizer |
| 容器访问工具 | 自动注册 `ApplicationContextUtils` | 应用提供同类型 Bean；新代码优先构造器注入 |

日志还支持 `loadup.log.include-trace-context`、`console-pattern`、`file-pattern`。
WebMVC 和 Observability 已传递引入本模块；非 Web 应用可直接引入。

迁移：原 `loadup-commons-time` 依赖替换为本模块；Clock 配置位于 `io.github.loadup.components.runtime.time`，
日志配置位于 `.logging`，ApplicationContextUtils 位于 `.context`。
`JsonUtil` 使用自己的固定默认 mapper；需要应用自定义序列化规则的代码应注入应用 mapper。

所有应用 `*-bootstrap` 必须显式声明 runtime 依赖。Spring 管理的通用 JSON 和 Clock 消费者通过构造器或 Bean 参数注入；
缺少共享 Bean 应暴露装配错误，不在消费者中创建兜底实例。测试可显式传入测试 mapper / Clock。
严格协议校验、诊断输出、JWT 协议、Redis 内部协议和启动早期配置解析保留专用实例，以免应用定制改变协议或启动语义。
