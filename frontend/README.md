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
- 首页和工作台为静态入口；系统管理页面来自 UPMS 菜单。没有对应组件的菜单显示占位页。
- Mock 演示数据仍保留，但所有环境默认关闭。开发环境通过 `vite-plugin-mock-dev-server` 提供 `/mock` 接口，已移除存在未修复漏洞的 MockJS 及旧插件；Mock 日志级别设为 warn，避免记录登录请求体。演示接口不代表真实业务接口。

UPMS 的用户、角色、部门、权限页面已提供查询、创建、编辑和删除操作。用户编辑支持角色分配，角色编辑支持权限与数据范围配置。新增审计中心、数据字典、文件资源、站内通知、导入导出任务与账号安全页面。审计和字典菜单由 UPMS `schema.sql` 授予超级管理员；已有数据库需要补入这两个菜单和角色关联后才能在导航中看到。

`/contract/*` 提供产品目录、条件配置、产品组合、销售方案和商户合约页面。目录页面从服务端加载版本，支持保存未发布版本、按行版本发布或下架，并可复制为新版本；关联项只能选择已发布版本。销售方案所选组合中至少要有一个默认选中的产品项，页面会在保存前提示。商户合约页使用服务端可信事实预览、幂等签约、分页查询和状态管理。未保存的表单内容仍只在当前页面内存中；条件编辑当前支持单层 ALL/ANY 与叶子条件，不支持任意深度嵌套。实际操作需要消费工程配置 Contract 方法权限及可信商户资料；本地管理员可通过 UPMS `schema.sql` 的 Contract 权限初始化语句授权，已有数据库需补执行并重新登录。

Controller 的 JSON 接口统一使用 POST + JSON body；ID 请求使用 `{ "id": "..." }`，无参数请求使用 `{}`。文件上传使用 multipart，下载使用流式二进制响应。

后端的权限变更需要刷新页面后重新加载导航菜单。部门父级在编辑时保持只读，可在创建下级部门时指定父级。

## 商户信息管理

`/merchants/list` 提供资料新增/编辑/启停/分页，使用 `/api/merchants/**`。登记号、详细地址和联系方式由 WebMVC 脱敏；编辑不回写掩码，敏感字段留空默认保留，可显式选择清空。签约页从启用商户列表选择服务端 id，基本行业资料供合约事实判断。源码已提供，未做类型检查、构建或端到端验证。

## 本地文档

文档源码位于 `apps/docs`，使用 `pnpm dev:docs` 本地预览、`pnpm build:docs` 构建静态文件。本仓库不再提供 Cloudflare 部署依赖或发布流程，托管方式由消费项目自行决定。

## 依赖安全维护

前端依赖以 `pnpm-lock.yaml` 为准。`pnpm.overrides` 对上游尚未更新的传递依赖设置限定范围的修复版本；Vue 统一使用 catalog 的 3.5.42，VitePress 内部 Vite 固定为 6.4.3。移除 Wrangler 后，不再引入 Cloudflare 的 miniflare、workerd、sharp 或 undici 工具链。

从 `frontend` 目录执行：

```sh
pnpm install --frozen-lockfile
pnpm audit --registry=https://registry.npmjs.org
pnpm --filter @vea/request test
pnpm --filter @vea/hooks test
pnpm build:admin
pnpm build:docs
```

显式指定官方 registry，因为当前配置的 npm 镜像不提供 audit API。2026-10-16 的 npm 审计仍有一项 `braces@3.0.3` 高危告警（[GHSA-vfj7-8cjw-p6xm](https://github.com/advisories/GHSA-vfj7-8cjw-p6xm)），来源为 stylelint / fast-glob 的 micromatch，尚无上游修复版本；未设置忽略规则或将其视为已修复。处理进展见根 ROADMAP。管理端整体类型检查仍受既有表格 DefaultRow 泛型错误阻塞，不能以打包成功代替类型检查通过。
