# 本地集成启动器

`loadup-application` 组合框架组件、UPMS、审计中心、数据字典和文件资源管理，用于本地运行与验证。它不是供消费工程引入的生产模块；集成方应通过 BOM 按需选择各组件坐标。

## 使用

在本地准备 `application.yml` 所需的外部服务与环境变量，再运行应用主类。示例接口请求位于 `src/main/resources/Router.http`。修改依赖组合或配置后，应按需运行该模块的验证；数据库与认证配置由本应用的资源文件管理。

启动后访问 `/scalar` 浏览 UPMS 等 Spring MVC 接口，或访问 `/v3/api-docs` 获取 OpenAPI JSON。接口请求示例见 `src/main/resources/Router.http`。

本地启动器引入审计中心 Web 适配，按 `application.yml` 中的 `loadup.audit.web.include-paths` 记录 UPMS 管理操作与登录/注册事件；管理员可调用 `POST /api/audit/events/query` 查询。

数据字典通过 `/api/dictionaries/**` 提供类型和条目管理、启用选项查询；修改直接写入 MySQL，无需重新部署。本地示例还将字典管理操作纳入审计采集范围。

文件资源通过 `/api/files/**` 提供认证上传、私有下载、元数据查询和删除。示例使用本地 DFS binder，存储位置由 `LOADUP_FILE_STORAGE_PATH` 指定，默认 `./data/files`；多节点部署需换成共享存储 binder。Multipart 请求上限为 20MB。业务引用由可信应用代码调用 `FileResourceService.attach/detach` 维护。

## 设计

依赖组合和源码入口见 [ARCHITECTURE.md](./ARCHITECTURE.md)。
