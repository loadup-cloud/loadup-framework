# Loadup Modules

通用业务能力聚合模块；业务模块按需引入。

## 子模块

- [`loadup-modules-upms`](loadup-modules-upms/README.md)
- [`loadup-modules-audit`](loadup-modules-audit/README.md)：操作审计存储与查询服务。
- [`loadup-modules-audit-web`](loadup-modules-audit-web/README.md)：可选 MVC 采集和管理员查询接口。
- [`loadup-modules-dictionary`](loadup-modules-dictionary/README.md)：业务字典类型与条目。
- [`loadup-modules-dictionary-web`](loadup-modules-dictionary-web/README.md)：可选字典管理与选项接口。
- [`loadup-modules-file`](loadup-modules-file/README.md)：文件元数据、业务引用与清理生命周期。
- [`loadup-modules-file-web`](loadup-modules-file-web/README.md)：可选上传、下载与文件管理接口。
- [`loadup-modules-notification`](loadup-modules-notification/README.md)：持久化站内收件箱与 Gotone `IN_APP` 渠道。
- [`loadup-modules-notification-web`](loadup-modules-notification-web/README.md)：可选消息发布及个人收件箱接口。
- [`loadup-modules-transfer`](loadup-modules-transfer/README.md)：导入导出任务编排、状态与进度。
- [`loadup-modules-transfer-web`](loadup-modules-transfer-web/README.md)：可选任务提交、查询与结果接口。

此 POM 用于 Maven 聚合；在消费工程中选择需要的具体 jar 坐标。

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

## 接入步骤

此目录是 Maven 聚合模块，不作为业务运行依赖。按用途选择子模块：

- [`loadup-modules-upms`](./loadup-modules-upms/README.md)：运行实现。
- [`loadup-modules-audit`](./loadup-modules-audit/README.md)：程序化审计记录。
- [`loadup-modules-audit-web`](./loadup-modules-audit-web/README.md)：HTTP 适配。
- [`loadup-modules-dictionary`](./loadup-modules-dictionary/README.md)：程序化字典查询与管理。
- [`loadup-modules-dictionary-web`](./loadup-modules-dictionary-web/README.md)：HTTP 适配。
- [`loadup-modules-file`](./loadup-modules-file/README.md)：程序化文件资源管理。
- [`loadup-modules-file-web`](./loadup-modules-file-web/README.md)：HTTP 适配。
- [`loadup-modules-notification`](./loadup-modules-notification/README.md)：程序化站内通知投递。
- [`loadup-modules-notification-web`](./loadup-modules-notification-web/README.md)：HTTP 适配。
- [`loadup-modules-transfer`](./loadup-modules-transfer/README.md)：程序化导入导出任务。
- [`loadup-modules-transfer-web`](./loadup-modules-transfer-web/README.md)：HTTP 适配。
