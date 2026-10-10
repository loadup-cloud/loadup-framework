# File Resources 架构

## 边界

`FileResourceService` 管理业务文件 ID、租户、所有者、元数据、引用和删除状态。它通过 `DfsService` 存取字节，不重新实现本地文件、数据库或对象存储 binder。`FileResourceGateway` 隔离 MySQL；可选 Web 模块负责身份与 HTTP 流式传输。模块不依赖 UPMS 或审计中心。

```text
业务/API → FileResourceService ┬→ FileResourceGateway → MySQL
                           └→ DfsService → 选定 DFS binder
```

## 数据与一致性

`file_resource` 记录业务 ID 与 DFS storage ID、租户/所有者、文件名、大小、类型、provider、状态和时间戳；`file_resource_reference` 记录业务引用，唯一键避免同一关联重复。引用增加与删除申请都先锁定文件行，再检查状态或引用计数，从而阻止“删除检查后又加引用”的竞态。

上传先完成 DFS 写入，再写 MySQL；MySQL 失败时尝试补偿删除 DFS 对象。如果补偿删除也失败，原异常附带清理异常，需按 DFS 运维手段清理孤儿对象。删除先在数据库事务内由 `ACTIVE` 变为 `PENDING_DELETE`，提交后清理 DFS，再置 `DELETED`。清理失败保留待处理状态，`cleanupPending` 可重试。若 DFS 对象已不存在，binder 的幂等删除仍可完成状态收敛。跨 MySQL 与 DFS 不承诺原子事务。

## 安全与部署

所有元数据查询按租户过滤；Web 入口从受信认证主体和租户上下文取得 owner/tenant，不接受请求体覆盖。只有所有者和超级管理员可读取或删除文件。Web 下载采用附件模式、`nosniff` 和流式传输；不会生成公开直链。业务引用不自动授予文件读取权限，业务域需要单独决定谁能查看附件。

HTTP 上传受 Spring Multipart 大小限制。对高风险文件类型的病毒扫描、内容审核、下载水印、保留期限和跨租户共享应在消费应用的业务规则中实现；首版不开放公开访问。

接入方式见 [README.md](README.md)。

## COLA 职责与装配

```text
web → app → domain
       ↓       ↑
     client  infrastructure → MySQL / Flyway
```

- client 定义不可变请求和 DTO，domain 保留领域模型与 `FileResourceGateway` 端口；应用边界由 `FileDTOConverter` 以 MapStruct Spring 模式映射。
- `FilePersistenceAutoConfiguration` 归 infrastructure，按 DataSource 和 `loadup.modules.file.enabled` 提供默认 Gateway，消费者可覆盖接口 Bean。
- app 在持久化装配之后按 Gateway 及所需组件条件创建 `FileResourceService`。生成的 converter 通过显式 `@Import` 注册，避免 REGISTER_BEAN 阶段条件与组件扫描冲突。
- web 只负责路由、可信身份/租户、方法授权和 HTTP 投影；请求契约归 client。全局响应、Jackson 与 `/api` 前缀由 WebMVC 组件处理。
- 仓储使用 MyBatis-Flex、Tables 常量与 Spring MapStruct；保留租户、锁、幂等及状态条件更新语义。Flyway 历史脚本保持原样，标准字段通过新增迁移补齐。
- 所有子模块 parent 指向根 `loadup-parent`，所有内部依赖坐标由 BOM 管理。

配置契约迁移到 `loadup.modules.file.*`，不保留旧前缀别名。自动装配、覆盖默认 Gateway 和关闭功能的回归源码见 `FileAutoConfigurationTest`，本次未执行；真实数据库与 HTTP 验收仍需由消费工程完成。

## 公共边界与映射约束

Facade 是 client 的业务契约，应用服务直接实现；Controller 和跨模块消费者依赖 Facade。domain 保留业务状态、规则与 Gateway，表示层字段转换交给 Spring 管理的 MapStruct。共享配置固定 Spring 模式、构造器注入和目标字段严格校验。

仓储依赖 database 的固定 UUID、审计时间、逻辑删除规则，显式声明空 BaseMapper，并通过模块生成的 Tables 表达查询。字典删除与文件引用解绑明确使用物理删除；文件状态、通知归档和任务生命周期是业务状态，独立于 BaseDO 的 deleted。

所有数据对象的诊断文本使用 commons-json；诊断序列化与真实 API JSON 分离，避免因 HTTP 脱敏设置改变日志中的凭证披露规则。

## Client 入参与分页边界

client.command 表达业务写动作，client.query 表达只读条件；Controller 和 Facade 共用入参契约。模块专用 PageDTO 已移除，app 将领域分页对象映射为 commons-dto 的 PageDTO；Web 输出 PageResponse，避免分页结构随模块变化。领域 Gateway 的分页返回值不携带 HTTP envelope。
