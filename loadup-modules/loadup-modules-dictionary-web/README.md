# Data Dictionary Web Adapter

提供业务字典的 Spring MVC 管理接口和启用选项查询接口。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-modules-dictionary-web</artifactId>
</dependency>
```

本模块自动引入字典核心、WebMVC 响应约定和方法授权组件。所有接口以 `/api/dictionaries` 开头，返回统一的 `result`、`data` 报文；列表还包含 `pageInfo`。

| 方法 | 路径 | 用途 |
|---|---|---|
| POST / PUT / DELETE | `/types`、`/types/{id}` | 创建、更新/启停、删除类型 |
| GET | `/types?page=1&size=20` | 分页查询类型 |
| POST / GET | `/types/{typeCode}/items` | 创建、分页查询条目 |
| PUT / DELETE | `/items/{id}` | 更新/启停、删除条目 |
| GET | `/{typeCode}/options` | 查询启用选项，仅返回 value 和 label |

管理接口要求 `ROLE_SUPER_ADMIN`；选项接口由接入方的 `/api/**` 资源服务器规则保护。请求中的租户 ID 由当前 `TenantUtil` 上下文决定。设置 `loadup.dictionary.web.enabled: false` 可关闭 Web 适配。

控制器带有 OpenAPI 注解；引入可选的 `loadup-components-springdoc` 后可在 `/scalar` 浏览。设计见 [ARCHITECTURE.md](./ARCHITECTURE.md)。
