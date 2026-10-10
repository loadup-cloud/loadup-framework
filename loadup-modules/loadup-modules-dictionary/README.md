# Data Dictionary

提供可动态维护的业务字典类型和条目。业务代码通过 `DictionaryService` 读取启用条目；数据保存在 MySQL，修改后无需重新部署。它不承担 ConfigCenter 的应用配置职责。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-modules-dictionary-app</artifactId>
</dependency>
```

提供 MySQL `DataSource` 和驱动。模块引入 Flyway，迁移脚本为 `db/migration/V20261002000002__create_data_dictionary.sql`。已有非空库首次启用 Flyway 时，按应用现有配置处理 baseline。需要 HTTP 接口时再引入 `loadup-modules-dictionary-web`。

## 业务 API

注入 `DictionaryService`，通过 `listEnabledItems(tenantId, typeCode)` 读取有效条目。类型或条目禁用后立即不再出现在结果中；管理端列表仍能查看禁用记录。类型编码创建后不可更改，统一转为小写；条目 value 创建后不可更改。同一租户内类型编码唯一，同一类型内 value 唯一。

类型支持创建、更新、分页查询、空类型删除；条目支持创建、更新、分页查询、删除。删除类型前必须先删除其条目。页大小为 1–100。没有租户上下文时使用保留租户 `__default__`。设置 `loadup.modules.dictionary.enabled: false` 可关闭服务自动装配。

设计与持久化边界见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## COLA 模块选择

本目录的 Maven 坐标 `loadup-modules-dictionary` 为聚合 POM。业务接入选择具体 jar，版本由根 BOM 管理：

| 子模块 | 用途 |
| --- | --- |
| [`client`](loadup-modules-dictionary-client/README.md) | 对外 DTO、请求契约；不依赖 Spring 或持久化实现 |
| [`domain`](loadup-modules-dictionary-domain/README.md) | 纯 Java 领域模型、分页值与 Gateway 接口 |
| [`infrastructure`](loadup-modules-dictionary-infrastructure/README.md) | 默认 MyBatis-Flex Gateway、Flyway 迁移与持久化装配 |
| [`app`](loadup-modules-dictionary-app/README.md) | 用例服务、DTO 映射与应用装配；程序化接入入口 |
| [`web`](loadup-modules-dictionary-web/README.md) | 可选 Spring MVC 适配；自动引入 app |
| [`test`](loadup-modules-dictionary-test/README.md) | 自动装配回归源码，不作为生产依赖 |

`DictionaryService` 现在位于 `io.github.loadup.modules.dictionary.app.service`；公开结果位于 `io.github.loadup.modules.dictionary.client.dto`。领域 Gateway 不引用客户端 DTO，也不引用数据库或 Spring API。HTTP 适配使用客户端契约，接口路径及 JSON 字段保持一致。

配置统一归入 `loadup.modules.dictionary`：

```yaml
loadup:
  modules:
    dictionary:
      enabled: true
      web:
        enabled: true
```

仅引入 app 时不注册 Controller；关闭 web 开关保留程序化服务，关闭模块 enabled 开关同时停止默认应用与持久化 Bean 装配。配置开关不控制 Flyway 对已在 classpath 上的脚本执行。不要在已有数据库重复复制迁移脚本。

## 统一接入契约

Java 消费方通过 `client.facade.XxxFacade` 注入公开业务入口；默认应用 Service 直接实现接口。引入 `*-app` 装配业务能力，引入 `*-web` 才提供 Controller，Web 适配不再提供独立 enabled 开关。模块整体启停仍使用 `loadup.modules.dictionary.enabled`。

JSON Controller 显式返回 SuccessResponse，分页保留已有分页报文契约；异常由全局 WebMVC 处理。下载仍为流式响应。请求与 DTO 字段声明 OpenAPI，凭证只写。持久化经 database 组件使用 MyBatis-Flex、Tables 常量和 Spring MapStruct Converter；数据库连接与可信租户来源由消费工程配置。新 schema 迁移与本轮 clean 编译、运行验证仍需本地执行。
