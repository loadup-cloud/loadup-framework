# Data Dictionary

提供可动态维护的业务字典类型和条目。业务代码通过 `DictionaryService` 读取启用条目；数据保存在 MySQL，修改后无需重新部署。它不承担 ConfigCenter 的应用配置职责。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-modules-dictionary</artifactId>
</dependency>
```

提供 MySQL `DataSource` 和驱动。模块引入 Flyway，迁移脚本为 `db/migration/V20261002000002__create_data_dictionary.sql`。已有非空库首次启用 Flyway 时，按应用现有配置处理 baseline。需要 HTTP 接口时再引入 `loadup-modules-dictionary-web`。

## 业务 API

注入 `DictionaryService`，通过 `listEnabledItems(tenantId, typeCode)` 读取有效条目。类型或条目禁用后立即不再出现在结果中；管理端列表仍能查看禁用记录。类型编码创建后不可更改，统一转为小写；条目 value 创建后不可更改。同一租户内类型编码唯一，同一类型内 value 唯一。

类型支持创建、更新、分页查询、空类型删除；条目支持创建、更新、分页查询、删除。删除类型前必须先删除其条目。页大小为 1–100。没有租户上下文时使用保留租户 `__default__`。设置 `loadup.dictionary.enabled: false` 可关闭服务自动装配。

设计与持久化边界见 [ARCHITECTURE.md](ARCHITECTURE.md)。
