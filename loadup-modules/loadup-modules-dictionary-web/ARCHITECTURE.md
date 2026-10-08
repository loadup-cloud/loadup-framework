# Data Dictionary Web Adapter 架构

`DictionaryWebAutoConfiguration` 在字典服务存在时注册 `DictionaryController`。管理操作调用 `DictionaryService`，由 `@PreAuthorize` 要求 `ROLE_SUPER_ADMIN`；业务选项接口只暴露启用条目的 `value` 和 `label`，实际身份校验仍由 Resource Server 的 `/api/**` 规则负责。

```text
HTTP /api/dictionaries/** → DictionaryController → DictionaryService → Repository
```

Web 层只负责接收请求、从 `TenantUtil` 读取租户、将分页结果转为 `PageDTO`。编码规范、唯一性、启停规则和事务均属于核心服务。应用可只引入核心模块，不暴露 HTTP 接口。

接入示例见 [README.md](README.md)。
