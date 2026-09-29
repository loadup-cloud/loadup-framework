# 技术组件

LoadUp 组件提供可独立引入的技术能力。应用通过 `loadup-dependencies` BOM 管理版本，再选择需要的组件坐标；多后端组件还须选择具体 binder。

| 能力 | 模块入口 |
|------|----------|
| 方法授权 | [authorization](loadup-components-authorization/README.md) |
| OAuth2 授权服务器 | [authserver](loadup-components-authserver/README.md) |
| JWT 资源服务器 | [resource-server](loadup-components-resource-server/README.md) |
| MVC 响应约定 | [webmvc](loadup-components-webmvc/README.md) |
| 缓存 | [cache](loadup-components-cache/README.md) |
| 验证码 | [captcha](loadup-components-captcha/README.md) |
| 配置中心 | [configcenter](loadup-components-configcenter/README.md) |
| 数据库 | [database](loadup-components-database/README.md) |
| 文件存储 | [dfs](loadup-components-dfs/README.md) |
| 扩展机制 | [extension](loadup-components-extension/README.md) |
| 幂等控制 | [globalunique](loadup-components-globalunique/README.md) |
| 通知 | [gotone](loadup-components-gotone/README.md) |
| 流水线 | [pipeline](loadup-components-pipeline/README.md) |
| 容错 | [resilience4j](loadup-components-resilience4j/README.md) |
| 重试任务 | [retrytask](loadup-components-retrytask/README.md) |
| 调度 | [scheduler](loadup-components-scheduler/README.md) |
| 签名 | [signature](loadup-components-signature/README.md) |
| OpenAPI | [springdoc](loadup-components-springdoc/README.md) |
| 测试容器 | [testcontainers](loadup-components-testcontainers/README.md) |

依赖方向与组件设计约束见 [ARCHITECTURE.md](./ARCHITECTURE.md)。
