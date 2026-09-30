# LoadUp Console

基于 [ElementAdmin v3](https://github.com/kailong321200875/vue-element-plus-admin) 搭建的 Vue 3 管理前端。引入的上游源码对应提交 `9047610a7ba478174a528916bff1ca44dcadc119`，其 MIT 许可见本目录的 `LICENSE`。模板的原始文档位于 `apps/docs`。

## 本地开发

需要 Node.js `^20.19.0 || ^22.13.0 || >=24.0.0`、pnpm `>=9.5.0`。在本目录执行：

```sh
corepack enable
pnpm install
pnpm dev:admin
```

管理端默认在 <http://localhost:4000/>。请先启动 LoadUp 后端，Vite 默认将 `/api` 代理到 `http://127.0.0.1:8080`。若后端地址不同，可在 `apps/admin/.env.base.local` 中设置：

```dotenv
VITE_BACKEND_URL=http://127.0.0.1:8081
```

生产部署时，请将 `/api` 反向代理到 LoadUp 后端。前端采用 hash 路由。

## 当前接入范围

- `POST /api/auth/login`：使用用户名和密码获取访问令牌，后续请求携带 `Bearer` 令牌。
- `POST /api/upms/permission/user-menu`：将 UPMS 菜单树转换成 ElementAdmin 动态路由。
- 首页为静态入口。没有 `componentPath` 或组件尚未创建的菜单展示占位页，待对应业务页面落地。
- Mock 功能仍保留于模板源码，但所有环境默认关闭。仅供独立查看上游演示，不代表真实接口。

UPMS 的用户、角色、部门、权限页面已提供查询、创建、编辑和删除操作。用户编辑支持角色分配，角色编辑支持权限与数据范围配置。菜单项没有对应页面时仍显示占位页。所有前端接口请求均使用 POST 和 JSON body；ID 请求使用 `{ "id": "..." }`，无参数请求使用 `{}`。

后端的权限变更需要刷新页面后重新加载导航菜单。部门父级在编辑时保持只读，可在创建下级部门时指定父级。
