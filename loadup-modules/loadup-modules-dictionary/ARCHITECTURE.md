# Data Dictionary 架构

## 职责与边界

`loadup-modules-dictionary` 提供 `DictionaryService`、纯 Java 记录模型、`DictionaryRepository` 契约和 JDBC 实现；`loadup-modules-dictionary-web` 是可选 MVC 适配。模块不依赖 UPMS、审计中心或 ConfigCenter。

```text
类型/条目管理 → DictionaryService → DictionaryRepository → MySQL
有效条目读取 → DictionaryService → 启用类型检查 → 启用条目有序查询
```

## 数据与一致性

`dictionary_type` 按 `(tenant_id, type_code)` 唯一，`dictionary_item` 按 `(tenant_id, type_id, item_value)` 唯一，条目通过外键引用类型。两表保留标准 `id`、`tenant_id`、`created_at`、`updated_at`、`deleted` 列；当前删除为物理删除，类型有条目时拒绝删除。类型编码和值不可修改，避免已被业务引用的含义发生漂移。

写操作由 `@Transactional` 包裹。管理查询返回含停用数据的分页结果；业务读取只有在类型启用时才返回启用条目，按排序值、标签、value 排序。每次读取直接访问数据库，因此修改立即可见，不引入缓存失效问题。

## 租户与扩展

服务显式接收租户 ID；为空时映射到保留租户 `__default__`。Repository 的每个读写操作均限定租户。Web 层从 `TenantUtil` 获取租户，不接受客户端在请求体中指定租户。多租户应用仍需保证其租户上下文绑定可信。首版 MySQL DDL 与 Flyway 迁移随模块提供。

接入方式见 [README.md](./README.md)。
