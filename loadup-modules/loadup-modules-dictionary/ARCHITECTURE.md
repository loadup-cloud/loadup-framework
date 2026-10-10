# Data Dictionary 架构

## 职责与边界

`loadup-modules-dictionary` 提供 `DictionaryService`、纯 Java 记录模型、`DictionaryGateway` 契约和 MyBatis-Flex 实现；`loadup-modules-dictionary-web` 是可选 MVC 适配。模块不依赖 UPMS、审计中心或 ConfigCenter。

```text
类型/条目管理 → DictionaryService → DictionaryGateway → MySQL
有效条目读取 → DictionaryService → 启用类型检查 → 启用条目有序查询
```

## 数据与一致性

`dictionary_type` 按 `(tenant_id, type_code)` 唯一，`dictionary_item` 按 `(tenant_id, type_id, item_value)` 唯一，条目通过外键引用类型。两表保留标准 `id`、`tenant_id`、`created_at`、`updated_at`、`deleted` 列；当前删除为物理删除，类型有条目时拒绝删除。类型编码和值不可修改，避免已被业务引用的含义发生漂移。

写操作由 `@Transactional` 包裹。管理查询返回含停用数据的分页结果；业务读取只有在类型启用时才返回启用条目，按排序值、标签、value 排序。每次读取直接访问数据库，因此修改立即可见，不引入缓存失效问题。

## 租户与扩展

服务显式接收租户 ID；为空时映射到保留租户 `__default__`。Repository 的每个读写操作均限定租户。Web 层从 `TenantUtil` 获取租户，不接受客户端在请求体中指定租户。多租户应用仍需保证其租户上下文绑定可信。首版 MySQL DDL 与 Flyway 迁移随模块提供。

接入方式见 [README.md](README.md)。

## COLA 职责与装配

```text
web → app → domain
       ↓       ↑
     client  infrastructure → MySQL / Flyway
```

- client 定义不可变请求和 DTO，domain 保留领域模型与 `DictionaryGateway` 端口；应用边界由 `DictionaryDTOConverter` 以 MapStruct Spring 模式映射。
- `DictionaryPersistenceAutoConfiguration` 归 infrastructure，按 DataSource 和 `loadup.modules.dictionary.enabled` 提供默认 Gateway，消费者可覆盖接口 Bean。
- app 在持久化装配之后按 Gateway 及所需组件条件创建 `DictionaryService`。生成的 converter 通过显式 `@Import` 注册，避免 REGISTER_BEAN 阶段条件与组件扫描冲突。
- web 只负责路由、可信身份/租户、方法授权和 HTTP 投影；请求契约归 client。全局响应、Jackson 与 `/api` 前缀由 WebMVC 组件处理。
- 仓储使用 MyBatis-Flex、Tables 常量与 Spring MapStruct；保留租户、锁、幂等及状态条件更新语义。Flyway 历史脚本保持原样，标准字段通过新增迁移补齐。
- 所有子模块 parent 指向根 `loadup-parent`，所有内部依赖坐标由 BOM 管理。

配置契约迁移到 `loadup.modules.dictionary.*`，不保留旧前缀别名。自动装配、覆盖默认 Gateway 和关闭功能的回归源码见 `DictionaryAutoConfigurationTest`，本次未执行；真实数据库与 HTTP 验收仍需由消费工程完成。

## 公共边界与映射约束

Facade 是 client 的业务契约，应用服务直接实现；Controller 和跨模块消费者依赖 Facade。domain 保留业务状态、规则与 Gateway，表示层字段转换交给 Spring 管理的 MapStruct。共享配置固定 Spring 模式、构造器注入和目标字段严格校验。

仓储依赖 database 的固定 UUID、审计时间、逻辑删除规则，显式声明空 BaseMapper，并通过模块生成的 Tables 表达查询。字典删除与文件引用解绑明确使用物理删除；文件状态、通知归档和任务生命周期是业务状态，独立于 BaseDO 的 deleted。

所有数据对象的诊断文本使用 commons-json；诊断序列化与真实 API JSON 分离，避免因 HTTP 脱敏设置改变日志中的凭证披露规则。

## Client 入参与分页边界

client.command 表达业务写动作，client.query 表达只读条件；Controller 和 Facade 共用入参契约。模块专用 PageDTO 已移除，app 将领域分页对象映射为 commons-dto 的 PageDTO；Web 输出 PageResponse，避免分页结构随模块变化。领域 Gateway 的分页返回值不携带 HTTP envelope。
