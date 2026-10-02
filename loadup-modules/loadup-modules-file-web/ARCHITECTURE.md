# File Resources Web 架构

`FileResourceController` 是 `FileResourceService` 的可选 MVC 适配。自动配置在核心服务存在时才创建 Controller；JSON 响应复用全局 WebMVC envelope 和 SpringDoc，二进制下载使用 `StreamingResponseBody`，不进入 JSON 包装。

所有入口要求已认证，主体必须是资源服务器提供的 `LoadUpUser`，其 `userId` 作为文件所有者和访问主体。租户来自 `TenantUtil`，不从请求参数读取。超级管理员身份由 `ROLE_SUPER_ADMIN` authority 决定。

下载先检查元数据权限，流开始后再由服务检查一次权限并打开 DFS stream，响应完成时关闭 stream。删除入口调用事务化标记方法，返回后执行存储清理，保证 DFS 操作不会发生在尚未提交的删除事务内。存储失败留待 `/cleanup` 或计划任务重试。

接入与接口见 [README.md](./README.md)。
